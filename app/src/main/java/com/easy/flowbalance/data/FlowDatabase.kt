package com.easy.flowbalance.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FlowSession::class, FlowOutgoing::class],
    version = 1,
    exportSchema = true
)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun sessionDao(): FlowSessionDao
    abstract fun outgoingDao(): FlowOutgoingDao

    companion object {
        @Volatile
        private var INSTANCE: FlowDatabase? = null

        fun getInstance(context: Context): FlowDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlowDatabase::class.java,
                    "flow_balance.db"
                ).build().also { INSTANCE = it }
            }
    }
}


