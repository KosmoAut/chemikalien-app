package at.packliste.app

import com.google.mlkit.vision.text.Text
import kotlin.math.abs

/**
 * Sucht auf dem Etikett den Chemikaliennamen: die Zeile mit der größten Schrift,
 * ohne Hersteller, Gefahrenhinweise, Nummern usw.
 */
object NamePicker {
    private val STOP = Regex(
        "^(gefahr|achtung|danger|warning|cas|eg[- ]?nr|ec[- ]?no|index|lot|charge|batch|art\\b|art\\.|cat\\b|cat\\.|best|ref\\b|" +
            "nettogewicht|netto|inhalt|füllmenge|merck|sigma|aldrich|roth|carl roth|vwr|fisher|thermo|honeywell|alfa|acros|tci\\b|" +
            "chemsolute|applichem|supelco|riedel|apatina|www|http|h\\s?\\d{3}|p\\s?\\d{3}|euh|un\\s?\\d{4}|rid|adr|tel|fax)",
        RegexOption.IGNORE_CASE,
    )
    private val CLEAN = Regex("[^\\p{L}\\p{N}()\\-,.%+/ ]")
    private val SPACES = Regex("\\s+")

    private class L(val t: String, val h: Int, val top: Int)

    fun pick(text: Text): List<String> {
        val lines = mutableListOf<L>()
        for (b in text.textBlocks) for (l in b.lines) {
            val box = l.boundingBox ?: continue
            val t = l.text.replace(CLEAN, " ").replace(SPACES, " ").trim()
            val letters = t.count { it.isLetter() }
            val digits = t.count { it.isDigit() }
            if (letters < 3 || t.length > 60 || digits > letters || STOP.containsMatchIn(t)) continue
            lines.add(L(t, box.height(), box.top))
        }
        lines.sortByDescending { it.h }
        val out = mutableListOf<String>()
        val first = lines.firstOrNull()
        if (first != null) {
            // Name über zwei Zeilen in gleicher Schriftgröße zusammenfügen
            val next = lines.firstOrNull { it !== first && abs(it.h - first.h) < first.h * 0.2 && it.top > first.top && it.top - first.top < first.h * 1.8 }
            if (next != null) out.add(first.t + " " + next.t)
            out.add(first.t)
        }
        for (l in lines) if (l.t !in out) out.add(l.t)
        return out.map(::pretty).distinct().take(6)
    }

    /** "ACETON 99%" -> "Aceton 99%", Formeln wie "NaOH" bleiben unverändert. */
    private fun pretty(s: String): String {
        val letters = s.filter { it.isLetter() }
        if (letters.length < 4 || letters != letters.uppercase()) return s
        return s.split(" ").joinToString(" ") { w ->
            if (w.any { it.isDigit() }) w else w.lowercase().replaceFirstChar { it.titlecase() }
        }
    }
}
