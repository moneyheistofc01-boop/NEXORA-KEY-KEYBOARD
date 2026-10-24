package com.ntech.nexorakey

import android.content.Context

/** Only settings are stored here. Typed text is never saved or sent anywhere. */
object Prefs {
    private fun sp(c: Context) =
        c.applicationContext.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)

    fun haptic(c: Context) = sp(c).getBoolean("haptic", true)
    fun setHaptic(c: Context, v: Boolean) = sp(c).edit().putBoolean("haptic", v).apply()

    fun sound(c: Context) = sp(c).getBoolean("sound", false)
    fun setSound(c: Context, v: Boolean) = sp(c).edit().putBoolean("sound", v).apply()

    fun numberRow(c: Context) = sp(c).getBoolean("numrow", true)
    fun setNumberRow(c: Context, v: Boolean) = sp(c).edit().putBoolean("numrow", v).apply()

    /** key height in percent, 80..130 */
    fun scale(c: Context) = sp(c).getInt("scale", 100)
    fun setScale(c: Context, v: Int) = sp(c).edit().putInt("scale", v).apply()

    fun theme(c: Context) = sp(c).getInt("theme", 0)
    fun setTheme(c: Context, v: Int) = sp(c).edit().putInt("theme", v).apply()

    /** 0 = English, 1 = Sinhala, 2 = Arabic */
    fun lang(c: Context) = sp(c).getInt("lang", 0)
    fun setLang(c: Context, v: Int) = sp(c).edit().putInt("lang", v).apply()
}
