package com.bohregard.workingwithworkmanager

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import androidx.work.CoroutineWorker
import kotlinx.coroutines.delay

class SimpleDelayWork(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val TAG = "TestWorker"

    override suspend fun doWork(): Result = coroutineScope {
        Log.d(TAG, "Starting Work")
        delay(10L * 1000L)
        Log.d(TAG, "Ending Work")
        Result.success()
    }
}