package com.ntech.nexorakey

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import java.text.BreakIterator

class NexoraKeyService : InputMethodService(), KbListener {

    private var kb: KeyboardView? = null

    override fun onCreateInputView(): View {
        val v = KeyboardView(this, this)
        kb = v
        return v
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        val cls = info.inputType and InputType.TYPE_MASK_CLASS
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        val isText = cls == InputType.TYPE_CLASS_TEXT
        val secureText = isText && (
            variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_URI
            )
        val numeric = cls == InputType.TYPE_CLASS_NUMBER ||
            cls == InputType.TYPE_CLASS_PHONE ||
            cls == InputType.TYPE_CLASS_DATETIME
        kb?.onNewInput(enterLabelFor(info), secureText, numeric)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        kb?.autoShift()
    }

    private fun enterLabelFor(info: EditorInfo): String {
        val multi = (info.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        val noAct = (info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (multi || noAct) return "\u23CE"
        return when (info.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_SEARCH -> "\uD83D\uDD0D"
            EditorInfo.IME_ACTION_SEND -> "\u27A4"
            EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_PREVIOUS -> "\u2192"
            EditorInfo.IME_ACTION_DONE -> "\u2713"
            EditorInfo.IME_ACTION_GO -> "\u279C"
            else -> "\u23CE"
        }
    }

    // ---------- KbListener ----------

    override fun commit(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    override fun backspace() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }
        val before = ic.getTextBeforeCursor(8, 0)
        if (before.isNullOrEmpty()) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            return
        }
        ic.deleteSurroundingText(deleteLength(before.toString()), 0)
    }

    private fun deleteLength(s: String): Int {
        val n = s.length
        // Sinhala rakaransaya / yansaya: HAL + ZWJ + RA|YA is removed as one unit
        if (n >= 3 && s[n - 2] == '\u200D' && s[n - 3] == '\u0DCA' &&
            (s[n - 1] == '\u0DBB' || s[n - 1] == '\u0DBA')
        ) return 3
        // Emoji and other surrogate pairs: remove a whole grapheme
        if (n >= 2 && Character.isLowSurrogate(s[n - 1])) {
            val bi = BreakIterator.getCharacterInstance()
            bi.setText(s)
            val end = bi.last()
            val start = bi.previous()
            return if (start == BreakIterator.DONE || end - start < 2) 2 else end - start
        }
        return 1
    }

    override fun enter() {
        val ic = currentInputConnection ?: return
        val ei = currentInputEditorInfo
        if (ei != null) {
            val action = ei.imeOptions and EditorInfo.IME_MASK_ACTION
            val noEnter = (ei.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
            val multi = (ei.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
            if (!noEnter && !multi &&
                action != EditorInfo.IME_ACTION_NONE &&
                action != EditorInfo.IME_ACTION_UNSPECIFIED
            ) {
                ic.performEditorAction(action)
                return
            }
        }
        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }

    override fun capsMode(): Int {
        val ic = currentInputConnection ?: return 0
        val ei = currentInputEditorInfo ?: return 0
        return ic.getCursorCapsMode(ei.inputType)
    }

    override fun openSettings() {
        val i = Intent(this, SetupActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
    }

    override fun showPicker() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }
}
