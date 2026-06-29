package com.magpiny.notafo.core

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

object SoundUtils {
    fun playDeletionSound(context: Context) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
