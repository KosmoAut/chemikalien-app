package at.packliste.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class Row(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var uri: String = "",
    var alt: List<String> = emptyList(),
    var reading: Boolean = false,
    var raw: List<String> = emptyList(),
)

class Box(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var kons: String,
    val rows: MutableList<Row> = mutableListOf(),
    var docUri: String = "",
    var csvUri: String = "",
)

/** Speichert alle Boxen als JSON in der App. */
class Store(context: Context) {
    private val file = File(context.filesDir, "daten.json")
    val boxes = mutableListOf<Box>()
    var currentIndex = 0

    val current: Box
        get() {
            if (boxes.isEmpty()) addBox()
            currentIndex = currentIndex.coerceIn(0, boxes.size - 1)
            return boxes[currentIndex]
        }

    fun addBox(): Box {
        val kons = boxes.getOrNull(currentIndex)?.kons ?: "flüssig"
        var n = boxes.size + 1
        while (boxes.any { it.name == "Box $n" }) n++
        val b = Box(name = "Box $n", kons = kons)
        boxes.add(b)
        currentIndex = boxes.size - 1
        save()
        return b
    }

    fun load() {
        boxes.clear()
        if (!file.exists()) return
        try {
            val root = JSONObject(file.readText())
            currentIndex = root.optInt("current", 0)
            val arr = root.optJSONArray("boxes") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val rows = mutableListOf<Row>()
                val ra = o.optJSONArray("rows") ?: JSONArray()
                for (j in 0 until ra.length()) {
                    val r = ra.getJSONObject(j)
                    val alt = mutableListOf<String>()
                    val aa = r.optJSONArray("alt") ?: JSONArray()
                    for (k in 0 until aa.length()) alt.add(aa.getString(k))
                    val raw = mutableListOf<String>()
                    val rw = r.optJSONArray("raw") ?: JSONArray()
                    for (k in 0 until rw.length()) raw.add(rw.getString(k))
                    rows.add(Row(r.optString("id", UUID.randomUUID().toString()), r.optString("name"), r.optString("uri"), alt, false, raw))
                }
                boxes.add(Box(o.optString("id", UUID.randomUUID().toString()), o.optString("name", "Box"), o.optString("kons", "flüssig"), rows, o.optString("docUri"), o.optString("csvUri")))
            }
        } catch (e: Exception) {
            // beschädigte Datei: Sicherung behalten, neu anfangen
            file.copyTo(File(file.parentFile, "daten_defekt.json"), overwrite = true)
            boxes.clear()
        }
    }

    fun save() {
        val arr = JSONArray()
        for (b in boxes) {
            val ra = JSONArray()
            for (r in b.rows) {
                ra.put(JSONObject().put("id", r.id).put("name", r.name).put("uri", r.uri).put("alt", JSONArray(r.alt)).put("raw", JSONArray(r.raw)))
            }
            arr.put(JSONObject().put("id", b.id).put("name", b.name).put("kons", b.kons).put("rows", ra).put("docUri", b.docUri).put("csvUri", b.csvUri))
        }
        val tmp = File(file.parentFile, "daten.tmp")
        tmp.writeText(JSONObject().put("current", currentIndex).put("boxes", arr).toString())
        tmp.renameTo(file)
    }
}
