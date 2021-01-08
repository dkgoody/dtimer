package com.dkgoody.dtimer.ui.fragment

import android.content.Context
import android.content.res.Configuration
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dkgoody.dtimer.DTimerState
import com.dkgoody.dtimer.DTimerViewModel
import com.dkgoody.dtimer.R
import com.dkgoody.dtimer.databinding.RunningFragmentBinding

open class DTimerRunningFragment(cycle : Int, theme : Int) : Fragment() {

    private val cycle = cycle
    private val theme = theme
    private val ringtone : Ringtone  by lazy { RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))}
    private lateinit var binding  : RunningFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val contextThemeWrapper = ContextThemeWrapper(activity, theme)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)

        binding = DataBindingUtil.inflate(
            localInflater,
            R.layout.running_fragment,
            container,
            false
        )

        val viewModel by activityViewModels<DTimerViewModel>()
        binding.viewModel = viewModel

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.progressBar.visibility = View.GONE
        }

        binding.timer0.setOnClickListener {
            findNavController().navigate(R.id.setup_0)
        }

        binding.timer1.setOnClickListener {
            findNavController().navigate(R.id.setup_1)
        }

        binding.startButton.setOnClickListener {
            silence()
            viewModel.startTimer(cycle)
        }

        binding.pauseButton.setOnClickListener {
            silence()
            viewModel.pauseTimer()
        }

        binding.resumeButton.setOnClickListener {
            viewModel.resumeTimer()
        }

        binding.skipButton.setOnClickListener {
            viewModel.stopTimer()
            findNavController().navigate(R.id.run_next)
        }

        binding.cancelButton.setOnClickListener {
            findNavController().navigate(R.id.setup_this)
        }

        binding.main.setOnClickListener {
            val state =  binding.viewModel?.timerStatePeek()!!
            when (state) {
                DTimerState.RUNNING -> {
                    viewModel.pauseTimer()
                }
                DTimerState.PAUSED -> {
                    viewModel.startTimer(cycle)
                }
                DTimerState.IDLE -> {
                    viewModel.startTimer(cycle)
                }
                DTimerState.DONE -> {
                    silence()
                }
            }
        }


        viewModel.timerStateChangeEvent.observe(viewLifecycleOwner, Observer { state ->
            state.getContentIfNotHandledOrReturnNull()?.let {
                updateInterface(it)
            }
        })

        viewModel.buzzEvent.observe(this, Observer { buzzType ->
            buzzType.getContentIfNotHandledOrReturnNull()?.let {
                if (it != DTimerViewModel.BuzzType.NO_BUZZ) {
                    buzz(it.pattern)
                }
            }
        })

        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        silence()
    }

    override fun onResume() {

        binding.timer0.text = binding.viewModel!!.getTimerTitle(0)
        binding.timer1.text = binding.viewModel!!.getTimerTitle(1)
        binding.next.text = binding.viewModel!!.nextString(cycle)
        val state = binding.viewModel?.timerStatePeek()!!
        if (state == DTimerState.IDLE)
        {
            Log.i("DTimer", "START again")
            binding.viewModel?.startTimer(cycle)
        }
        super.onResume()
        updateInterface(binding.viewModel?.timerStatePeek()!!)
    }

    private fun updateInterface(state : DTimerState) {
            when (state) {
                DTimerState.RUNNING -> {
                    // <Restart Pause Next>
                    binding.pauseButton.visibility = View.VISIBLE
                    binding.resumeButton.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    silence()
                }
                DTimerState.PAUSED -> {
                    // <Restart Resume Next>
                    binding.pauseButton.visibility = View.GONE
                    binding.resumeButton.visibility = View.VISIBLE
                    binding.cancelButton.visibility = View.GONE
                    silence()
                }
                DTimerState.DONE -> {
                    // <Restart End Next>
                    binding.pauseButton.visibility = View.GONE
                    binding.resumeButton.visibility = View.GONE
                    binding.cancelButton.visibility = View.VISIBLE
                    attention()
                }
                else -> {
                    silence()
                }
            }
    }

    private fun attention() {
        ringtone.play()
    }

    private fun silence() {
            ringtone.stop()
    }

    private fun buzz(pattern: LongArray) {
        val vibrator =
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator? ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(pattern, -1)
        }
    }
}
