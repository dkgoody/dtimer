package com.dkgoody.dtimer

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle

//  Data Structure in sync with persistance storage - both SharedPreferences and SavedStateHandle
//  Updated when:
// 1. Settings are changed
// 2. Going to the background

class DTimerPersistentState(context: Context, savedStateHandle : SavedStateHandle?, size : Int = 2)
{
    companion object {
        val shared_prefs_ref = "com.dkgoody.dtimer"
        val TIMER_VALUE_KEY = "timer_value"
        val TIMER_TITLE_KEY = "timer_title"
        val CYCLE_KEY = "cycle"
        val WAKEUP_KEY = "wakeup"
        val FREEZE_KEY = "freeze"
        val STATE_KEY = "state"
    }

    val sp by lazy { context.getSharedPreferences(shared_prefs_ref,
        Context.MODE_PRIVATE
    ) }

    val ssh by lazy { savedStateHandle}

    private val _size = size
    private var _cycle = 0
    private var _timer_initial = IntArray(size) // in seconds
    private var _timer_title = Array<String>(size) { "" }
    private var _state = 0
    private var _freeze = 0L
    private var _wakeup = 0L

    fun cycle() = _cycle
    fun next_cycle(cycle : Int) = (cycle + 1)% _size
    fun timer_value(cycle : Int)  : Int = _timer_initial[cycle]
    fun timer_value_ms(cycle : Int)  : Long = _timer_initial[cycle]*1000L
    fun timer_title(cycle : Int)  : String = _timer_title[cycle]
    fun next_timer_value(cycle : Int)  : Int = _timer_initial[next_cycle(cycle)]
    fun wakeup() = _wakeup
    fun freeze() = _freeze
    fun state() = _state

    init
    {
        _timer_initial = sp.getString(TIMER_VALUE_KEY,
            _timer_initial.joinToString())!!
            .removeSurrounding("[","]")
            .replace(" ","")
            .split(",")
            .map{ it.toInt() }
            .toIntArray()

        _timer_title = sp.getString(TIMER_TITLE_KEY,
            _timer_title.joinToString())!!
            .split(",")
            .map{ it.trim() }
            .toTypedArray<String>()

        _wakeup = sp.getLong(WAKEUP_KEY, _wakeup)
        _cycle = sp.getInt(CYCLE_KEY, _cycle)
        _state = sp.getInt(STATE_KEY, _state)
        _freeze = sp.getLong(FREEZE_KEY, _freeze)

    }

    // Save settings
    fun set(cycle : Int, minutes : Int, seconds : Int, title : String, state : Int)  {

        _timer_initial[cycle] = minutes*60 + seconds
        _timer_title[cycle] = title
        _state = state
        if (ssh != null) {
            Log.i("DTimer", "has ssh")
            ssh!!.set(DTimerPersistentState.TIMER_VALUE_KEY, _timer_initial)
            ssh!!.set(DTimerPersistentState.TIMER_TITLE_KEY, _timer_title)
            ssh!!.set(STATE_KEY, _state)
            ssh!!.set(FREEZE_KEY, _freeze)
        }
        _apply()
    }

    // Save current state
    fun save(cycle : Int, freeze : Long, wakeup : Long, state : Int)  {

        _cycle = cycle
        _wakeup = wakeup
        _freeze = freeze
        _state = state

        _apply()
    }

    private fun _apply() {
        sp.edit()
            .putString(TIMER_VALUE_KEY, _timer_initial.joinToString())
            .putString(TIMER_TITLE_KEY, _timer_title.joinToString(){it})
            .putLong(WAKEUP_KEY, _wakeup)
            .putInt(CYCLE_KEY, _cycle)
            .putInt(STATE_KEY, _state)
            .putLong(FREEZE_KEY, _freeze)
            .apply()
    }
}