package com.easy.flowbalance.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

data class SessionDigest(
    val session: FlowSession,
    val outgoings: List<FlowOutgoing>,
    val spent: Double,
    val balance: Double
)

class FlowRepository(
    private val sessionDao: FlowSessionDao,
    private val outgoingDao: FlowOutgoingDao
) {

    fun observeSessions(): Flow<List<FlowSession>> = sessionDao.watchAll()

    fun observeSessionDigest(sessionId: Long): Flow<SessionDigest?> {
        val sessionFlow = sessionDao.watch(sessionId)
        val outgoingFlow = outgoingDao.watchForSession(sessionId)
        val totalFlow = outgoingDao.watchTotal(sessionId)
        return combine(sessionFlow, outgoingFlow, totalFlow) { session, outgoings, total ->
            session?.let {
                SessionDigest(
                    session = it,
                    outgoings = outgoings,
                    spent = total,
                    balance = it.expectedInflow - total
                )
            }
        }
    }

    suspend fun createSession(
        label: String,
        desiredYear: Int?,
        desiredMonth: Int?,
        expectedInflow: Double
    ): Long {
        val calendar = Calendar.getInstance()
        val year = desiredYear ?: calendar.get(Calendar.YEAR)
        val month = desiredMonth ?: (calendar.get(Calendar.MONTH) + 1)
        return sessionDao.insert(
            FlowSession(
                label = label,
                year = year,
                month = month,
                expectedInflow = expectedInflow
            )
        )
    }

    suspend fun updateSession(session: FlowSession) {
        sessionDao.update(session)
    }

    suspend fun deleteSession(session: FlowSession) {
        sessionDao.delete(session)
    }

    suspend fun getSession(sessionId: Long): FlowSession? = sessionDao.find(sessionId)

    suspend fun addOutgoing(
        sessionId: Long,
        title: String,
        amount: Double,
        category: String,
        timestamp: Long?
    ) {
        outgoingDao.insert(
            FlowOutgoing(
                sessionId = sessionId,
                title = title,
                amount = amount,
                category = category,
                spentAt = timestamp ?: System.currentTimeMillis()
            )
        )
    }

    suspend fun updateOutgoing(outgoing: FlowOutgoing) {
        outgoingDao.update(outgoing)
    }

    suspend fun removeOutgoing(outgoing: FlowOutgoing) {
        outgoingDao.delete(outgoing)
    }
}


