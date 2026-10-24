package com.ntech.nexorakey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class SetupActivity : Activity() {

    private lateinit var st1: TextView
    private lateinit var st2: TextView
    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    private val mute = Color.parseColor("#8D93A6")

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density + 0.5f).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        val th = THEMES[Prefs.theme(this).coerceIn(0, THEMES.size - 1)]
        val ctx: Context = this

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.parseColor("#050508"))
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(18), dp(26), dp(18), dp(30))
        scroll.addView(root, ViewGroup.LayoutParams(MATCH, WRAP))

        // header
        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        val icon = ImageView(this)
        icon.setImageResource(R.drawable.ic_launcher)
        head.addView(icon, LinearLayout.LayoutParams(dp(60), dp(60)))
        val titles = LinearLayout(this)
        titles.orientation = LinearLayout.VERTICAL
        titles.setPadding(dp(14), 0, 0, 0)
        val t1 = TextView(this)
        t1.text = "NEXORA KEY"
        t1.textSize = 24f
        t1.typeface = Typeface.DEFAULT_BOLD
        t1.letterSpacing = 0.14f
        t1.setTextColor(Color.WHITE)
        val t2 = TextView(this)
        t2.text = "by N TECH OFC"
        t2.textSize = 13f
        t2.setTextColor(th.b)
        titles.addView(t1)
        titles.addView(t2)
        head.addView(titles)
        root.addView(head)

        // step 1
        val c1 = card(root)
        title(c1, "1. Keyboard \u0D91\u0D9A \u0DC3\u0D9A\u0DCA\u200D\u0DBB\u0DD2\u0DBA \u0D9A\u0DBB\u0DB1\u0DCA\u0DB1")
        body(c1, "Settings \u0DC4\u0DD2 NEXORA KEY switch \u0D91\u0D9A On \u0D9A\u0DBB\u0DB1\u0DCA\u0DB1.")
        st1 = TextView(this)
        st1.textSize = 14f
        c1.addView(st1)
        button(c1, "Settings \u0DC0\u0DD2\u0DC0\u0DD8\u0DAD \u0D9A\u0DBB\u0DB1\u0DCA\u0DB1", th.a) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        // step 2
        val c2 = card(root)
        title(c2, "2. NEXORA KEY \u0DAD\u0DDD\u0DBB\u0DB1\u0DCA\u0DB1")
        body(c2, "\u0D94\u0DB6\u0DDA keyboard \u0D91\u0D9A \u0DBD\u0DD9\u0DC3 NEXORA KEY \u0DAD\u0DDD\u0DBB\u0DCF\u0D9C\u0DB1\u0DCA\u0DB1.")
        st2 = TextView(this)
        st2.textSize = 14f
        c2.addView(st2)
        button(c2, "Keyboard \u0DAD\u0DDD\u0DBB\u0DB1\u0DCA\u0DB1", th.b) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        // test field
        val c3 = card(root)
        title(c3, "\u0DB4\u0DBB\u0DD3\u0D9A\u0DCA\u0DC2\u0DCF \u0D9A\u0DBB\u0DB1\u0DCA\u0DB1")
        val et = EditText(this)
        et.hint = "\u0DB8\u0DD9\u0DAD\u0DB1 \u0DBD\u0DD2\u0DBA\u0DBD\u0DCF \u0DB6\u0DBD\u0DB1\u0DCA\u0DB1\u2026"
        et.setHintTextColor(mute)
        et.setTextColor(Color.WHITE)
        et.textSize = 18f
        et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        et.minLines = 3
        et.gravity = Gravity.TOP
        et.background = glass(Color.argb(24, 255, 255, 255), Color.argb(8, 255, 255, 255), Color.argb(40, 255, 255, 255), dp(12).toFloat(), 2)
        et.setPadding(dp(12), dp(10), dp(12), dp(10))
        c3.addView(et, LinearLayout.LayoutParams(MATCH, WRAP))

        // settings
        val c4 = card(root)
        title(c4, "Settings")
        toggle(c4, "Haptic (\u0D9A\u0DB8\u0DCA\u0DB4\u0DB1\u0DBA)", Prefs.haptic(ctx)) { Prefs.setHaptic(ctx, it) }
        toggle(c4, "Key sound (\u0DC1\u0DB6\u0DCA\u0DAF\u0DBA)", Prefs.sound(ctx)) { Prefs.setSound(ctx, it) }
        toggle(c4, "Number row (\u0D89\u0DBD\u0D9A\u0DCA\u0D9A\u0DB8\u0DCA \u0DB4\u0DDA\u0DC5\u0DD2\u0DBA)", Prefs.numberRow(ctx)) { Prefs.setNumberRow(ctx, it) }
        body(c4, "Key height (\u0DBA\u0DAD\u0DD4\u0DBB\u0DD4 \u0D8B\u0DC3)")
        val seek = SeekBar(this)
        seek.max = 50
        seek.progress = (Prefs.scale(ctx) - 80).coerceIn(0, 50)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                Prefs.setScale(ctx, 80 + p)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        c4.addView(seek, LinearLayout.LayoutParams(MATCH, WRAP))
        body(c4, "Theme (\u0DAD\u0DDA\u0DB8\u0DCF\u0DC0)")
        val chips = LinearLayout(this)
        chips.orientation = LinearLayout.HORIZONTAL
        THEMES.forEachIndexed { i, t ->
            val chip = TextView(this)
            chip.text = t.name
            chip.setTextColor(Color.WHITE)
            chip.textSize = 13f
            chip.gravity = Gravity.CENTER
            chip.setPadding(dp(12), dp(7), dp(12), dp(7))
            val sel = i == Prefs.theme(ctx)
            chip.background = glass(
                withAlpha(if (sel) 110 else 20, t.a), withAlpha(if (sel) 60 else 6, t.b),
                if (sel) t.b else Color.argb(40, 255, 255, 255), dp(16).toFloat(), 2
            )
            chip.setOnClickListener {
                Prefs.setTheme(ctx, i)
                recreate()
            }
            val lp = LinearLayout.LayoutParams(WRAP, WRAP)
            lp.marginEnd = dp(6)
            chips.addView(chip, lp)
        }
        c4.addView(chips)

        // privacy
        val c5 = card(root)
        title(c5, "Privacy")
        body(
            c5,
            "\u0D94\u0DB6 \u0DBD\u0DD2\u0DBA\u0DB1 \u0DAF\u0DDA \u0D9C\u0DB6\u0DA9\u0DCF \u0D9A\u0DBB\u0DB1\u0DCA\u0DB1\u0DDA \u0DC4\u0DDD \u0DBA\u0DC0\u0DB1\u0DCA\u0DB1\u0DDA \u0DB1\u0DD0\u0DC4\u0DD0. " +
                "\u0DB8\u0DD9\u0DB8 app \u0D91\u0D9A\u0DA7 Internet \u0D85\u0DC0\u0DC3\u0DBB\u0DBA\u0D9A\u0DCA \u0DB1\u0DD0\u0DAD."
        )

        setContentView(scroll)
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) refresh()
    }

    private fun refresh() {
        if (!::st1.isInitialized) return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any { it.packageName == packageName }
        val cur = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        val selected = cur.startsWith("$packageName/")
        st1.text = if (enabled) "\u2713 \u0DC3\u0D9A\u0DCA\u200D\u0DBB\u0DD2\u0DBA\u0DBA\u0DD2" else "\u2717 \u0DAD\u0DC0\u0DB8 \u0DC3\u0D9A\u0DCA\u200D\u0DBB\u0DD2\u0DBA \u0DB1\u0DD0\u0DAD"
        st1.setTextColor(if (enabled) Color.parseColor("#4CE08A") else Color.parseColor("#FF6B81"))
        st2.text = if (selected) "\u2713 \u0DAD\u0DDD\u0DBB\u0DCF \u0D87\u0DAD" else "\u2717 \u0DAD\u0DC0\u0DB8 \u0DAD\u0DDD\u0DBB\u0DCF \u0DB1\u0DD0\u0DAD"
        st2.setTextColor(if (selected) Color.parseColor("#4CE08A") else Color.parseColor("#FF6B81"))
    }

    // ---------- small UI helpers ----------

    private fun card(root: LinearLayout): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.setPadding(dp(16), dp(14), dp(16), dp(16))
        c.background = glass(Color.argb(40, 255, 255, 255), Color.argb(12, 255, 255, 255), Color.argb(48, 255, 255, 255), dp(20).toFloat(), 2)
        val lp = LinearLayout.LayoutParams(MATCH, WRAP)
        lp.topMargin = dp(14)
        root.addView(c, lp)
        return c
    }

    private fun title(c: LinearLayout, s: String) {
        val t = TextView(this)
        t.text = s
        t.textSize = 17f
        t.typeface = Typeface.DEFAULT_BOLD
        t.setTextColor(Color.WHITE)
        c.addView(t)
    }

    private fun body(c: LinearLayout, s: String) {
        val t = TextView(this)
        t.text = s
        t.textSize = 14f
        t.setTextColor(mute)
        t.setPadding(0, dp(6), 0, dp(6))
        c.addView(t)
    }

    private fun button(c: LinearLayout, s: String, color: Int, onClick: () -> Unit) {
        val b = TextView(this)
        b.text = s
        b.textSize = 15f
        b.gravity = Gravity.CENTER
        b.typeface = Typeface.DEFAULT_BOLD
        b.setTextColor(Color.WHITE)
        b.background = glass(withAlpha(220, color), withAlpha(110, color), color, dp(14).toFloat(), 2)
        b.setOnClickListener { onClick() }
        val lp = LinearLayout.LayoutParams(MATCH, dp(48))
        lp.topMargin = dp(10)
        c.addView(b, lp)
    }

    private fun toggle(c: LinearLayout, label: String, initial: Boolean, onChange: (Boolean) -> Unit) {
        val s = Switch(this)
        s.text = label
        s.textSize = 15f
        s.setTextColor(Color.WHITE)
        s.isChecked = initial
        s.setPadding(0, dp(8), 0, dp(8))
        s.setOnCheckedChangeListener { _, checked -> onChange(checked) }
        c.addView(s, LinearLayout.LayoutParams(MATCH, WRAP))
    }
}
