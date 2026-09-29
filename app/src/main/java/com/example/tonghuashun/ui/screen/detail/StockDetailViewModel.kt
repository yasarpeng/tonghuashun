package com.example.tonghuashun.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonghuashun.data.model.KLineEntry
import com.example.tonghuashun.data.model.NewsItem
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.QuoteExtra
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.model.TimeShareEntry
import com.example.tonghuashun.data.repository.StockRepository
import com.example.tonghuashun.data.repository.WatchlistRepository
import com.example.tonghuashun.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * 详情页图表周期。顺序与同花顺个股页一致：分时 / 日K / 周K / 月K。
 * 周K、月K 由日K在前端聚合得到（免费接口不单独提供）。
 */
enum class ChartPeriod(val label: String) {
    TIME_SHARE("分时"),
    DAY_K("日K"),
    WEEK_K("周K"),
    MONTH_K("月K"),
}

data class DetailUiState(
    val loading: Boolean = true,
    val stock: Stock? = null,
    val extra: QuoteExtra? = null,
    val dailyKline: List<KLineEntry> = emptyList(),
    val weeklyKline: List<KLineEntry> = emptyList(),
    val monthlyKline: List<KLineEntry> = emptyList(),
    val timeShare: List<TimeShareEntry> = emptyList(),
    val prevClose: Double = 0.0,
    val orderBook: OrderBook = OrderBook(emptyList(), emptyList()),
    val news: List<NewsItem> = emptyList(),
    val message: String? = null,
) {
    fun klineOf(period: ChartPeriod): List<KLineEntry> = when (period) {
        ChartPeriod.WEEK_K -> weeklyKline
        ChartPeriod.MONTH_K -> monthlyKline
        else -> dailyKline
    }
}

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private companion object {
        /** 行情轮询间隔：现价/五档/分时按这个节奏刷新，K线不重复拉取 */
        const val REFRESH_INTERVAL_MS = 5_000L
    }

    private val fullCode: String = savedStateHandle[Routes.STOCK_DETAIL_ARG] ?: ""

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _period = MutableStateFlow(ChartPeriod.TIME_SHARE)
    val period: StateFlow<ChartPeriod> = _period.asStateFlow()

    val isFavorite: StateFlow<Boolean> = watchlistRepository.codes
        .map { it.contains(fullCode) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), watchlistRepository.isFavorite(fullCode))

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    init {
        load()
        viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                refreshQuote()
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            // 5 个接口（实时/扩展/日K/分时/五档）彼此独立，并发请求，避免串行等待
            val stockTask = async { stockRepository.getStock(fullCode) }
            val extraTask = async { stockRepository.getQuoteExtra(fullCode) }
            val klineTask = async { stockRepository.getDailyKLine(fullCode) }
            val timeShareTask = async { stockRepository.getTimeShare(fullCode) }
            val orderBookTask = async { stockRepository.getOrderBook(fullCode) }

            val stock = stockTask.await()
            val extra = extraTask.await()
            val daily = klineTask.await()
            val (ts, prevClose) = timeShareTask.await()
            val ob = orderBookTask.await()
            _uiState.value = DetailUiState(
                loading = false,
                stock = stock,
                extra = extra,
                dailyKline = daily,
                weeklyKline = aggregate(daily, ChartPeriod.WEEK_K),
                monthlyKline = aggregate(daily, ChartPeriod.MONTH_K),
                timeShare = ts,
                prevClose = if (prevClose > 0) prevClose else (stock?.prevClose ?: 0.0),
                orderBook = ob,
                news = stockRepository.getNews(),
                message = if (stock == null) "行情加载失败，请检查网络" else null,
            )
        }
    }

    fun setPeriod(p: ChartPeriod) {
        _period.value = p
    }

    /** 只刷新行情类数据（现价/扩展指标/五档/分时），不动K线，避免每次轮询都打一堆接口 */
    private suspend fun refreshQuote() {
        if (_uiState.value.stock == null) return
        coroutineScope {
            val stockTask = async { stockRepository.getStock(fullCode) }
            val extraTask = async { stockRepository.getQuoteExtra(fullCode) }
            val orderBookTask = async { stockRepository.getOrderBook(fullCode) }
            val timeShareTask = async { stockRepository.getTimeShare(fullCode) }

            val stock = stockTask.await() ?: return@coroutineScope
            val extra = extraTask.await()
            val ob = orderBookTask.await()
            val (ts, prevClose) = timeShareTask.await()
            val prev = _uiState.value
            _uiState.value = prev.copy(
                stock = stock,
                extra = extra ?: prev.extra,
                orderBook = if (ob.asks.isEmpty() && ob.bids.isEmpty()) prev.orderBook else ob,
                timeShare = if (ts.isEmpty()) prev.timeShare else ts,
                prevClose = if (prevClose > 0) prevClose else prev.prevClose,
            )
        }
    }

    fun toggleFavorite() = watchlistRepository.toggle(fullCode)

    /**
     * 把日K聚合成周K / 月K：开盘取首日、收盘取末日、最高最低取区间极值、成交量求和。
     */
    private fun aggregate(daily: List<KLineEntry>, period: ChartPeriod): List<KLineEntry> {
        if (daily.isEmpty()) return emptyList()
        val groups = LinkedHashMap<String, MutableList<KLineEntry>>()
        daily.forEach { entry ->
            val key = bucketKey(entry.label, period)
            groups.getOrPut(key) { mutableListOf() }.add(entry)
        }
        return groups.map { (key, list) ->
            KLineEntry(
                label = key,
                open = list.first().open,
                close = list.last().close,
                high = list.maxOf { it.high },
                low = list.minOf { it.low },
                volume = list.sumOf { it.volume },
            )
        }
    }

    private fun bucketKey(label: String, period: ChartPeriod): String {
        val date = runCatching { dayFormat.parse(label) }.getOrNull() ?: return label
        val cal = Calendar.getInstance(Locale.CHINA).apply { time = date }
        return when (period) {
            ChartPeriod.WEEK_K -> {
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                dayFormat.format(cal.time)
            }
            ChartPeriod.MONTH_K -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                dayFormat.format(cal.time)
            }
            else -> label
        }
    }
}
