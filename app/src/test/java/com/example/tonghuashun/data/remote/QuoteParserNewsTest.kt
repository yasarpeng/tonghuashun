package com.example.tonghuashun.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class QuoteParserNewsTest {

    private fun feedJson(createTime: String, richText: String, tag: String = "公司"): String = """
        {"result":{"status":{"code":0,"msg":"OK"},"data":{"feed":{"list":[
          {"id":5147626,"rich_text":${quote(richText)},"create_time":"$createTime","tag":[{"name":"$tag"}]}
        ]}}}}
    """.trimIndent()

    private fun quote(s: String): String = "\"" + s.replace("\"", "\\\"") + "\""

    @Test
    fun parsesTitleFromBrackets() {
        val json = feedJson("2026-10-15 18:58:19", "【天马新材：签订经销协议】天马新材公告，公司近期与英格瓷签订经销协议。")
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            .parse("2026-10-15 18:58:49")!!.time
        val list = QuoteParser.parseSinaNews(json, now)
        assertEquals(1, list.size)
        assertEquals("天马新材：签订经销协议", list[0].title)
        assertEquals("公司", list[0].source)
        assertEquals("刚刚", list[0].time) // 30s 内
    }

    @Test
    fun relativeTimeMinutesAndHours() {
        val base = "2026-10-15 18:00:00"
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        val tenMinLater = now.parse("2026-10-15 18:10:00")!!.time
        val l1 = QuoteParser.parseSinaNews(feedJson(base, "【A】正文"), tenMinLater)
        assertEquals("10分钟前", l1[0].time)

        val twoHoursLater = now.parse("2026-10-15 20:00:00")!!.time
        val l2 = QuoteParser.parseSinaNews(feedJson(base, "【B】正文"), twoHoursLater)
        assertEquals("2小时前", l2[0].time)
    }

    @Test
    fun fallsBackToTruncatedTitleWithoutBrackets() {
        val longText = "这是一条没有书名号的长资讯内容".repeat(5)
        val json = feedJson("2026-10-15 18:00:00", longText)
        val list = QuoteParser.parseSinaNews(json)
        assertTrue(list[0].title.endsWith("…"))
        assertTrue(list[0].title.length <= 41)
    }

    @Test
    fun malformedJsonReturnsEmpty() {
        assertTrue(QuoteParser.parseSinaNews("not-json").isEmpty())
        assertTrue(QuoteParser.parseSinaNews("{}").isEmpty())
    }
}
