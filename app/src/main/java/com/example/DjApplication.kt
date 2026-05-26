package com.example

import android.app.Application
import com.example.data.DjDatabase
import com.example.data.DjRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DjApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { DjDatabase.getDatabase(this) }
    val repository by lazy { DjRepository(database.djDao()) }

    override fun onCreate() {
        super.onCreate()
        // Run pre-population on startup
        applicationScope.launch {
            repository.checkAndPrePopulate()
        }
    }
}
