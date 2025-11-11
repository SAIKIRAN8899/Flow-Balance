package com.easy.flowbalance.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easy.flowbalance.data.FlowDatabase
import com.easy.flowbalance.data.FlowRepository
import com.easy.flowbalance.data.FlowSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimelineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FlowRepository(
        FlowDatabase.getInstance(application).sessionDao(),
        FlowDatabase.getInstance(application).outgoingDao()
    )

    val sessions = repository.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createSessionQuick(label: String? = null, inflow: Double = 0.0) {
        viewModelScope.launch {
            repository.createSession(
                label = label ?: "Current Cycle",
                desiredYear = null,
                desiredMonth = null,
                expectedInflow = inflow
            )
        }
    }

    fun createSessionExplicit(label: String, year: Int, month: Int, inflow: Double) {
        viewModelScope.launch {
            repository.createSession(
                label = label,
                desiredYear = year,
                desiredMonth = month,
                expectedInflow = inflow
            )
        }
    }

    fun updateSession(session: FlowSession) {
        viewModelScope.launch { repository.updateSession(session) }
    }

    fun deleteSession(session: FlowSession) {
        viewModelScope.launch { repository.deleteSession(session) }
    }
}


