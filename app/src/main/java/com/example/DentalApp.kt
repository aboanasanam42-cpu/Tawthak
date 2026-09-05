package com.example

import android.app.Application
import com.example.data.local.DentalDatabase
import com.example.data.repository.DentalRepository
import com.example.util.SampleDataGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DentalApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { DentalDatabase.getInstance(this) }
    val repository by lazy { DentalRepository(database) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            SampleDataGenerator.seedInitialDataIfEmpty(this@DentalApp, repository)
        }
    }
}
