package at.packliste.app

/** Erzeugt die Packliste »Gefährliche Abfälle in Kleingebinden« (Word-kompatibles HTML) und eine CSV-Liste. */
object Packliste {
    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    fun html(box: Box): String {
        val per = 20
        val pages = maxOf(1, (box.rows.size + per - 1) / per)
        val body = StringBuilder()
        for (p in 0 until pages) {
            val tr = StringBuilder()
            for (i in 0 until per) {
                val n = p * per + i
                val r = box.rows.getOrNull(n)
                tr.append("<tr><td class=\"c\">${if (r != null) n + 1 else ""}</td><td>${esc(r?.name ?: "")}</td><td class=\"c\"></td>")
                tr.append("<td class=\"c\">${if (r != null && box.kons == "fest") "X" else ""}</td>")
                tr.append("<td class=\"c\">${if (r != null && box.kons != "fest") "X" else ""}</td></tr>")
            }
            val brk = if (p < pages - 1) " style=\"page-break-after:always\"" else ""
            body.append(
                """<div$brk><p class="t">Packliste »Gefährliche Abfälle in Kleingebinden«</p><p class="r">Blatt ${p + 1} von $pages</p>
<table><tr><td style="height:36pt;width:75%">Abfallerzeuger/Kunde:</td><td>Fass-Nr.: ${esc(box.name)}</td></tr>
<tr><td colspan="2">Übergebinde: &nbsp;☐ 30 l Fass &nbsp;☐ 60 l Fass &nbsp;☐ 120 l Fass &nbsp;☐ 200 l Fass &nbsp;☐ Sonstige ________________</td></tr></table><br>
<table class="g"><tr><th rowspan="2" style="width:8%">lfd.Nr.</th><th rowspan="2" style="text-align:left">Produkt- oder chemische Bezeichnung und Art des Gebindes</th><th style="width:15%">Menge</th><th colspan="2" style="width:20%">Konsistenz</th></tr>
<tr><th><i>Liter / kg</i></th><th><i>fest</i></th><th><i>flüssig</i></th></tr>$tr</table></div>"""
            )
        }
        return """<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word"><head><meta charset="utf-8">
<style>@page{size:A4;margin:1.5cm}body{font-family:Arial,sans-serif;font-size:10pt}.t{color:#d0101c;font-size:18pt;margin:0}.r{text-align:right;margin:4pt 0}
table{border-collapse:collapse;width:100%}td,th{border:1px solid #000;padding:3pt 5pt;font-size:10pt}.g td{height:16pt}.c{text-align:center}</style></head><body>$body</body></html>"""
    }

    fun csv(box: Box): String {
        val sb = StringBuilder("Nr;Name;Konsistenz\r\n")
        box.rows.forEachIndexed { i, r -> sb.append("${i + 1};\"${r.name.replace("\"", "\"\"")}\";${box.kons}\r\n") }
        return sb.toString()
    }
}
