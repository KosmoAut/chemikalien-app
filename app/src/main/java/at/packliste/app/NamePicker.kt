package at.packliste.app

import com.google.mlkit.vision.text.Text
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Findet auf dem Etikett den Chemikaliennamen:
 * 1. bekannter Name aus der Chemikalienliste (größte Schrift zuerst),
 * 2. sonst CAS-Nummer aus der Liste,
 * 3. sonst die waagrechte Zeile mit der größten Schrift ohne Hersteller, Gefahrenhinweise, Nummern.
 */
object NamePicker {
    class LineInfo(val text: String, val height: Float, val top: Float, val angleDeg: Float)

    private val STOP_START = Regex(
        "^(gefahr|achtung|danger|warning|peligro|atencion|cas|eg[- ]?nr|ec[- ]?no|index|lot|charge|batch|art\\b|art\\.|cat\\b|cat\\.|" +
            "best|ref\\b|nettogewicht|netto|inhalt|füllmenge|www|http|h\\s?\\d{3}|p\\s?\\d{3}|euh|un\\s?\\d{4}|rid|adr|tel|fax|" +
            "only for|nur für|for laboratory|for analysis|zur analyse|m\\s?=|mw|molar|dichte|density|ph\\b)",
        RegexOption.IGNORE_CASE,
    )
    private val BRAND = Regex(
        "(merck|sigma|aldrich|millipore|roth|vwr|fisher|thermo|honeywell|fluka|riedel|alfa aesar|acros|\\btci\\b|chemsolute|" +
            "applichem|panreac|supelco|scharlau|carlo erba|grüssing|lenovo|dell|samsung|apatina|reagents|itw|avantor|j\\.?t\\.? ?baker|\\bsial\\b|bldpharm|tokyo chemical|tokio chemical|roche|abcr|fluorochem|apollo scientific|santa cruz|cayman|biosynth|carbosynth|serva|lonza|gibco|invitrogen|bio-rad|biorad|emd|calbiochem|sigma-aldrich|thermo scientific|chemcruz|enamine|combi-blocks|strem)",
        RegexOption.IGNORE_CASE,
    )
    private val CLEAN = Regex("[^\\p{L}\\p{N}()\\[\\]{}\\-,.%+/'±′ ]")
    private val SPACES = Regex("\\s+")
    /** Qualitätsangaben hinten abschneiden: "Acetonitrile for UV, IR, HPLC" → "Acetonitrile". */
    private val GRADE = Regex(
        "\\s+(for|für|zur|pro|p\\.?\\s?a\\.?|hplc|acs|uv|reag|ph\\.?\\s?eur|usp|bp|emsure|emplura|rotipuran|rotisolv|lichrosolv|" +
            "chromasolv|puriss|purum|reinst|rein|techn|technisch|extra|\\d+[,.]?\\d*\\s?%|≥|>).*$",
        RegexOption.IGNORE_CASE,
    )

    fun pick(text: Text): List<String> {
        val lines = mutableListOf<LineInfo>()
        for (b in text.textBlocks) for (l in b.lines) {
            val p = l.cornerPoints
            if (p != null && p.size == 4) {
                val angle = Math.toDegrees(atan2((p[1].y - p[0].y).toDouble(), (p[1].x - p[0].x).toDouble())).toFloat()
                val h = hypot((p[3].x - p[0].x).toDouble(), (p[3].y - p[0].y).toDouble()).toFloat()
                lines.add(LineInfo(l.text, h, p[0].y.toFloat(), angle))
            } else {
                val box = l.boundingBox ?: continue
                lines.add(LineInfo(l.text, box.height().toFloat(), box.top.toFloat(), 0f))
            }
        }
        return pickFromLines(lines)
    }

    fun pickFromLines(raw: List<LineInfo>): List<String> {
        val allText = raw.joinToString("\n") { it.text }
        // nur (annähernd) waagrechte Zeilen, größte Schrift zuerst
        val lines = raw
            .filter { abs(it.angleDeg) < 25 }
            .map { LineInfo(it.text.replace(CLEAN, " ").replace(SPACES, " ").trim(), it.height, it.top, it.angleDeg) }
            .filter { it.text.isNotEmpty() }
            .sortedByDescending { it.height }

        val out = mutableListOf<String>()
        // 1. bekannte Namen
        Chemicals.findByName(lines.map { it.text }).forEach { out.add(it.chem.name) }
        // 2. CAS-Nummern
        Chemicals.findByCas(allText).forEach { out.add(it.name) }
        // 3. größte Schrift
        val candidates = lines.filter { l ->
            val t = l.text
            val letters = t.count { it.isLetter() }
            val digits = t.count { it.isDigit() }
            letters >= 3 && t.length <= 60 && digits <= letters && !STOP_START.containsMatchIn(t) && !BRAND.containsMatchIn(t)
        }
        val first = candidates.firstOrNull()
        if (first != null) {
            val next = candidates.firstOrNull {
                it !== first && abs(it.height - first.height) < first.height * 0.2f && it.top > first.top && it.top - first.top < first.height * 1.8f
            }
            if (next != null) out.add(pretty(strip(first.text + " " + next.text)))
            out.add(pretty(strip(first.text)))
        }
        for (c in candidates) out.add(pretty(strip(c.text)))
        // Namen aus der Liste bleiben genau so geschrieben (NADP, MOPS, HEPES …)
        return out.filter { it.length >= 3 }.distinct().take(6)
    }

    private fun strip(s: String): String {
        val cut = s.replace(GRADE, "").trim().trimEnd(',', '.', '-')
        return if (cut.count { it.isLetter() } >= 3) cut else s
    }

    /** "ACETON" -> "Aceton", Formeln wie "NaOH" bleiben unverändert. */
    private fun pretty(s: String): String {
        val letters = s.filter { it.isLetter() }
        if (letters.length < 4 || letters != letters.uppercase()) return s
        return s.split(" ").joinToString(" ") { w ->
            if (w.any { it.isDigit() }) w else w.lowercase().replaceFirstChar { it.titlecase() }
        }
    }
}
