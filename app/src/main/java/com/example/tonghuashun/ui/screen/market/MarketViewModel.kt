package com.example.tonghuashun.ui.screen.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonghuashun.data.model.MarketIndex
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketUiState(
    val indices: List<MarketIndex> = emptyList(),
    val gainers: List<Stock> = emptyList(),
    val losers: List<Stock> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: StockRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val indices = runCatching { repository.getIndices() }.getOrDefault(emptyList())
            val gainers = runCatching { repository.getTopGainers() }.getOrDefault(emptyList())
            val losers = runCatching { repository.getTopLosers() }.getOrDefault(emptyList())
            _uiState.value = MarketUiState(
                indices = indices,
                gainers = gainers,
                losers = losers,
                loading = false,
            )
        }
    }
}
