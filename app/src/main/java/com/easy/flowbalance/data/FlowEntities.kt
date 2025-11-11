package com.easy.flowbalance.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flow_session",
    indices = [Index("month"), Index("year")]
)
data class FlowSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val year: Int,
    val month: Int,
    val expectedInflow: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "flow_outgoing",
    foreignKeys = [
        ForeignKey(
            entity = FlowSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("spentAt")]
)
data class FlowOutgoing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val title: String,
    val amount: Double,
    val spentAt: Long = System.currentTimeMillis(),
    val category: String = "General"
)


