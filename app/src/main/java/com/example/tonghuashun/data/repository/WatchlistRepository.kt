package com.example.tonghuashun.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自选股仓库，内存维护用户关注的股票代码集合。
 * 应用重启后重置为默认自选。
 */
@Singleton
class WatchlistRepository @Inject constructor() {

    private val _codes = MutableStateFlow(
        linkedSetOf("sh600519", "sz002594", "sz300750", "sh688981", "sz300059")
    )
    val codes: StateFlow<Set<String>> = _codes.asStateFlow()

    fun isFavorite(code: String): Boolean = _codes.value.contains(code)

    fun toggle(code: String) {
        val current = LinkedHashSet(_codes.value)
        if (!current.add(code)) current.remove(code)
        _codes.value = current
    }

    fun add(code: String) {
        val current = LinkedHashSet(_codes.value)
        current.add(code)
        _codes.value = current
    }

    fun remove(code: String) {
        val current = LinkedHashSet(_codes.value)
        current.remove(code)
        _codes.value = current
    }
}
