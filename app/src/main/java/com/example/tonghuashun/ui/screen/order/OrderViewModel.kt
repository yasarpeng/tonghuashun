package com.example.tonghuashun.ui.screen.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonghuashun.data.model.Order
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.OrderSide
import com.example.tonghuashun.data.model.OrderStatus
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.repository.StockRepository
import com.example.tonghuashun.data.repository.TradeRepository
import com.example.tonghuashun.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderUiState(
    val loading: Boolean = true,
    val stock: Stock? = null,
    val orderBook: OrderBook = OrderBook(emptyList(), emptyList()),
    val available: Double = 0.0,       // 可用资金
    val availableShares: Long = 0L,    // 可卖数量
    val message: String? = null,
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository,
    private val tradeRepository: TradeRepository,
) : ViewModel() {

    private val fullCode: String = savedStateHandle[Routes.ORDER_ARG_CODE] ?: ""
    val side: OrderSide =
        if ((savedStateHandle.get<String>(Routes.ORDER_ARG_SIDE) ?: "buy") == "sell") OrderSide.SELL else OrderSide.BUY

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    /** 用户输入的委托价与数量 */
    private val _price = MutableStateFlow(0.0)
    val price: StateFlow<Double> = _price.asStateFlow()

    private val _shares = MutableStateFlow(0L)
    val shares: StateFlow<Long> = _shares.asStateFlow()

    /** 当前委托列表（供下单页底部展示"当日委托"） */
    val orders: StateFlow<List<Order>> = tradeRepository.orders
        .map { list -> list.filter { it.status != OrderStatus.CANCELLED }.take(20) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        load()
        // 定时刷新行情
        viewModelScope.launch {
            while (true) {
                delay(4000)
                refreshQuote()
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val stock = stockRepository.getStock(fullCode)
            val ob = runCatching { stockRepository.getOrderBook(fullCode) }.getOrDefault(OrderBook(emptyList(), emptyList()))
            _uiState.value = OrderUiState(
                loading = false,
                stock = stock,
                orderBook = ob,
                available = tradeRepository.cash.value,
                availableShares = tradeRepository.availableShares(fullCode),
                message = if (stock == null) "行情加载失败" else null,
            )
            // 默认委托价 = 现价
            if (stock != null && _price.value <= 0.0) {
                _price.value = stock.price
            }
        }
    }

    private fun refreshQuote() {
        viewModelScope.launch {
            val stock = stockRepository.getStock(fullCode) ?: return@launch
            val ob = runCatching { stockRepository.getOrderBook(fullCode) }.getOrDefault(_uiState.value.orderBook)
            _uiState.value = _uiState.value.copy(
                stock = stock,
                orderBook = ob,
                available = tradeRepository.cash.value,
                availableShares = tradeRepository.availableShares(fullCode),
            )
        }
    }

    /** 价格步进（A股最小变动 0.01） */
    fun stepPrice(delta: Double) {
        val cur = if (_price.value > 0) _price.value else (_uiState.value.stock?.price ?: 0.0)
        _price.value = (cur + delta).coerceAtLeast(0.01).let { "%.2f".format(it).toDouble() }
    }

    fun setPrice(v: Double) { _price.value = if (v < 0) 0.0 else v }

    fun setPriceToLevel(v: Double) { _price.value = v }

    /** 数量步进（100 股/手） */
    fun stepShares(delta: Long) {
        _shares.value = (_shares.value + delta).coerceAtLeast(0)
    }

    fun setShares(v: Long) { _shares.value = if (v < 0) 0 else v }

    /**
     * 按仓位比例快捷设置数量。
     * 买入：按可用资金 / 委托价 计算最大可买（向下取整到 100 股）后乘比例。
     * 卖出：按可卖数量乘比例。
     */
    fun setByRatio(ratio: Double) {
        val st = _uiState.value
        val p = if (_price.value > 0) _price.value else (st.stock?.price ?: return)
        val target = when (side) {
            OrderSide.BUY -> {
                if (p <= 0) return
                val maxShares = (st.available / p).toLong() / 100 * 100
                (maxShares * ratio).toLong() / 100 * 100
            }
            OrderSide.SELL -> {
                (st.availableShares * ratio).toLong() / 100 * 100
            }
        }
        _shares.value = target.coerceAtLeast(0)
    }

    /** 预估金额 */
    fun estimatedAmount(): Double {
        val p = if (_price.value > 0) _price.value else (_uiState.value.stock?.price ?: 0.0)
        return p * _shares.value
    }

    /**
     * 提交下单，返回结果消息。
     *
     * 采用"点了就成交"：**按你填写的价格**成交，不受现价与涨跌停限制，
     * 与交易页「买入 / 卖出」页的行为保持一致——填 8.88 就按 8.88 成交，不会按现价成交。
     */
    fun submit(): String {
        val stock = _uiState.value.stock ?: return "无行情"
        val p = if (_price.value > 0) _price.value else stock.price
        val qty = _shares.value
        if (qty <= 0) return "请输入委托数量"
        val r = tradeRepository.executeImmediately(
            fullCode = stock.fullCode,
            code = stock.code,
            name = stock.name,
            side = side,
            price = p,
            shares = qty,
        )
        return if (r.isSuccess) {
            _shares.value = 0
            // 刷新可用
            _uiState.value = _uiState.value.copy(
                available = tradeRepository.cash.value,
                availableShares = tradeRepository.availableShares(stock.fullCode),
            )
            "%s成交 %d 股 @ %.2f（%s）".format(side.label, qty, p, stock.name)
        } else {
            r.exceptionOrNull()?.message ?: "委托失败"
        }
    }
}
