package com.dkgoody.dtimer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dkgoody.dtimer.DTimerViewModel
import com.dkgoody.dtimer.R
import com.dkgoody.dtimer.databinding.SettingFragmentBinding


open class DTimerSettingFragment(cycle : Int, theme : Int) : Fragment() {

    private val cycle = cycle
    private val theme = theme

    protected lateinit var binding  : SettingFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val contextThemeWrapper =  ContextThemeWrapper(activity,  theme)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)

        binding = DataBindingUtil.inflate(
            localInflater,
            R.layout.setting_fragment,
            container,
            false
        )
        val viewModel by activityViewModels<DTimerViewModel>()

        binding.viewModel = viewModel

        binding.minPicker.minValue = 0
        binding.minPicker.maxValue = 59
        binding.minPicker.wrapSelectorWheel = true

        binding.secPicker.minValue = 0
        binding.secPicker.maxValue = 59
        binding.secPicker.wrapSelectorWheel = true

        binding.cycleTitle.setHint(when (cycle) { 0 -> getString(R.string.cycle_1_title) else -> getString(
                    R.string.cycle_0_title) })

        binding.startButton.setOnClickListener {
            updateViewModel()
            viewModel.startTimer(cycle)
            findNavController().navigate(R.id.run_this)
        }

        binding.backSign.setOnClickListener {
            updateViewModel()
            findNavController().navigate(R.id.setup_next)
        }

        binding.forwardSign.setOnClickListener {
            updateViewModel()
            findNavController().navigate(R.id.setup_next)
        }



        binding.setLifecycleOwner(this)


        return binding.root
    }

    override fun onPause() {
        updateViewModel()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.viewModel?.stopTimer()
        binding.minPicker.value = binding.viewModel?.getTimerMin(cycle) as Int
        binding.secPicker.value = binding.viewModel?.getTimerSec(cycle) as Int
        binding.cycleTitle.setText(binding.viewModel?.getTimerTitle(cycle) as String)

    }

    private fun updateViewModel() {
        binding.viewModel?.updateSettings(cycle,
            binding.minPicker.value,
            binding.secPicker.value,
            when (binding.cycleTitle.text.isBlank()) {
                true -> binding.cycleTitle.hint.toString()
                else -> binding.cycleTitle.text.toString()
            }, false)
    }
}