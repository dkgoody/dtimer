package com.dkgoody.dtimer.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dkgoody.dtimer.DTimerViewModel
import com.dkgoody.dtimer.R


class MainActivity : AppCompatActivity() {

    lateinit var viewModel : DTimerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel = viewModels<DTimerViewModel>().value
    }

    override fun onPause() {
        super.onPause()
        viewModel.putToBackground(this)
    }

    override fun onResume() {
        viewModel.putToForeground(this)
        super.onResume()
    }
}