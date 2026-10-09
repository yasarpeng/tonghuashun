package com.example.tonghuashun.data.repository

import com.example.tonghuashun.data.local.LocalStore
import com.example.tonghuashun.data.local.NoOpLocalStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自选股仓库，维护用户关注的股票代码集合（保留添加顺序）。
 * 每次增删都会写入 [LocalStore]，应用重启后自动恢复；首次启动使用默认自选。
 */
@Singleton
class WatchlistRepository @Inject constructor(
    private val store: LocalStore,
) {

    /** 单元测试 / 无 Context 场景：纯内存运行，不落盘。 */
    constructor() : this(NoOpLocalStore())

    private val gson = Gson()

    private val _codes = MutableStateFlow(restore())
    val codes: StateFlow<Set<String>> = _codes.asStateFlow()

    fun isFavorite(code: String): Boolean = _codes.value.contains(code)

    fun toggle(code: String) {
        val current = LinkedHashSet(_codes.value)
        if (!current.add(code)) current.remove(code)
        _codes.value = current
        persist()
    }

    fun add(code: String) {
        val current = LinkedHashSet(_codes.value)
        current.add(code)
        _codes.value = current
        persist()
    }

    fun remove(code: String) {
        val current = LinkedHashSet(_codes.value)
        current.remove(code)
        _codes.value = current
        persist()
    }

    /** 读取本地自选；没有存过或解析失败时回退到默认自选。 */
    private fun restore(): Set<String> {
        val raw = store.read(KEY_WATCHLIST) ?: return LinkedHashSet(DEFAULT_CODES)
        val list = runCatching { gson.fromJson(raw, Array<String>::class.java)?.toList() }
            .getOrNull()
            ?: return LinkedHashSet(DEFAULT_CODES)
        return LinkedHashSet(list)
    }

    private fun persist() {
        store.write(KEY_WATCHLIST, gson.toJson(_codes.value.toList()))
    }

    private companion object {
        const val KEY_WATCHLIST = "watchlist_codes"

        val DEFAULT_CODES = listOf("sh600519", "sz002594", "sz300750", "sh688981", "sz300059")
    }
}
