package com.example.tonghuashun.ui.screen.finance

import androidx.lifecycle.ViewModel
import com.example.tonghuashun.data.model.FinanceProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor() : ViewModel() {

    private val _products = MutableStateFlow(
        listOf(
            FinanceProduct("同花顺钱包", "历史0回撤 灵活申赎", 1.42, "0折申购"),
            FinanceProduct("绩优中短债", "假期不打烊 立享8天收益", 2.85, "推荐"),
            FinanceProduct("红利精选混合", "低估值高股息 稳健配置", 3.68, null),
            FinanceProduct("CPO主题基金", "算力光模块 高弹性", 5.21, "高风险"),
            FinanceProduct("全球投资QDII", "分散配置 海外资产", 4.12, "全球"),
            FinanceProduct("闲钱理财好品", "假期赚更多 随时取用", 2.05, null),
        )
    )
    val products: StateFlow<List<FinanceProduct>> = _products.asStateFlow()
}
