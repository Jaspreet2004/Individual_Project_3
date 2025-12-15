package com.example.individualproject3.util

import android.content.Context
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager

class SoundManager(private val context: Context) {
    // We use mp3 resources, but for this constraint we can use ToneGenerator for simple beeps
    // Use ToneGenerator that works without files
    
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    fun playMoveSound() {
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    fun playWinSound() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
    }

    fun playLoseSound() {
        toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 500)
    }

    fun playCoinSound() {
        toneGen.startTone(ToneGenerator.TONE_DTMF_1, 150)
    }

    fun playClickSound() {
        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 50)
    }

    fun playCorrectSound() {
        toneGen.startTone(ToneGenerator.TONE_SUP_PIP, 150) // High short beep
    }

    fun playWrongSound() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 300) // Lower buzz
    }

    fun playBGM() {
    }
    
    fun stopBGM() {
    }
}
