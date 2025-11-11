package com.easy.flowbalance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.easy.flowbalance.ui.SessionScreen
import com.easy.flowbalance.ui.theme.FlowBalanceTheme
import com.easy.flowbalance.viewmodel.SessionViewModel
import com.easy.flowbalance.viewmodel.SessionViewModelFactory

class SessionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
        require(sessionId > 0) { "Missing session id" }
        val viewModel: SessionViewModel by viewModels {
            SessionViewModelFactory(application, sessionId)
        }
        setContent {
            FlowBalanceTheme {
                val digest by viewModel.digest.collectAsState()
                SessionScreen(
                    digest = digest,
                    onBack = { finish() },
                    onUpdateInflow = { viewModel.updateInflow(it) },
                    onCreateOutgoing = { title, amount, category, millis ->
                        viewModel.addOutgoing(title, amount, category, millis)
                    },
                    onUpdateOutgoing = { viewModel.updateOutgoing(it) },
                    onDeleteOutgoing = { viewModel.deleteOutgoing(it) }
                )
            }
        }
    }
}


