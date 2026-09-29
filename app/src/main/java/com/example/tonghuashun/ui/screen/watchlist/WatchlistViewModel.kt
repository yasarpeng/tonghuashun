package com.example.tonghuashun.ui.screen.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonghuashun.data.model.MarketIndex
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.repository.StockRepository
import com.example.tonghuashun.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: StockRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private companion object {
        /** 自选行情刷新间隔，与同花顺自选页的跳动节奏接近 */
        const val REFRESH_INTERVAL_MS = 5_000L
    }

    /** 顶部指数条 */
    private val _indices = MutableStateFlow<List<MarketIndex>>(emptyList())
    val indices: StateFlow<List<MarketIndex>> = _indices.asStateFlow()

    /** 自选股实时列表（价格先出，分时迷你走势并行回填） */
    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    /** 定时刷新与自选增删触发的刷新可能并发，加锁避免互相覆盖 */
    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            _indices.value = runCatching { repository.getIndices() }.getOrDefault(emptyList())
        }
        // 自选增删后立即刷新
        viewModelScope.launch {
            watchlistRepository.codes.collect { refresh() }
        }
        // 行情定时刷新
        viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                refresh()
            }
        }
    }

    /* ------------------ 搜索添加自选 ------------------ */

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Stock>>(emptyList())
    val searchResults: StateFlow<List<Stock>> = _searchResults.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    /** 当前自选代码集合，用于在搜索结果里标记"已添加" */
    val watchCodes: StateFlow<Set<String>> = watchlistRepository.codes

    fun onSearchQueryChange(q: String) {
        _searchQuery.value = q
        if (q.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searching.value = true
            _searchResults.value = runCatching { repository.searchStocks(q) }.getOrDefault(emptyList())
            _searching.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun addToWatchlist(fullCode: String) = watchlistRepository.add(fullCode)

    fun isInWatchlist(fullCode: String): Boolean = watchlistRepository.isFavorite(fullCode)

    /**
     * 刷新自选列表。
     *
     * 之前这里是「批量行情 -> 逐只串行拉分时 -> 一次性返回」，
     * 5 只自选只要分时接口慢一点，整页就要等十几秒才有内容。
     * 现在改成：1 次批量实时行情先渲染列表，再并行补齐分时迷你走势，逐只要到就逐只画出来。
     */
    private suspend fun refresh() = refreshMutex.withLock {
        val codes = watchlistRepository.codes.value.toList()
        if (codes.isEmpty()) {
            _stocks.value = emptyList()
            return@withLock
        }
        val realtime = runCatching { repository.getRealtime(codes) }.getOrDefault(emptyList())
        if (realtime.isEmpty()) return@withLock

        // 1) 先上价格，保证列表立即可见（复用已有的走势，避免闪烁）
        val cachedTrends = _stocks.value.associate { it.fullCode to it.trend }
        _stocks.value = realtime.map { it.copy(trend = cachedTrends[it.fullCode].orEmpty()) }

        // 2) 再并行补齐缺失的分时走势
        val missing = realtime.filter { cachedTrends[it.fullCode].isNullOrEmpty() }
        if (missing.isEmpty()) return@withLock
        coroutineScope {
            missing.map { stock ->
                async(Dispatchers.IO) {
                    val trend = runCatching {
                        repository.getTimeShare(stock.fullCode).first.map { it.price }
                    }.getOrDefault(emptyList())
                    if (trend.isNotEmpty()) {
                        _stocks.value = _stocks.value.map {
                            if (it.fullCode == stock.fullCode) it.copy(trend = trend) else it
                        }
                    }
                }
            }.awaitAll()
        }
    }

    fun remove(fullCode: String) = watchlistRepository.remove(fullCode)
}
