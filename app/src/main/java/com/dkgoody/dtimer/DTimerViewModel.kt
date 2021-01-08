package com.dkgoody.dtimer

import android.app.*
import android.content.Context
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.*


// ViewModel
// Keep settings (in persistent state)
// Keep current cycle, state and timeleft
// Set/Start/Stop/Pause/Resume timer
// Sync with persistent state when going background and when settings changed
// Maintain buzz event

class DTimerViewModel(application: Application, private val savedStateHandle: SavedStateHandle)
    : AndroidViewModel(application) {

    private val _context = getApplication<Application>().applicationContext
    private var _persistentState = DTimerPersistentState(_context, savedStateHandle)

    enum class BuzzType(val pattern: LongArray) {
        ENDING_BUZZ(pattern = longArrayOf(0, 200)),
        NO_BUZZ(pattern = longArrayOf(0))
    }

    private val TIMER_INTERVAL_MS = 10L
    private val SECOND_TO_MS = 1000
    private val PROGRESS_SIZE = 1000
    private val SECONDS_TO_BUZZ = 10

    private val _timeleft = MutableLiveData<Long>() // Milliseconds
    private var _initialtime = 0L

    private lateinit var _countDownTimer : CountDownTimer

    val buzzEvent = MutableLiveData<DTimerEvent<BuzzType>>()
    val timerStateChangeEvent = MutableLiveData<DTimerEvent<DTimerState>>()

    val countDownString = Transformations.map(_timeleft) { time ->
        DateUtils.formatElapsedTime(time/1000)
    }

    fun nextString(cycle : Int) : String  {
        return DateUtils.formatElapsedTime(_persistentState.next_timer_value(cycle).toLong())
    }

    val progress = Transformations.map(_timeleft) { time ->
        when(_initialtime) {
            0L -> 0
            else -> (time*PROGRESS_SIZE / _initialtime).toInt()
        }
    }


    init {
        _timeleft.value = 0L
        buzzEvent.value = DTimerEvent(BuzzType.NO_BUZZ)
        timerStateChangeEvent.value = DTimerEvent(DTimerState.IDLE)
    }

    // For use in DTimerSettingFragment
    fun updateSettings(cycle : Int, minutes : Int, seconds : Int, title : String) {
        _persistentState.set(cycle, minutes, seconds, title, DTimerState.IDLE.ordinal)
    }

    fun getTimerMin(cycle : Int)  = _getTimer(cycle)/60
    fun getTimerSec(cycle : Int)  = _getTimer(cycle)%60
    private fun _getTimer(cycle : Int)  = _persistentState.timer_value(cycle)

    fun getTimerTitle(cycle : Int)  = _persistentState.timer_title(cycle)

    fun timerStatePeek() = timerStateChangeEvent.value!!.peek()

    fun pauseTimer() {
        _stopTimer(DTimerState.PAUSED)
    }

    fun stopTimer() {
        _stopTimer(DTimerState.IDLE)
    }

    private fun _stopTimer(state : DTimerState) {

        if (this::_countDownTimer.isInitialized) {
            _countDownTimer.cancel()
        }
        timerStateChangeEvent.value = DTimerEvent(state)
        buzzEvent.value = DTimerEvent<BuzzType>(BuzzType.NO_BUZZ)
    }


    fun startTimer(cycle : Int) : Boolean {
        _initialtime = _persistentState.timer_value_ms(cycle)
        _timeleft.value = _initialtime
        return resumeTimer()
    }

    fun resumeTimer() : Boolean {
        if (this::_countDownTimer.isInitialized) {
            _countDownTimer.cancel()
        }

        if (_timeleft.value!! == 0L) {
            Log.i("DTimer", "left is not null already")
            return false
        }

        Log.i("DTimer", "left=" + _timeleft.value!!)
        _countDownTimer = object : CountDownTimer(_timeleft.value!!, TIMER_INTERVAL_MS) {

            var ticksBetweenBuzz = 0L;
            override fun onTick(millisUntilFinished: Long) {
                _timeleft.value = millisUntilFinished
                if (millisUntilFinished < SECOND_TO_MS) {
                    _timeleft.value = 0L
                }
                if (millisUntilFinished <= SECONDS_TO_BUZZ*SECOND_TO_MS) {
                    if ((ticksBetweenBuzz%SECOND_TO_MS) == 0L ) {
                        buzzEvent.value = DTimerEvent<BuzzType>(BuzzType.ENDING_BUZZ)
                    }
                    ticksBetweenBuzz += TIMER_INTERVAL_MS
                }
            }
            override fun onFinish() {
                buzzEvent.value = DTimerEvent<BuzzType>(BuzzType.NO_BUZZ)
                timerStateChangeEvent.value  = DTimerEvent(DTimerState.DONE)
            }
        }
        timerStateChangeEvent.value = DTimerEvent(DTimerState.RUNNING)
        _countDownTimer.start()
        Log.i("DTimer", "starting timer")
        return true
    }


    fun putToBackground(context: Context) {

        val left = _timeleft.value!!

        _persistentState.save(
            _persistentState.cycle(),
            left,
            System.currentTimeMillis() + left,
            timerStatePeek().ordinal)

        when (timerStatePeek()) {
            DTimerState.RUNNING -> {
                if (left > 0) {
                    DTimerService.run(_context)
                }
            }
            DTimerState.PAUSED -> {
                DTimerNotifications.sendPauseNotification(
                    context,
                    _persistentState.cycle(),
                    _persistentState.timer_title(_persistentState.cycle())
                )
            }
        }
    }


    fun putToForeground(context: Context)
    {
        DTimerService.cancel(_context)

        _initialtime = _persistentState.timer_value_ms(_persistentState.cycle())
        timerStateChangeEvent.value = DTimerEvent(DTimerState.valueOf(_persistentState.state())!!)

        when (timerStatePeek()) {
            DTimerState.RUNNING -> {
                if (_persistentState.wakeup() > System.currentTimeMillis()) {
                    _timeleft.value = _persistentState.wakeup() - System.currentTimeMillis()
                    resumeTimer()
                }
            }
            DTimerState.PAUSED -> {
                _timeleft.value = _persistentState.freeze()
            }
            else -> {
                _timeleft.value = 0
            }
        }
    }
}