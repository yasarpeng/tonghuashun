package com.example.tonghuashun.data.local

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 把 App 级的本地存储注入给仓库层。 */
@Module
@InstallIn(SingletonComponent::class)
object LocalStoreModule {

    @Provides
    @Singleton
    fun provideLocalStore(@ApplicationContext context: Context): LocalStore =
        SharedPrefsLocalStore(context)
}
