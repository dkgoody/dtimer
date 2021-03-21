package com.dkgoody.dtimer


import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.*


class DTimerAttention(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)
    private val ringtone : Ringtone by lazy { RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(
        RingtoneManager.TYPE_ALARM))}

    override fun onInit(i: Int) {
        if (i == TextToSpeech.SUCCESS) {

            val result = tts.setLanguage(Locale.UK)
            //tts.setSpeechRate(0.99f)
            //tts.setPitch(0.9f)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.i("DTimer", "This Language is not supported")
            }
        } else {
            Log.i("DTimer", "Initialization failed")
        }
    }

    fun alert(message: String, voice: Boolean, ring : Boolean) {

        if (voice)
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)

        if (ring) {
            Log.i("DTimer", "ALARM")
            ringtone.play()
        }
    }

    fun cancel() {

        ringtone.stop()
    }
}