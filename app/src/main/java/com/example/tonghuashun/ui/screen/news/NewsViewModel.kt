package com.example.tonghuashun.ui.screen.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonghuashun.data.model.NewsItem
import com.example.tonghuashun.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: StockRepository,
) : ViewModel() {

    private val _news = MutableStateFlow<List<NewsItem>>(emptyList())
    val news: StateFlow<List<NewsItem>> = _news.asStateFlow()

    private val _category = MutableStateFlow(0)
    val category: StateFlow<Int> = _category.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling(0)
    }

    /** 切换分类：立即重新拉取并重启轮询。 */
    fun selectCategory(index: Int) {
        if (_category.value == index) return
        _category.value = index
        startPolling(index)
    }

    /** 手动刷新当前分类。 */
    fun refresh() {
        viewModelScope.launch {
            val list = repository.getNews(_category.value)
            if (list.isNotEmpty()) _news.value = list
        }
    }

    private fun startPolling(category: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                val list = repository.getNews(category)
                // 分类未变时才更新，避免切换竞态；首次或刷新成功即覆盖
                if (_category.value == category && list.isNotEmpty()) {
                    _news.value = list
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 30_000L // 30s 轮询刷新 7x24 快讯
    }
}
