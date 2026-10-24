package com.ntech.nexorakey

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

data class NexTheme(val name: String, val a: Int, val b: Int)

val THEMES: List<NexTheme> = listOf(
    NexTheme("Neon", Color.parseColor("#FF2850"), Color.parseColor("#00D2FF")),
    NexTheme("Cyan", Color.parseColor("#00D2FF"), Color.parseColor("#3D7BFF")),
    NexTheme("Crimson", Color.parseColor("#FF2850"), Color.parseColor("#FF8A1F")),
    NexTheme("Violet", Color.parseColor("#B36BFF"), Color.parseColor("#00D2FF"))
)

fun withAlpha(alpha: Int, c: Int): Int = Color.argb(alpha, Color.red(c), Color.green(c), Color.blue(c))

fun glass(top: Int, bottom: Int, stroke: Int, radius: Float, strokePx: Int = 2): GradientDrawable {
    val d = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, bottom))
    d.cornerRadius = radius
    d.setStroke(strokePx, stroke)
    return d
}
