package it.fast4x.rimusic.extensions.exovisualizer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

/**
 * The visualizer is a view which listens to the FFT changes and forwards it to the band view.
 */
@UnstableApi
class ExoVisualizer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Player.Listener, FFTAudioProcessor.FFTListener {

    var processor: FFTAudioProcessor? = null

    private var currentWaveform: FloatArray? = null

    private val bandView = FFTBandView(context, attrs)

    init {
        addView(bandView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun updateProcessorListenerState(enable: Boolean) {
        if (enable) {
            processor?.listener = this
        } else {
            processor?.listener = null
            currentWaveform = null
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateProcessorListenerState(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateProcessorListenerState(false)
    }

    override fun onFFTReady(sampleRateHz: Int, channelCount: Int, fft: FloatArray) {
        currentWaveform = fft
        bandView.onFFT(fft)
    }

}