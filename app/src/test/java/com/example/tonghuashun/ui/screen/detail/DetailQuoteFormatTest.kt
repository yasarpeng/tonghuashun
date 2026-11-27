package com.example.tonghuashun.ui.screen.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class DetailQuoteFormatTest {
    @Test
    fun `ETF报价保留三位小数避免相邻买卖档显示为相同价格`() {
        assertEquals("1.293", detailPrice("510980", 1.293))
        assertEquals("1.294", detailPrice("510980", 1.294))
        assertEquals("1.289", detailPrice("159915", 1.289))
    }

    @Test
    fun `普通股票继续显示两位小数`() {
        assertEquals("1454.69", detailPrice("600519", 1454.69))
        assertEquals("10.50", detailPrice("000001", 10.5))
    }
    @Test
    fun `分时时间轴扣除午休且保留收盘端点`() {
        assertEquals(0, quoteMinuteIndex("09:30"))
        assertEquals(120, quoteMinuteIndex("11:30"))
        assertEquals(120, quoteMinuteIndex("13:00"))
        assertEquals(240, quoteMinuteIndex("15:00"))
        assertEquals(null, quoteMinuteIndex("invalid"))
    }

    @Test
    fun `前后切股在自选列表循环且空列表不可切换`() {
        val codes = listOf("sh510980", "sh600519", "sz000858")
        assertEquals("sh600519", adjacentQuoteCode(codes, "sh510980", 1))
        assertEquals("sz000858", adjacentQuoteCode(codes, "sh510980", -1))
        assertEquals("sh510980", adjacentQuoteCode(codes, "sz000858", 1))
        assertEquals(null, adjacentQuoteCode(emptyList(), "sh510980", 1))
    }

    @Test
    fun `成交价精度与用户看到的委托价一致`() {
        assertEquals(1.293, com.example.tonghuashun.util.QuoteFormat.normalizedPrice("sh510980", 1.2934), 0.0000001)
        assertEquals(1.294, com.example.tonghuashun.util.QuoteFormat.normalizedPrice("510980", 1.2936), 0.0000001)
        assertEquals(10.12, com.example.tonghuashun.util.QuoteFormat.normalizedPrice("600519", 10.1234), 0.0000001)
    }

}
