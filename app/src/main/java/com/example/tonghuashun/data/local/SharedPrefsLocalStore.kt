package com.example.tonghuashun.data.local

import android.annotation.SuppressLint
import android.content.Context

/**
 * SharedPreferences 实现：用 JSON 字符串存快照，进程被杀后下次启动仍在。
 *
 * 这里刻意用 `commit()` 同步落盘而不是 `apply()`：写入量很小（账户快照 / 自选代码
 * 只有几 KB），且都发生在用户操作时，同步写换来的"返回即已持久化"更可靠。
 */
class SharedPrefsLocalStore(context: Context) : LocalStore {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(key: String): String? = prefs.getString(key, null)

    @SuppressLint("ApplySharedPref")
    override fun write(key: String, value: String) {
        prefs.edit().putString(key, value).commit()
    }

    private companion object {
        const val PREFS_NAME = "tonghuashun_local_store"
    }
}
