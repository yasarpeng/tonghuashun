package com.example.tonghuashun.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * 新浪财经实时行情接口。
 * 返回 GBK 编码文本，需带 Referer(由拦截器统一添加)。用 ResponseBody 手动按 GBK 解码。
 * 示例: https://hq.sinajs.cn/list=sh600519,sz000858
 */
interface SinaApi {
    @GET
    suspend fun getRealtime(
        @Url url: String,
    ): ResponseBody
}

/**
 * 腾讯财经接口：日K线 / 分时 / 实时。返回 JSON 或文本。
 */
interface TencentApi {
    // 日K线（前复权）: web.ifzq.gtimg.cn/appstock/app/fqkline/get?param=sh600519,day,,,320,qfq
    @GET("appstock/app/fqkline/get")
    suspend fun getKLine(
        @Query("param") param: String,
    ): ResponseBody

    // 分时: web.ifzq.gtimg.cn/appstock/app/minute/query?code=sh600519
    @GET("appstock/app/minute/query")
    suspend fun getMinute(
        @Query("code") code: String,
    ): ResponseBody
}

/**
 * 腾讯 qt 简版行情：单只股票一次返回市值 / 流通市值 / 市盈(TTM) / 市净率 / 量比 / 换手率 / 振幅
 * 等扩展字段，用于个股详情页的九宫格指标区。
 * 示例: https://qt.gtimg.cn/q=sh600329
 */
interface TencentQtApi {
    @GET
    suspend fun getQuote(
        @Url url: String,
    ): ResponseBody
}

/**
 * 新浪财经 7x24 直播资讯接口（UTF-8 JSON）。
 * zhibo_id=152 为财经全球直播（与同花顺 7x24 快讯同源）。
 * tag_id 用于分类过滤（0=全部）。
 * 示例: https://zhibo.sina.com.cn/api/zhibo/feed?page=1&page_size=20&zhibo_id=152&tag_id=0&dire=f&dpc=1
 */
interface SinaNewsApi {
    @GET("api/zhibo/feed")
    suspend fun getFeed(
        @Query("zhibo_id") zhiboId: Int = 152,
        @Query("tag_id") tagId: Int = 0,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("dire") dire: String = "f",
        @Query("dpc") dpc: Int = 1,
    ): ResponseBody
}
