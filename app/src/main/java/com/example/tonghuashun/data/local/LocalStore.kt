package com.example.tonghuashun.data.local

/**
 * 极简的本地键值存储抽象。
 *
 * 抽一层接口的目的：仓库层同时要能在两种环境里跑——
 * - App 运行时：由 Hilt 注入 [SharedPrefsLocalStore]，真正把数据落到本地；
 * - JVM 单元测试：直接 new 仓库时用 [NoOpLocalStore]，不依赖 Android Context 也能跑逻辑。
 */
interface LocalStore {

    /** 读取字符串，键不存在时返回 null。 */
    fun read(key: String): String?

    /** 写入字符串，返回后应已持久化。 */
    fun write(key: String, value: String)
}

/**
 * 不落盘的实现：写入即丢弃，读取恒为 null。
 * 供单元测试与无 Context 场景使用，保证仓库默认行为仍是纯内存。
 */
class NoOpLocalStore : LocalStore {
    override fun read(key: String): String? = null
    override fun write(key: String, value: String) = Unit
}
