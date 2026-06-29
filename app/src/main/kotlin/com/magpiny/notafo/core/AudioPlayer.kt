package com.magpiny.notafo.core

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var player: MediaPlayer? = null
    private var visualizer: Visualizer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _waveform = MutableStateFlow<List<Float>>(emptyList())
    val waveform: StateFlow<List<Float>> = _waveform.asStateFlow()

    fun playFile(file: java.io.File) {
        stop() // Stop existing if any
        
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            setOnCompletionListener {
                _isPlaying.value = false
                stop()
            }
            
            setupVisualizer(audioSessionId)
            
            start()
            _isPlaying.value = true
        }
    }

    private fun setupVisualizer(audioSessionId: Int) {
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                        waveform?.let {
                            val normalized = it.map { byte -> (byte.toInt() + 128) / 255f }
                            _waveform.value = normalized
                        }
                    }

                    override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {}
                }, Visualizer.getMaxCaptureRate() / 2, true, false)
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        visualizer?.enabled = false
        visualizer?.release()
        visualizer = null
        
        player?.stop()
        player?.release()
        player = null
        _isPlaying.value = false
        _waveform.value = emptyList()
    }
}
