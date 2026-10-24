package com.ntech.nexorakey

enum class Act { CHAR, BACK, SHIFT, SYMBOLS, ABC, SYMPAGE, LANG, SPACE, ENTER, LAYER, EMOJI, ARSHIFT, PAD }

data class Key(
    val label: String,
    val value: String = label,
    val act: Act = Act.CHAR,
    val weight: Float = 1f
)

object Layouts {
    private const val DC = "\u25CC"

    private fun cp(s: String): List<Key> = s.map { Key(it.toString()) }
    private fun words(s: String): List<Key> = s.split(" ").filter { it.isNotEmpty() }.map { Key(it) }
    private fun marks(s: String): List<Key> = s.split(" ").filter { it.isNotEmpty() }.map { Key(DC + it, it) }
    private fun pad() = Key("", "", Act.PAD, 0.5f)

    private fun bottom(lang: Int, symbols: Boolean, enter: String, nextLayer: String): List<Key> {
        val list = mutableListOf<Key>()
        if (symbols) list.add(Key("ABC", act = Act.ABC, weight = 1.4f))
        else list.add(Key("?123", act = Act.SYMBOLS, weight = 1.4f))
        if (!symbols && lang == 1) list.add(Key(nextLayer, act = Act.LAYER, weight = 1f))
        list.add(Key("\uD83C\uDF10", act = Act.LANG))
        list.add(Key(if (lang == 2) "\u060C" else ","))
        val name = if (symbols) "" else when (lang) {
            1 -> "\u0DC3\u0DD2\u0D82\u0DC4\u0DBD"
            2 -> "\u0627\u0644\u0639\u0631\u0628\u064A\u0629"
            else -> "English"
        }
        list.add(Key(name, " ", Act.SPACE, if (lang == 1 && !symbols) 3f else 4f))
        list.add(Key("."))
        list.add(Key("\uD83D\uDE0A", act = Act.EMOJI))
        list.add(Key(enter, act = Act.ENTER, weight = 1.4f))
        return list
    }

    fun build(
        lang: Int, symbols: Boolean, layer: Int, arShift: Boolean, symPage: Int,
        numberRow: Boolean, enter: String, nextLayer: String
    ): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()
        val back = Key("\u232B", act = Act.BACK, weight = 1.5f)

        if (symbols) {
            rows.add(cp("1234567890"))
            if (symPage == 0) {
                rows.add(cp("@#\$%&-+()/"))
                rows.add(listOf(Key("=\\<", act = Act.SYMPAGE, weight = 1.5f)) + cp("*\"':;!?") + back)
            } else {
                rows.add(cp("~`|\u2022\u221A\u03C0\u00F7\u00D7\u00B6\u2206"))
                rows.add(listOf(Key("?123", act = Act.SYMPAGE, weight = 1.5f)) + cp("\u00A3\u00A2\u20AC\u00A5^\u00B0={}") + back)
            }
            rows.add(bottom(lang, true, enter, nextLayer))
            return rows
        }

        when (lang) {
            0 -> {
                if (numberRow) rows.add(cp("1234567890"))
                rows.add(cp("qwertyuiop"))
                rows.add(listOf(pad()) + cp("asdfghjkl") + pad())
                rows.add(listOf(Key("\u21E7", act = Act.SHIFT, weight = 1.5f)) + cp("zxcvbnm") + back)
            }
            1 -> {
                if (numberRow) rows.add(cp("1234567890"))
                val common = "\u0DCF \u0DD0 \u0DD2 \u0DD3 \u0DD4 \u0DD6 \u0DD9 \u0DDA \u0DDC \u0DCA"
                when (layer) {
                    0 -> {
                        rows.add(words("\u0D9A \u0D9C \u0DA0 \u0DA2 \u0DA7 \u0DA9 \u0DAD \u0DAF \u0DB1 \u0DB4"))
                        rows.add(words("\u0DB6 \u0DB8 \u0DBA \u0DBB \u0DBD \u0DC0 \u0DC3 \u0DC4 \u0DC5 \u0DC6"))
                        rows.add(marks(common) + back)
                    }
                    1 -> {
                        rows.add(words("\u0D9B \u0D9D \u0DA1 \u0DA3 \u0DA8 \u0DAA \u0DAE \u0DB0 \u0DB5 \u0DB7"))
                        rows.add(words("\u0DC1 \u0DC2 \u0DAB \u0D9E \u0DA4 \u0DAC \u0DB3 \u0D9F \u0DB9 \u0DA5 \u0DA6"))
                        rows.add(
                            marks(
                                "\u0DD1 \u0DDD \u0DDB \u0DDE \u0DD8 \u0DF2 \u0D82 \u0D83 \u0DCA\u200D\u0DBB \u0DCA\u200D\u0DBA"
                            ) + back
                        )
                    }
                    else -> {
                        rows.add(words("\u0D85 \u0D86 \u0D87 \u0D88 \u0D89 \u0D8A \u0D8B \u0D8C \u0D91 \u0D92"))
                        rows.add(words("\u0D94 \u0D95 \u0D93 \u0D96 \u0D8D \u0D8E \u0D8F \u0D90") + Key("ZWJ", "\u200D"))
                        rows.add(marks(common) + back)
                    }
                }
            }
            else -> {
                if (numberRow) rows.add(cp("1234567890"))
                if (!arShift) {
                    rows.add(cp("\u0636\u0635\u062B\u0642\u0641\u063A\u0639\u0647\u062E\u062D\u062C\u062F"))
                    rows.add(cp("\u0634\u0633\u064A\u0628\u0644\u0627\u062A\u0646\u0645\u0643\u0637\u0630"))
                    rows.add(
                        listOf(Key(DC + "\u064B", "", Act.ARSHIFT, 1.4f)) +
                            cp("\u0626\u0621\u0624\u0631") + Key("\u0644\u0627") +
                            cp("\u0649\u0629\u0648\u0632\u0638") + back
                    )
                } else {
                    rows.add(
                        marks("\u064B \u064C \u064D \u064E \u064F \u0650 \u0651 \u0652") +
                            cp("\u0640\u061F\u061B\u060C")
                    )
                    rows.add(cp("\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669"))
                    rows.add(
                        listOf(Key("\u0636", "", Act.ARSHIFT, 1.4f)) +
                            cp("\u067E\u0686\u0698\u06AF\u06A4\u06A9\u06CC\u00AB\u00BB") + back
                    )
                }
            }
        }
        rows.add(bottom(lang, false, enter, nextLayer))
        return rows
    }
}
