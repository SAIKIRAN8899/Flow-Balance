package com.easy.flowbalance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.easy.flowbalance.ui.TrendsScreen
import com.easy.flowbalance.ui.theme.FlowBalanceTheme

class TrendsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowBalanceTheme {
                TrendsScreen(onBack = { finish() })
            }
        }
    }
}


