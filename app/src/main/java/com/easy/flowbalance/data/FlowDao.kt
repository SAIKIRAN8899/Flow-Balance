package com.easy.flowbalance.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlowSessionDao {
    @Query("SELECT * FROM flow_session ORDER BY year DESC, month DESC, id DESC")
    fun watchAll(): Flow<List<FlowSession>>

    @Query("SELECT * FROM flow_session ORDER BY year DESC, month DESC, id DESC")
    suspend fun fetchAll(): List<FlowSession>

    @Query("SELECT * FROM flow_session WHERE id = :sessionId")
    fun watch(sessionId: Long): Flow<FlowSession?>

    @Query("SELECT * FROM flow_session WHERE id = :sessionId")
    suspend fun find(sessionId: Long): FlowSession?

    @Insert
    suspend fun insert(session: FlowSession): Long

    @Update
    suspend fun update(session: FlowSession)

    @Delete
    suspend fun delete(session: FlowSession)
}

@Dao
interface FlowOutgoingDao {
    @Query(
        """
        SELECT * FROM flow_outgoing 
        WHERE sessionId = :sessionId 
        ORDER BY spentAt DESC, id DESC
        """
    )
    fun watchForSession(sessionId: Long): Flow<List<FlowOutgoing>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM flow_outgoing WHERE sessionId = :sessionId")
    fun watchTotal(sessionId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM flow_outgoing WHERE sessionId = :sessionId")
    suspend fun fetchTotal(sessionId: Long): Double

    @Insert
    suspend fun insert(outgoing: FlowOutgoing): Long

    @Update
    suspend fun update(outgoing: FlowOutgoing)

    @Delete
    suspend fun delete(outgoing: FlowOutgoing)
}


