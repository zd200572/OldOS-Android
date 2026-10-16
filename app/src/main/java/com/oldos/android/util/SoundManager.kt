package com.oldos.android.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.oldos.android.R

object SoundManager {
    private var soundPool: SoundPool? = null
    private var unlockSoundId: Int = 0
    private var isLoaded: Boolean = false

    fun init(context: Context) {
        if (soundPool != null) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build().apply {
                setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0 && sampleId == unlockSoundId) {
                        isLoaded = true
                    }
                }
                unlockSoundId = load(context, R.raw.unlock, 1)
            }
    }

    fun playUnlockSound() {
        soundPool?.let { pool ->
            if (isLoaded && unlockSoundId != 0) {
                pool.play(unlockSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
            }
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isLoaded = false
    }
}
