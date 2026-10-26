package com.example.productivitylauncher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.productivitylauncher.MainActivity

enum class NoiseKind(val label: String) { White("White noise"), Pink("Pink noise"), Brown("Brown noise") }

/** What the Quick actions player shows and controls. The service reads [kind] while it plays. */
object Noise {
    var playing by mutableStateOf(false)
        internal set
    var kind by mutableStateOf(NoiseKind.White)

    fun toggle(context: Context) {
        val i = Intent(context, NoiseService::class.java)
        if (playing) { playing = false; context.startService(i.setAction(NoiseService.ACTION_STOP)) }
        else { playing = true; context.startForegroundService(i.setAction(NoiseService.ACTION_PLAY)) }
    }

    fun step(delta: Int) {
        val all = NoiseKind.entries
        kind = all[(kind.ordinal + delta + all.size) % all.size]
    }
}

/**
 * Makes noise from random numbers and plays it. White noise is plain random samples.
 * Pink softens the highs (like steady rain) and brown softens them even more (like a waterfall).
 */
class NoiseService : Service() {
    @Volatile private var running = false
    private var track: AudioTrack? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopNoise(); return START_NOT_STICKY }
        startForegroundCompat()
        if (!running) startNoise()
        return START_NOT_STICKY
    }

    private fun startForegroundCompat() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Noise", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, NoiseService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Noise is playing")
            .setContentText("Tap to open the launcher")
            .setContentIntent(open)
            .addAction(Notification.Action.Builder(null, "Stop", stop).build())
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTE_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else startForeground(NOTE_ID, n)
    }

    private fun startNoise() {
        running = true
        val rate = 44100
        val min = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
            .setBufferSizeInBytes(maxOf(min, 8192) * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        track = t
        t.play()
        Thread {
            val rnd = java.util.Random()
            val buf = ShortArray(4096)
            var b0 = 0f; var b1 = 0f; var b2 = 0f; var brown = 0f
            while (running) {
                val kind = Noise.kind
                for (i in buf.indices) {
                    val white = rnd.nextFloat() * 2f - 1f
                    val v = when (kind) {
                        NoiseKind.White -> white * 0.30f
                        NoiseKind.Pink -> {
                            b0 = 0.99765f * b0 + white * 0.0990460f
                            b1 = 0.96300f * b1 + white * 0.2965164f
                            b2 = 0.57000f * b2 + white * 1.0526913f
                            (b0 + b1 + b2 + white * 0.1848f) * 0.09f
                        }
                        NoiseKind.Brown -> {
                            brown = (brown + 0.02f * white) / 1.02f
                            brown * 3.0f
                        }
                    }
                    buf[i] = (v.coerceIn(-1f, 1f) * 32767f).toInt().toShort()
                }
                t.write(buf, 0, buf.size)
            }
            runCatching { t.stop(); t.release() }
        }.start()
    }

    private fun stopNoise() {
        running = false
        Noise.playing = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() { running = false; Noise.playing = false; super.onDestroy() }

    companion object {
        const val ACTION_PLAY = "noise.play"
        const val ACTION_STOP = "noise.stop"
        private const val CHANNEL = "noise"
        private const val NOTE_ID = 4242
    }
}
