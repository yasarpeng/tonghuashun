package com.example.tonghuashun.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonghuashun.data.model.NewsItem
import com.example.tonghuashun.data.model.Sector
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val hotStocks: List<Stock> = emptyList(),
    val sectors: List<Sector> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    /** 「今天炒什么」右侧涨跌分布：取自热门股票池的实时涨跌家数 */
    val upCount: Int = 0,
    val downCount: Int = 0,
    val limitUpCount: Int = 0,
    val loading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StockRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            // 一次拉取股票池，热门股/涨跌分布都基于同一份实时数据，避免重复请求
            val pool = runCatching { repository.getStockPool() }.getOrDefault(emptyList())
            val sectors = repository.getSectors()
            val news = repository.getNews()
            _uiState.value = HomeUiState(
                hotStocks = pool.sortedByDescending { it.changePercent }.take(6),
                sectors = sectors,
                news = news,
                upCount = pool.count { it.change > 0 },
                downCount = pool.count { it.change < 0 },
                limitUpCount = pool.count { it.changePercent >= 9.8 },
                loading = false,
            )
        }
    }
}
