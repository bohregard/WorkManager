package com.bohregard.workingwithworkmanager

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import androidx.work.CoroutineWorker
import kotlinx.coroutines.delay

class ApiWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val TAG = "ApiWorker"

    override suspend fun doWork(): Result = coroutineScope {
        Log.d(TAG, "RunAttemptCount: $runAttemptCount")
        if(runAttemptCount == 2) {
            Result.success()
        } else {
            delay(5L * 1000L)
            Result.retry()
        }
    }
}