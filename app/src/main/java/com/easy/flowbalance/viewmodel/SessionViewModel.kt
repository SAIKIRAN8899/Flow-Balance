package com.easy.flowbalance.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easy.flowbalance.data.FlowDatabase
import com.easy.flowbalance.data.FlowOutgoing
import com.easy.flowbalance.data.FlowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(
    application: Application,
    private val sessionId: Long
) : AndroidViewModel(application) {

    private val repository = FlowRepository(
        FlowDatabase.getInstance(application).sessionDao(),
        FlowDatabase.getInstance(application).outgoingDao()
    )

    val digest = repository.observeSessionDigest(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updateInflow(newInflow: Double) {
        viewModelScope.launch {
            val session = repository.getSession(sessionId) ?: return@launch
            repository.updateSession(session.copy(expectedInflow = newInflow))
        }
    }

    fun addOutgoing(title: String, amount: Double, category: String, timestamp: Long?) {
        viewModelScope.launch {
            repository.addOutgoing(
                sessionId = sessionId,
                title = title,
                amount = amount,
                category = category,
                timestamp = timestamp
            )
        }
    }

    fun updateOutgoing(outgoing: FlowOutgoing) {
        viewModelScope.launch {
            repository.updateOutgoing(outgoing)
        }
    }

    fun deleteOutgoing(outgoing: FlowOutgoing) {
        viewModelScope.launch {
            repository.removeOutgoing(outgoing)
        }
    }
}

class SessionViewModelFactory(
    private val application: Application,
    private val sessionId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(application, sessionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


