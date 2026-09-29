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
