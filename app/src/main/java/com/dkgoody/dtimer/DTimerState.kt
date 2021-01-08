package com.dkgoody.dtimer

enum class DTimerState(val value: Int) {
    IDLE(0),        // just start on resume
    DONE(1),        // go to next
    RUNNING(2),
    PAUSED(3);

    companion object {
        fun valueOf(value: Int) = DTimerState.values().find { it.value == value }
    }
}
