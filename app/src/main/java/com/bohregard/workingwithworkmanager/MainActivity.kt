package com.bohregard.workingwithworkmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wm = WorkManager.getInstance(this@MainActivity)

        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val testWork = OneTimeWorkRequestBuilder<TestWorker>()
                .setConstraints(constraints)
                .addTag("CustomTag")
                .build()
        wm.enqueue(testWork)
    }
}
