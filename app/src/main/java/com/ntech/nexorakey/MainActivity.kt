package com.ntech.nexorakey

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val bg = Color.rgb(8, 10, 18)
    private val cyan = Color.rgb(0, 200, 255)
    private val red = Color.rgb(255, 40, 90)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(bg)
        }

        val title = TextView(this).apply {
            text = "NEXORA KEY"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cyan)
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "by N TECH OFC\n\nOffline-first multilingual keyboard\nSinhala • English • العربية"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 28)
        }

        val enable = actionButton("1. Enable NEXORA KEY", cyan) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        val choose = actionButton("2. Select keyboard", red) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }
        val note = TextView(this).apply {
            text = "\nPrivacy: this starter app requests no Internet permission and does not save typed text.\n\nThis is an early MVP: language layouts are basic, not a full predictive keyboard."
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }

        page.addView(title)
        page.addView(subtitle)
        page.addView(enable, buttonParams())
        page.addView(choose, buttonParams())
        page.addView(note)
        setContentView(page)
    }

    private fun actionButton(label: String, color: Int, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(32, 36, 52))
            textSize = 15f
            setOnClickListener { action() }
            contentDescription = label
        }

    private fun buttonParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { setMargins(0, 8, 0, 8) }
}
