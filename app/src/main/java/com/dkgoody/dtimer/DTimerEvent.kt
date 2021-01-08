package com.dkgoody.dtimer

open class DTimerEvent<out T>(private val content: T) {
        var hasBeenHandled = false
            private set

        fun getContentIfNotHandledOrReturnNull(): T? {

            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peek() : T = content
    }