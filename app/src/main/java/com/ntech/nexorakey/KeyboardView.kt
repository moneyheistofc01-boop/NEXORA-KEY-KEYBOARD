package com.ntech.nexorakey

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

interface KbListener {
    fun commit(text: String)
    fun backspace()
    fun enter()
    fun capsMode(): Int
    fun openSettings()
    fun showPicker()
}

class KV(ctx: Context, val key: Key) : TextView(ctx) {
    var normalBg: Drawable? = null
    var pressedBg: Drawable? = null
    var rep: Runnable? = null
    var longRun: Runnable? = null
    var longFired = false
}

class KeyboardView(ctx: Context, private val listener: KbListener) : LinearLayout(ctx) {

    private val density = ctx.resources.displayMetrics.density
    private val handler = Handler(Looper.getMainLooper())
    private val audio = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val kvs = ArrayList<KV>()
    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

    private var th: NexTheme = THEMES[0]
    private var lang = Prefs.lang(ctx).coerceIn(0, 2)
    private var symbols = false
    private var emoji = false
    private var shift = 0
    private var layer = 0
    private var symPage = 0
    private var arShift = false
    private var enterLabel = "\u23CE"
    private var savedLang = -1
    private var lastShiftTap = 0L

    init {
        orientation = VERTICAL
        layoutDirection = LAYOUT_DIRECTION_LTR
        rebuild()
    }

    private fun dp(v: Int): Int = (v * density + 0.5f).toInt()
    private fun dpf(v: Float): Float = v * density
    private fun keyHeight(): Int = (dp(48) * Prefs.scale(context) / 100f).toInt()

    // ---------- public API used by the service ----------

    fun onNewInput(enter: String, forceEnglish: Boolean, startSymbols: Boolean) {
        enterLabel = enter
        if (forceEnglish) {
            if (savedLang < 0) savedLang = lang
            lang = 0
        } else if (savedLang >= 0) {
            lang = savedLang
            savedLang = -1
        }
        symbols = startSymbols
        emoji = false
        layer = 0
        symPage = 0
        arShift = false
        shift = 0
        rebuild()
        autoShift()
    }

    fun autoShift() {
        if (lang != 0 || symbols || emoji || shift == 2) return
        val want = if (listener.capsMode() != 0) 1 else 0
        if (want != shift) {
            shift = want
            updateShiftLook()
        }
    }

    // ---------- building ----------

    fun rebuild() {
        th = THEMES[Prefs.theme(context).coerceIn(0, THEMES.size - 1)]
        removeAllViews()
        kvs.clear()
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#0D0D16"), Color.parseColor("#040406"))
        )
        setPadding(dp(3), 0, dp(3), dp(10))

        val line = View(context)
        line.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(th.a, th.b)
        )
        addView(line, LinearLayout.LayoutParams(MATCH, dp(2)))
        addView(buildToolbar(), LinearLayout.LayoutParams(MATCH, dp(40)))
        if (emoji) addView(buildEmoji()) else addView(buildRows())
        updateShiftLook()
    }

    private fun postRebuild() {
        post {
            rebuild()
            autoShift()
        }
    }

    private fun buildToolbar(): View {
        val bar = LinearLayout(context)
        bar.orientation = HORIZONTAL
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.setPadding(dp(8), 0, dp(2), 0)

        val brand = TextView(context)
        brand.text = "NEXORA KEY"
        brand.textSize = 11f
        brand.typeface = Typeface.DEFAULT_BOLD
        brand.letterSpacing = 0.18f
        brand.setTextColor(th.b)
        bar.addView(brand, LinearLayout.LayoutParams(0, WRAP, 1f))

        val names = listOf("EN", "\u0DC3\u0DD2\u0D82", "\u0639\u0631")
        names.forEachIndexed { i, n ->
            val c = TextView(context)
            c.text = n
            c.textSize = 13f
            c.gravity = Gravity.CENTER
            c.setTextColor(Color.WHITE)
            c.setPadding(dp(12), 0, dp(12), 0)
            c.background = if (i == lang)
                glass(withAlpha(80, th.b), withAlpha(30, th.b), th.b, dpf(14f), 2)
            else
                glass(Color.TRANSPARENT, Color.TRANSPARENT, Color.argb(34, 255, 255, 255), dpf(14f), 2)
            c.setOnClickListener {
                lang = i
                Prefs.setLang(context, i)
                symbols = false
                emoji = false
                layer = 0
                arShift = false
                savedLang = -1
                postRebuild()
            }
            val lp = LinearLayout.LayoutParams(WRAP, dp(28))
            lp.marginStart = dp(5)
            bar.addView(c, lp)
        }

        val gear = TextView(context)
        gear.text = "\u2699"
        gear.textSize = 19f
        gear.gravity = Gravity.CENTER
        gear.setTextColor(Color.argb(200, 255, 255, 255))
        gear.setPadding(dp(12), 0, dp(12), 0)
        gear.setOnClickListener { listener.openSettings() }
        bar.addView(gear, LinearLayout.LayoutParams(WRAP, MATCH))
        return bar
    }

    private fun buildRows(): View {
        val box = LinearLayout(context)
        box.orientation = VERTICAL
        val kh = keyHeight()
        val nextLayer = listOf("\u0D9A", "\u0D9B", "\u0D85")[(layer + 1) % 3]
        val rows = Layouts.build(
            lang, symbols, layer, arShift, symPage,
            Prefs.numberRow(context), enterLabel, nextLayer
        )
        for (r in rows) {
            val row = LinearLayout(context)
            row.orientation = HORIZONTAL
            row.gravity = Gravity.CENTER
            for (k in r) {
                if (k.act == Act.PAD) {
                    row.addView(View(context), LinearLayout.LayoutParams(0, kh, k.weight))
                    continue
                }
                val lp = LinearLayout.LayoutParams(0, kh, k.weight)
                lp.setMargins(dp(2), dp(3), dp(2), dp(3))
                row.addView(makeKey(k), lp)
            }
            box.addView(row, LinearLayout.LayoutParams(MATCH, kh + dp(6)))
        }
        return box
    }

    private fun buildEmoji(): View {
        val box = LinearLayout(context)
        box.orientation = VERTICAL
        val kh = keyHeight()
        val unit = kh + dp(6)
        val visibleRows = if (Prefs.numberRow(context)) 4 else 3

        val sv = ScrollView(context)
        sv.isVerticalScrollBarEnabled = false
        sv.overScrollMode = View.OVER_SCROLL_NEVER
        val col = LinearLayout(context)
        col.orientation = VERTICAL
        val list = EMOJIS.split(" ").filter { it.isNotEmpty() }
        val perRow = 8
        var i = 0
        while (i < list.size) {
            val row = LinearLayout(context)
            row.orientation = HORIZONTAL
            for (j in 0 until perRow) {
                val tv = TextView(context)
                tv.gravity = Gravity.CENTER
                tv.textSize = 24f
                if (i + j < list.size) {
                    val e = list[i + j]
                    tv.text = e
                    tv.setOnClickListener {
                        feedback(tv)
                        listener.commit(e)
                    }
                }
                row.addView(tv, LinearLayout.LayoutParams(0, unit, 1f))
            }
            col.addView(row, LinearLayout.LayoutParams(MATCH, unit))
            i += perRow
        }
        sv.addView(col, ViewGroup.LayoutParams(MATCH, WRAP))
        box.addView(sv, LinearLayout.LayoutParams(MATCH, unit * visibleRows))

        val bar = LinearLayout(context)
        bar.orientation = HORIZONTAL
        bar.gravity = Gravity.CENTER
        val keys = listOf(
            Key("ABC", act = Act.ABC, weight = 1.5f),
            Key("", " ", Act.SPACE, 5f),
            Key("\u232B", act = Act.BACK, weight = 1.5f)
        )
        for (k in keys) {
            val lp = LinearLayout.LayoutParams(0, kh, k.weight)
            lp.setMargins(dp(2), dp(3), dp(2), dp(3))
            bar.addView(makeKey(k), lp)
        }
        box.addView(bar, LinearLayout.LayoutParams(MATCH, kh + dp(6)))
        return box
    }

    private fun fnBg(r: Float): Drawable =
        glass(Color.argb(30, 255, 255, 255), Color.argb(8, 255, 255, 255), Color.argb(34, 255, 255, 255), r, 2)

    private fun labelFor(k: Key): String {
        if (lang == 0 && !symbols && k.act == Act.CHAR && shift > 0 &&
            k.label.length == 1 && k.label[0].isLetter()
        ) return k.label.uppercase(Locale.ROOT)
        return k.label
    }

    private fun makeKey(k: Key): KV {
        val kv = KV(context, k)
        kv.gravity = Gravity.CENTER
        kv.setTextColor(Color.WHITE)
        kv.includeFontPadding = false
        kv.isClickable = true
        kv.setSingleLine(true)
        val fn = k.act != Act.CHAR && k.act != Act.SPACE
        kv.textSize = when {
            k.label.startsWith("\u25CC") -> 20f
            fn || k.label.length > 1 -> 15f
            else -> 21f
        }
        kv.text = labelFor(k)
        val r = dpf(11f)
        kv.normalBg = when {
            k.act == Act.ENTER -> glass(withAlpha(235, th.a), withAlpha(130, th.a), th.a, r, 2)
            fn -> fnBg(r)
            else -> glass(Color.argb(54, 255, 255, 255), Color.argb(16, 255, 255, 255), Color.argb(46, 255, 255, 255), r, 2)
        }
        kv.pressedBg = glass(withAlpha(190, th.b), withAlpha(120, th.a), th.b, r, 2)
        kv.background = kv.normalBg
        if (k.act == Act.SPACE) {
            kv.textSize = 13f
            kv.setTextColor(Color.argb(150, 255, 255, 255))
        }
        attach(kv)
        kvs.add(kv)
        return kv
    }

    private fun updateShiftLook() {
        val r = dpf(11f)
        for (kv in kvs) {
            when (kv.key.act) {
                Act.CHAR -> if (lang == 0 && !symbols) kv.text = labelFor(kv.key)
                Act.SHIFT -> {
                    kv.text = when (shift) {
                        0 -> "\u21E7"
                        1 -> "\u2B06"
                        else -> "\u21EA"
                    }
                    kv.normalBg = when (shift) {
                        0 -> fnBg(r)
                        1 -> glass(withAlpha(120, th.b), withAlpha(50, th.b), th.b, r, 2)
                        else -> glass(withAlpha(235, th.b), withAlpha(150, th.b), th.b, r, 2)
                    }
                    kv.background = kv.normalBg
                }
                else -> {}
            }
        }
    }

    // ---------- touch handling ----------

    @Suppress("ClickableViewAccessibility")
    private fun attach(kv: KV) {
        kv.setOnTouchListener { v, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    kv.background = kv.pressedBg
                    feedback(kv)
                    onDown(kv)
                }
                MotionEvent.ACTION_UP -> {
                    kv.background = kv.normalBg
                    stopTimers(kv)
                    val inside = ev.x >= -dp(8) && ev.x <= v.width + dp(8) &&
                        ev.y >= -dp(12) && ev.y <= v.height + dp(12)
                    if (inside) onUp(kv)
                }
                MotionEvent.ACTION_CANCEL -> {
                    kv.background = kv.normalBg
                    stopTimers(kv)
                }
            }
            true
        }
    }

    private fun stopTimers(kv: KV) {
        kv.rep?.let { handler.removeCallbacks(it) }
        kv.longRun?.let { handler.removeCallbacks(it) }
        kv.rep = null
        kv.longRun = null
    }

    @Suppress("DEPRECATION")
    private fun feedback(v: View) {
        if (Prefs.haptic(context)) {
            v.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
        if (Prefs.sound(context)) {
            audio.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1f)
        }
    }

    private fun onDown(kv: KV) {
        when (kv.key.act) {
            Act.BACK -> {
                listener.backspace()
                val r = object : Runnable {
                    override fun run() {
                        listener.backspace()
                        handler.postDelayed(this, 45)
                    }
                }
                kv.rep = r
                handler.postDelayed(r, 380)
            }
            Act.LANG -> {
                kv.longFired = false
                val r = Runnable {
                    kv.longFired = true
                    listener.showPicker()
                }
                kv.longRun = r
                handler.postDelayed(r, 450)
            }
            else -> {}
        }
    }

    private fun onUp(kv: KV) {
        val k = kv.key
        when (k.act) {
            Act.CHAR -> commitKey(k)
            Act.SPACE -> listener.commit(" ")
            Act.ENTER -> listener.enter()
            Act.SHIFT -> {
                val now = SystemClock.uptimeMillis()
                shift = when (shift) {
                    2 -> 0
                    1 -> if (now - lastShiftTap < 400) 2 else 0
                    else -> 1
                }
                lastShiftTap = now
                updateShiftLook()
            }
            Act.SYMBOLS -> {
                symbols = true
                symPage = 0
                postRebuild()
            }
            Act.ABC -> {
                symbols = false
                emoji = false
                postRebuild()
            }
            Act.SYMPAGE -> {
                symPage = 1 - symPage
                postRebuild()
            }
            Act.LANG -> {
                if (!kv.longFired) {
                    lang = (lang + 1) % 3
                    Prefs.setLang(context, lang)
                    symbols = false
                    layer = 0
                    arShift = false
                    savedLang = -1
                    postRebuild()
                }
            }
            Act.LAYER -> {
                layer = (layer + 1) % 3
                postRebuild()
            }
            Act.EMOJI -> {
                emoji = true
                postRebuild()
            }
            Act.ARSHIFT -> {
                arShift = !arShift
                postRebuild()
            }
            Act.BACK, Act.PAD -> {}
        }
    }

    private fun commitKey(k: Key) {
        var t = k.value
        if (lang == 0 && !symbols && shift > 0 && t.length == 1) t = t.uppercase(Locale.ROOT)
        listener.commit(t)
        if (shift == 1) {
            shift = 0
            updateShiftLook()
        }
    }
}
