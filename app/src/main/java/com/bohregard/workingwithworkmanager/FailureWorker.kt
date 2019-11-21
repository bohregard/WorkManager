package com.bohregard.workingwithworkmanager

import android.content.Context
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import androidx.work.CoroutineWorker
import kotlinx.coroutines.delay

class FailureWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = coroutineScope {
        delay(10L * 1000L)
        Result.failure()
    }
}