package com.ntech.nexorakey

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class NexoraKeyboardService : InputMethodService() {
    private val languages = listOf("EN", "සිං", "عربي")
    private var languageIndex = 0
    private var shifted = false
    private val background = Color.rgb(9, 11, 20)
    private val keyBackground = Color.rgb(28, 32, 48)
    private val cyan = Color.rgb(0, 205, 255)
    private val red = Color.rgb(255, 45, 90)
    private var keyboardRoot: LinearLayout? = null

    override fun onCreateInputView(): View {
        return buildKeyboard().also { keyboardRoot = it }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Password fields are not logged, cached, or inspected by this keyboard.
        // This MVP does not read text around the cursor.
        keyboardRoot?.let { refreshKeyboard() }
    }

    private fun buildKeyboard(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(5, 7, 5, 7)
            setBackgroundColor(background)
        }
        keyboardRoot = root
        refreshKeyboard()
        return root
    }

    private fun refreshKeyboard() {
        val root = keyboardRoot ?: return
        root.removeAllViews()

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        toolbar.addView(label("NEXORA", cyan, 14f), LinearLayout.LayoutParams(0, 42, 1f))
        toolbar.addView(keyButton(languages[languageIndex], red) { cycleLanguage() },
            LinearLayout.LayoutParams(84, 42))
        toolbar.addView(keyButton("⌫", red) { deleteOne() },
            LinearLayout.LayoutParams(58, 42))
        root.addView(toolbar)

        rowsForCurrentLanguage().forEachIndexed { rowIndex, row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { symbol ->
                line.addView(keyButton(if (shifted && languageIndex == 0) symbol.uppercase() else symbol, keyBackground) {
                    commit(symbolFor(symbol))
                }, LinearLayout.LayoutParams(0, 45, 1f).apply {
                    setMargins(2, 3, 2, 3)
                })
            }
            root.addView(line)
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        bottom.addView(keyButton("⇧", red) {
            shifted = !shifted
            refreshKeyboard()
        }, LinearLayout.LayoutParams(52, 46))
        bottom.addView(keyButton("?123", keyBackground) {
            commit("123")
        }, LinearLayout.LayoutParams(62, 46))
        bottom.addView(keyButton("🌐", keyBackground) { cycleLanguage() },
            LinearLayout.LayoutParams(48, 46))
        bottom.addView(keyButton("English / සිංහල / العربية", keyBackground) {
            commit(" ")
        }, LinearLayout.LayoutParams(0, 46, 1f))
        bottom.addView(keyButton("↵", cyan) { enterKey() },
            LinearLayout.LayoutParams(52, 46))
        root.addView(bottom)
    }

    private fun rowsForCurrentLanguage(): List<List<String>> = when (languageIndex) {
        0 -> listOf(
            "qwertyuiop".map { it.toString() },
            "asdfghjkl".map { it.toString() },
            "zxcvbnm".map { it.toString() }
        )
        1 -> listOf(
            "අආඇඈඉඊඋඌඑඒ".map { it.toString() },
            "කගචජටඩතදනප".map { it.toString() },
            "බමයරලවසහළ".map { it.toString() }
        )
        else -> listOf(
            "ضصثقفغعهخح".map { it.toString() },
            "شسيبلاتنم".map { it.toString() },
            "ئءؤرىةوزظ".map { it.toString() }
        )
    }

    private fun symbolFor(symbol: String): String {
        if (languageIndex == 0 && shifted) {
            shifted = false
            refreshKeyboard()
            return symbol.uppercase()
        }
        return symbol
    }

    private fun commit(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun deleteOne() {
        val ic: InputConnection = currentInputConnection ?: return
        ic.deleteSurroundingText(1, 0)
    }

    private fun enterKey() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        when (action) {
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_DONE -> ic.performEditorAction(action)
            else -> ic.commitText("\n", 1)
        }
    }

    private fun cycleLanguage() {
        languageIndex = (languageIndex + 1) % languages.size
        refreshKeyboard()
    }

    private fun label(text: String, color: Int, size: Float): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
        }

    private fun keyButton(text: String, accent: Int, action: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            textSize = if (text.length > 3) 11f else 17f
            setTextColor(if (accent == cyan) Color.BLACK else Color.WHITE)
            setBackgroundColor(accent)
            isAllCaps = false
            setPadding(0, 0, 0, 0)
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { action() }
        }
}
