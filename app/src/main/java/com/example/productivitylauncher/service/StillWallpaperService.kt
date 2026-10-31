package com.example.productivitylauncher.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.SurfaceHolder
import androidx.compose.ui.graphics.toArgb
import com.example.productivitylauncher.R
import com.example.productivitylauncher.data.clockText
import com.example.productivitylauncher.data.dayKey
import com.example.productivitylauncher.data.homeGreeting
import com.example.productivitylauncher.data.upNextText
import com.example.productivitylauncher.ui.theme.DarkPalette
import com.example.productivitylauncher.ui.theme.LightPalette
import com.example.productivitylauncher.ui.theme.Palette
import com.example.productivitylauncher.ui.theme.ThemeMode
import com.example.productivitylauncher.ui.theme.paletteOf
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * The Still live wallpaper: the Home page's date, clock, greeting and "Up next" drawn on a plain Still background,
 * in the light or dark Still palette. It reads the launcher's own SharedPreferences file on this phone and needs no
 * permission, so the PIN, pattern and fingerprint of the lock screen keep working as usual.
 *
 * Android draws its own lock screen clock over any wallpaper. If that looks doubled, turn off "Clock on wallpaper"
 * in Settings and the wallpaper keeps only the greeting and the next task.
 */
class StillWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = StillEngine()

    private inner class StillEngine : Engine() {
        private val prefs: SharedPreferences = getSharedPreferences("launcher", Context.MODE_PRIVATE)
        private var visible = false
        private var receiverOn = false

        // Fonts are the same bundled files the launcher uses (res/font), so the wallpaper matches Home.
        private val clockFace = font(R.font.bricolage_light)
        private val displayFace = font(R.font.bricolage_regular)
        private val displayMedium = font(R.font.bricolage_medium)
        private val monoMedium = font(R.font.geist_mono_medium)
        private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        private val shape = Paint(Paint.ANTI_ALIAS_FLAG)

        private fun font(id: Int): Typeface = runCatching { resources.getFont(id) }.getOrDefault(Typeface.DEFAULT)

        private val tick = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = draw()
        }
        // SharedPreferences keeps listeners weakly, so this stays a field.
        private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(false)
            prefs.registerOnSharedPreferenceChangeListener(prefListener)
        }

        override fun onDestroy() {
            prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
            stopTicking()
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) { startTicking(); draw() } else stopTicking()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            draw()
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            super.onSurfaceRedrawNeeded(holder)
            draw()
        }

        /** The minute tick (and time or zone changes) are system broadcasts, so no permission is needed. */
        private fun startTicking() {
            if (receiverOn) return
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(tick, filter, Context.RECEIVER_NOT_EXPORTED)
            else registerReceiver(tick, filter)
            receiverOn = true
        }

        private fun stopTicking() {
            if (!receiverOn) return
            runCatching { unregisterReceiver(tick) }
            receiverOn = false
        }

        private fun palette(): Palette {
            val mode = runCatching { ThemeMode.valueOf(prefs.getString("theme", "Auto") ?: "Auto") }.getOrDefault(ThemeMode.Auto)
            return paletteOf(mode) ?: run {
                val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                if (night) DarkPalette else LightPalette
            }
        }

        /** The first task not done yet and whether there are any tasks at all, read the same way LauncherState stores them. */
        private fun tasks(): Pair<String?, Boolean> {
            val frog = prefs.getString("frog", "") ?: ""
            val frogDone = prefs.getString("frog_done_day", "") == dayKey()
            val list = mutableListOf<Pair<String, Boolean>>()
            if (frog.isNotBlank()) list += frog to frogDone
            runCatching {
                val arr = JSONArray(prefs.getString("tasks", "[]") ?: "[]")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list += o.optString("text") to o.optBoolean("done", false)
                }
            }
            return list.firstOrNull { !it.second }?.first to list.isNotEmpty()
        }

        private fun draw() {
            if (!visible) return
            val holder = surfaceHolder
            val canvas = runCatching { holder.lockCanvas() }.getOrNull() ?: return
            try {
                paintStill(canvas)
            } finally {
                runCatching { holder.unlockCanvasAndPost(canvas) }
            }
        }

        private fun paintStill(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            // 1 unit = 1/390 of the width, the same scale the launcher uses (MainActivity), so proportions equal Home.
            val u = w / 390f
            val p = palette()
            val ink = p.text.toArgb()
            val muted = p.muted.toArgb()
            val line = p.line.toArgb()
            canvas.drawColor(p.phone.toArgb())

            val now = Date()
            val cal = Calendar.getInstance().apply { time = now }
            val hour24 = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val use24 = DateFormat.is24HourFormat(this@StillWallpaperService)
            val name = (prefs.getString("user_name", "") ?: "").trim()
            val wake = prefs.getInt("wake_min", 7 * 60)
            val bed = prefs.getInt("bed_min", 23 * 60)
            val greeting = homeGreeting(hour24 * 60 + minute, wake, bed)
            val (next, hasTasks) = tasks()
            val showClock = prefs.getBoolean("wallpaper_clock", true)
            val gutter = 28f * u

            var y: Float
            if (showClock) {
                // Date (mono, 13, uppercase), as on Home.
                val dateLine = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(now).uppercase()
                text(canvas, dateLine, gutter, 85.5f * u, 13f * u, monoMedium, muted, em = 0.092f)
                // The clock: Bricolage Light 112, each digit centred in a fixed cell like the Home odometer.
                drawClock(canvas, clockText(hour24, minute, use24), gutter, 238f * u, u, ink, p.dot.toArgb())
                y = 287f * u
            } else {
                // Without the clock the block sits in the lower part of the screen, clear of the system lock screen clock.
                y = h * 0.62f
            }

            // Greeting: "Good morning, <name>." with the name in ink, medium, underlined.
            if (name.isBlank()) {
                text(canvas, greeting, gutter, y, 22f * u, displayFace, muted, em = -0.02f)
            } else {
                val lead = greeting.removeSuffix(".") + ", "
                var x = gutter
                x += text(canvas, lead, x, y, 22f * u, displayFace, muted, em = -0.02f)
                val room = w - gutter - x - 8f * u
                val shown = ellipsize(name, displayMedium, 22f * u, -0.02f, room)
                val nameWidth = text(canvas, shown, x, y, 22f * u, displayMedium, ink, em = -0.02f)
                shape.color = ink; shape.style = Paint.Style.STROKE; shape.strokeWidth = 1.5f * u
                canvas.drawLine(x, y + 3f * u, x + nameWidth, y + 3f * u, shape)
                text(canvas, ".", x + nameWidth, y, 22f * u, displayFace, muted, em = -0.02f)
            }

            // Hairline, then "Up next" with an empty check circle, as on Home.
            y += 25.5f * u
            shape.color = line; shape.style = Paint.Style.STROKE; shape.strokeWidth = 1f * u
            canvas.drawLine(gutter, y, w - gutter, y, shape)
            y += 25f * u
            val circle = 30f * u
            shape.color = ink; shape.strokeWidth = 1.5f * u
            canvas.drawCircle(gutter + circle / 2f, y + circle / 2f, circle / 2f - 0.75f * u, shape)
            val textX = gutter + circle + 16f * u
            text(canvas, "UP NEXT", textX, y + 6.5f * u, 11f * u, monoMedium, muted, em = 1f / 11f)
            val upNext = ellipsize(upNextText(next, hasTasks), displayMedium, 19f * u, -0.02f, w - gutter - textX)
            text(canvas, upNext, textX, y + 30f * u, 19f * u, displayMedium, if (next == null) muted else ink, em = -0.02f)
        }

        /** "HH:MM" as four digit cells (57.9 units) and a colon cell (36 units), the proportions of the Home clock. */
        private fun drawClock(canvas: Canvas, hhmm: String, x0: Float, baseline: Float, u: Float, ink: Int, dot: Int) {
            val cell = 57.9f * u
            val colon = 36f * u
            val size = 112f * u
            var x = x0
            for (ch in hhmm) {
                if (ch == ':') {
                    shape.style = Paint.Style.FILL; shape.color = dot
                    canvas.drawCircle(x + colon / 2f, baseline - 57f * u, 6.3f * u, shape)
                    canvas.drawCircle(x + colon / 2f, baseline - 21f * u, 6.3f * u, shape)
                    x += colon
                } else {
                    paint.typeface = clockFace; paint.textSize = size; paint.letterSpacing = 0f
                    val s = ch.toString()
                    text(canvas, s, x + (cell - paint.measureText(s)) / 2f, baseline, size, clockFace, ink)
                    x += cell
                }
            }
        }

        /** Draws one run of text at a baseline and returns its width. [em] is letter spacing in em. */
        private fun text(canvas: Canvas, s: String, x: Float, baseline: Float, size: Float, face: Typeface, color: Int, em: Float = 0f): Float {
            paint.typeface = face
            paint.textSize = size
            paint.letterSpacing = em
            paint.color = color
            canvas.drawText(s, x, baseline, paint)
            return paint.measureText(s)
        }

        private fun ellipsize(s: String, face: Typeface, size: Float, em: Float, room: Float): String {
            paint.typeface = face
            paint.textSize = size
            paint.letterSpacing = em
            return TextUtils.ellipsize(s, paint, room.coerceAtLeast(0f), TextUtils.TruncateAt.END).toString()
        }
    }
}
