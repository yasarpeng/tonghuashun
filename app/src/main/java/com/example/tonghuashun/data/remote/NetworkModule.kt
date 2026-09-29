package com.example.tonghuashun.data.remote

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // 新浪接口要求带 Referer，否则返回 403；统一添加常见浏览器 UA + Referer
        val headerInterceptor = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .header("Referer", "https://finance.sina.com.cn")
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile Safari/537.36",
                )
                .build()
            chain.proceed(req)
        }
        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("sina")
    fun provideSinaRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://hq.sinajs.cn/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("tencent")
    fun provideTencentRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://web.ifzq.gtimg.cn/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

    /** 腾讯 qt 简版行情（GBK） */
    @Provides
    @Singleton
    @Named("qt")
    fun provideQtRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://qt.gtimg.cn/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideSinaApi(@Named("sina") retrofit: Retrofit): SinaApi =
        retrofit.create(SinaApi::class.java)

    @Provides
    @Singleton
    fun provideTencentApi(@Named("tencent") retrofit: Retrofit): TencentApi =
        retrofit.create(TencentApi::class.java)

    @Provides
    @Singleton
    fun provideTencentQtApi(@Named("qt") retrofit: Retrofit): TencentQtApi =
        retrofit.create(TencentQtApi::class.java)
}
