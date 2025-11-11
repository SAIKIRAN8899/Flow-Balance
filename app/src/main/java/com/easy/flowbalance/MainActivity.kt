package com.easy.flowbalance

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.easy.flowbalance.ui.theme.FlowBalanceTheme
import com.easy.flowbalance.viewmodel.TimelineViewModel
import com.easy.flowbalance.ui.TimelineScreen

class MainActivity : ComponentActivity() {

    private val timelineViewModel: TimelineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowBalanceTheme {
                val sessions by timelineViewModel.sessions.collectAsState()
                TimelineScreen(
                    sessions = sessions,
                    onQuickCapture = { timelineViewModel.createSessionQuick(label = "Current Cycle") },
                    onCreate = { label, year, month, inflow ->
                        timelineViewModel.createSessionExplicit(label, year, month, inflow)
                    },
                    onEdit = { timelineViewModel.updateSession(it) },
                    onDelete = { timelineViewModel.deleteSession(it) },
                    onOpen = { session ->
                        startActivity(
                            Intent(this, SessionActivity::class.java).apply {
                                putExtra(SessionActivity.EXTRA_SESSION_ID, session.id)
                            }
                        )
                    },
                    onOpenTrends = {
                        startActivity(Intent(this, TrendsActivity::class.java))
                    }
                )
            }
        }
    }
}