package com.bohregard.workingwithworkmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val UNIQUE_WORK = "BackgroundWork"

    private val TAG = "MainActivity"

    private val wm by lazy { WorkManager.getInstance(this@MainActivity) }
    private val constraints by lazy {
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wm.getWorkInfosByTagLiveData("SimpleTag").observe(this@MainActivity, Observer {
            updateWorkStatus(it, one_time_work, one_time_work_status, "Enqueue One Time Work")
        })

        wm.getWorkInfosByTagLiveData("FailureTag").observe(this@MainActivity, Observer {
            updateWorkStatus(
                it,
                one_time_work_failure,
                one_time_work_failure_status,
                "Enqueue One Time Work"
            )
        })

        wm.getWorkInfosByTagLiveData("ChainedTag").observe(this@MainActivity, Observer {
            updateWorkStatus(
                it,
                chain_work,
                chain_work_status,
                "Chain Work"
            )
        })

        wm.getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK).observe(this@MainActivity, Observer {
            updateWorkStatus(it, api_fetch, api_status, "Unique Work With Retry")
        })

        one_time_work.setOnClickListener {
            if (it.tag != null) {
                wm.cancelWorkById(it.tag as UUID)
            } else {
                val delayedWork = OneTimeWorkRequestBuilder<SimpleDelayWork>()
                    .setConstraints(constraints)
                    .addTag("SimpleTag")
                    .build()
                it.tag = delayedWork.id
                wm.enqueue(delayedWork)
            }
        }

        api_fetch.setOnClickListener {
            if (it.tag != null) {
                wm.cancelWorkById(it.tag as UUID)
            } else {
                val uniqueWork = OneTimeWorkRequestBuilder<ApiWorker>()
                    // Defaults to Exponential @ 30 Seconds
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.SECONDS)
                    .setConstraints(constraints)
                    .build()
                it.tag = uniqueWork.id
                wm.enqueueUniqueWork(UNIQUE_WORK, ExistingWorkPolicy.KEEP, uniqueWork)
            }
        }

        one_time_work_failure.setOnClickListener {
            Log.d(TAG, "One Time Work Failure Clicked")
            if (it.tag != null) {
                wm.cancelWorkById(it.tag as UUID)
            } else {
                val uniqueWork = OneTimeWorkRequestBuilder<FailureWorker>()
                    // Defaults to Exponential @ 30 Seconds
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.SECONDS)
                    .addTag("FailureTag")
                    .setConstraints(constraints)
                    .build()
                it.tag = uniqueWork.id
                wm.enqueue(uniqueWork)
            }
        }

        chain_work.setOnClickListener {
            val delayedWork = OneTimeWorkRequestBuilder<SimpleDelayWork>()
                .setConstraints(constraints)
                .setInputData(workDataOf("TEST" to "NEW VALUE"))
                .addTag("ChainedTag")
                .build()

            val uniqueWork = OneTimeWorkRequestBuilder<SimpleDelayWork>()
                // Defaults to Exponential @ 30 Seconds
                .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.SECONDS)
                .addTag("ChainedTag")
                .setConstraints(constraints)
                .build()

            wm.beginWith(delayedWork)
                .then(uniqueWork)
                .enqueue()
        }

        prune.setOnClickListener {
            wm.cancelAllWork()
            wm.pruneWork()
        }
    }

    private fun updateWorkStatus(
        work: List<WorkInfo>,
        button: Button,
        status: TextView,
        positiveText: String
    ) {
        Log.d(TAG, "$work")
        val wi = work.firstOrNull { w -> !w.state.isFinished }

        status.text = if (work.all { it.state.isFinished } && work.isNotEmpty() && button.tag != null) {
            val finishedWork = work.first { it.id == button.tag as UUID }
            Log.d(TAG, "Data: ${finishedWork.outputData}")
            finishedWork.state.name
        } else {
            "${wi?.state ?: "NO WORK"}"
        }

        if (wi != null) {
            button.text = "Cancel Work"
            button.tag = wi.id
        } else {
            button.text = positiveText
            button.tag = null
        }
    }
}
