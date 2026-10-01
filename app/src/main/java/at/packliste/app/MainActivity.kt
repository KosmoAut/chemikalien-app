package at.packliste.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.view.Gravity
import android.widget.FrameLayout
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var store: Store
    private lateinit var preview: PreviewView
    private lateinit var status: TextView
    private lateinit var spinner: Spinner
    private lateinit var kons: RadioGroup
    private lateinit var list: RecyclerView
    private lateinit var adapter: RowAdapter

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val thumbs = LruCache<String, Bitmap>(80)
    private var updatingUi = false

    private val deleteLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }

    /** Foto löschen; bei Fotos aus einer früheren Installation fragt Android um Erlaubnis. */
    private fun deletePhotoFile(uriStr: String) {
        if (uriStr.isBlank()) return
        val uri = Uri.parse(uriStr)
        try {
            contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            if (Build.VERSION.SDK_INT >= 30) {
                try {
                    val sender = MediaStore.createDeleteRequest(contentResolver, listOf(uri)).intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    private val camPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) startCamera() else say("Ohne Kamera-Erlaubnis geht es nicht. In den Einstellungen erlauben.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashFile = java.io.File(filesDir, "absturz.txt")
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try { crashFile.writeText("${e.javaClass.simpleName}: ${e.message}\n" + e.stackTrace.take(6).joinToString("\n")) } catch (_: Exception) { }
            prev?.uncaughtException(t, e)
        }
        setContentView(R.layout.activity_main)
        preview = findViewById(R.id.preview)
        status = findViewById(R.id.status)
        spinner = findViewById(R.id.boxSpinner)
        kons = findViewById(R.id.kons)
        list = findViewById(R.id.list)

        store = Store(this)
        store.load()
        if (store.boxes.isEmpty()) store.addBox()

        adapter = RowAdapter()
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (updatingUi) return
                store.currentIndex = pos
                store.save()
                refreshBox()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        kons.setOnCheckedChangeListener { _, id ->
            if (updatingUi) return@setOnCheckedChangeListener
            store.current.kons = if (id == R.id.fest) "fest" else "flüssig"
            store.save()
        }
        findViewById<Button>(R.id.addBox).setOnClickListener {
            store.addBox()
            refreshAll()
            say("${store.current.name} angelegt (${store.current.kons})")
        }
        findViewById<Button>(R.id.menuBtn).setOnClickListener { showMenu(it) }
        findViewById<Button>(R.id.capture).setOnClickListener { takePhoto() }

        refreshAll()
        val ver = try { packageManager.getPackageInfo(packageName, 0).versionName } catch (_: Exception) { "?" }
        say("Packliste Version $ver – Etikett antippen zum Scharfstellen")
        if (crashFile.exists()) {
            val txt = crashFile.readText(); crashFile.delete()
            AlertDialog.Builder(this).setTitle("Letzter Absturz").setMessage(txt).setPositiveButton("OK", null).show()
        }

        if (intent?.getBooleanExtra("nocamera", false) == true) { /* automatischer Test ohne Kamera */ }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else camPermission.launch(Manifest.permission.CAMERA)
        handleShare(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShare(intent)
    }

    /** Fotos, die aus der Galerie o. Ä. an die App geteilt werden, in die aktuelle Box übernehmen. */
    @Suppress("DEPRECATION")
    private fun handleShare(intent: Intent?) {
        if (intent == null) return
        val uris = when (intent.action) {
            Intent.ACTION_SEND -> listOfNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
            Intent.ACTION_SEND_MULTIPLE -> intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) ?: emptyList()
            else -> emptyList()
        }
        if (uris.isEmpty()) return
        intent.action = null
        for (u in uris) importImage(u)
    }

    internal fun importImage(src: Uri) {
        val box = store.current
        val nr = box.rows.size + 1
        try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, String.format(Locale.ROOT, "%03d_foto_%d.jpg", nr, System.currentTimeMillis()))
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, photoFolder(box))
            }
            val dst = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: throw IllegalStateException("kein Speicherplatz")
            contentResolver.openInputStream(src).use { input ->
                contentResolver.openOutputStream(dst).use { out -> input!!.copyTo(out!!) }
            }
            val row = Row(uri = dst.toString(), reading = true)
            box.rows.add(row)
            store.save()
            refreshBox()
            list.scrollToPosition(box.rows.size - 1)
            recognize(box, row, dst)
        } catch (e: Exception) {
            say("Foto konnte nicht übernommen werden: ${e.message}")
        }
    }

    // ---------- Kamera ----------

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            val p = Preview.Builder().build()
            p.setSurfaceProvider(preview.surfaceProvider)
            val ic = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            try {
                provider.unbindAll()
                camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, p, ic)
                imageCapture = ic
            } catch (e: Exception) {
                say("Kamera konnte nicht gestartet werden: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))

        preview.setOnTouchListener { v, ev ->
            if (ev.action == MotionEvent.ACTION_UP) {
                val point = preview.meteringPointFactory.createPoint(ev.x, ev.y)
                camera?.cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
                v.performClick()
            }
            true
        }
    }

    /** Lautstärke-Taste löst auch aus. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event?.repeatCount == 0) takePhoto()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun takePhoto() {
        val ic = imageCapture ?: run { say("Kamera startet noch …"); return }
        val box = store.current
        val nr = box.rows.size + 1
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, String.format(Locale.ROOT, "%03d_foto_%d.jpg", nr, System.currentTimeMillis()))
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, photoFolder(box))
        }
        val opts = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).build()
        val row = Row(reading = true)
        box.rows.add(row)
        adapter.notifyItemInserted(box.rows.size - 1)
        list.scrollToPosition(box.rows.size - 1)
        updateSpinnerLabels()
        preview.animate().alpha(0.3f).setDuration(80).withEndAction { preview.animate().alpha(1f).setDuration(120) }
        say("Foto $nr wird gelesen …")

        ic.takePicture(opts, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(out: ImageCapture.OutputFileResults) {
                val uri = out.savedUri
                if (uri == null) { failRow(box, row, "Foto nicht gespeichert"); return }
                row.uri = uri.toString()
                store.save()
                notifyRow(box, row)
                recognize(box, row, uri)
            }
            override fun onError(e: ImageCaptureException) = failRow(box, row, "Foto fehlgeschlagen: ${e.message}")
        })
    }

    private fun failRow(box: Box, row: Row, msg: String) {
        box.rows.remove(row)
        store.save()
        refreshBox()
        say(msg)
    }

    // ---------- Texterkennung ----------

    private fun recognize(box: Box, row: Row, uri: Uri) {
        row.reading = true
        notifyRow(box, row)
        val image = try { InputImage.fromFilePath(this, uri) } catch (e: Exception) {
            row.reading = false; notifyRow(box, row); say("Foto konnte nicht gelesen werden"); return
        }
        recognizer.process(image)
            .addOnSuccessListener { text ->
                val alts = NamePicker.pick(text)
                row.raw = text.textBlocks.flatMap { b -> b.lines.map { it.text } }.take(20)
                row.alt = alts
                if (row.name.isBlank()) row.name = alts.firstOrNull() ?: ""
                row.reading = false
                store.save()
                renamePhoto(box, row)
                notifyRow(box, row)
                say(if (row.name.isNotBlank()) "✓ ${row.name}" else "Name nicht lesbar – Zeile antippen und eintippen")
            }
            .addOnFailureListener { e ->
                row.reading = false
                notifyRow(box, row)
                say("Texterkennung fehlgeschlagen: ${e.message}")
            }
    }

    // ---------- Dateien ----------

    private fun safe(s: String) = s.replace(Regex("[\\\\/:*?\"<>|]+"), "").replace(Regex("\\s+"), " ").trim().take(50).ifBlank { "unbenannt" }
    private fun photoFolder(box: Box) = "Pictures/Chemikalien/${safe(box.name)}/"
    private fun docFolder(box: Box) = "Documents/Chemikalien/${safe(box.name)}/"

    private fun renamePhoto(box: Box, row: Row) {
        if (row.uri.isBlank()) return
        val nr = box.rows.indexOf(row) + 1
        val cv = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, String.format(Locale.ROOT, "%03d_%s.jpg", nr, safe(row.name.ifBlank { "unbenannt" })))
        }
        try { contentResolver.update(Uri.parse(row.uri), cv, null, null) } catch (_: Exception) { }
    }

    private fun moveBoxPhotos(box: Box) {
        for (r in box.rows) {
            if (r.uri.isBlank()) continue
            val cv = ContentValues().apply { put(MediaStore.MediaColumns.RELATIVE_PATH, photoFolder(box)) }
            try { contentResolver.update(Uri.parse(r.uri), cv, null, null) } catch (_: Exception) { }
        }
    }

    private fun writeDocument(box: Box, oldUri: String, fileName: String, mime: String, content: ByteArray): Uri? {
        if (oldUri.isNotBlank()) try { contentResolver.delete(Uri.parse(oldUri), null, null) } catch (_: Exception) { }
        val cv = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, docFolder(box))
        }
        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), cv) ?: return null
        contentResolver.openOutputStream(uri)?.use { it.write(content) }
        return uri
    }

    private fun exportBox() {
        val box = store.current
        if (box.rows.isEmpty()) { say("Die Box ist noch leer."); return }
        try {
            val name = safe(box.name)
            val doc = writeDocument(box, box.docUri, "Packliste_$name.doc", "application/msword", ("﻿" + Packliste.html(box)).toByteArray())
            val csv = writeDocument(box, box.csvUri, "Liste_$name.csv", "text/csv", ("﻿" + Packliste.csv(box)).toByteArray())
            box.docUri = doc?.toString() ?: ""
            box.csvUri = csv?.toString() ?: ""
            store.save()
            AlertDialog.Builder(this)
                .setTitle("Gespeichert")
                .setMessage("Packliste und Liste: Dokumente/Chemikalien/$name\nFotos: Bilder/Chemikalien/$name")
                .setPositiveButton("Packliste teilen") { _, _ ->
                    if (doc != null) startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "application/msword"
                        putExtra(Intent.EXTRA_STREAM, doc)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, "Packliste teilen"))
                }
                .setNegativeButton("OK", null)
                .show()
        } catch (e: Exception) {
            say("Speichern fehlgeschlagen: ${e.message}")
        }
    }

    // ---------- Menü & Dialoge ----------

    private fun showMenu(anchor: View) {
        val m = PopupMenu(this, anchor)
        m.menu.add(0, 1, 0, "Packliste speichern")
        m.menu.add(0, 2, 1, "Box umbenennen")
        m.menu.add(0, 3, 2, "Box aus Liste entfernen")
        m.setOnMenuItemClickListener {
            when (it.itemId) {
                1 -> exportBox()
                2 -> renameBox()
                3 -> removeBox()
            }
            true
        }
        m.show()
    }

    private fun renameBox() {
        val box = store.current
        val input = EditText(this).apply { setText(box.name); selectAll() }
        AlertDialog.Builder(this)
            .setTitle("Box umbenennen")
            .setView(padded(input))
            .setPositiveButton("OK") { _, _ ->
                val n = input.text.toString().trim()
                if (n.isNotBlank()) { box.name = n; store.save(); moveBoxPhotos(box); refreshAll() }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun removeBox() {
        val box = store.current
        AlertDialog.Builder(this)
            .setTitle("${box.name} entfernen?")
            .setMessage("Die Box verschwindet aus der App. Fotos und Packliste bleiben im Ordner am Handy.")
            .setPositiveButton("Entfernen") { _, _ ->
                store.boxes.remove(box)
                if (store.boxes.isEmpty()) store.addBox()
                store.currentIndex = store.boxes.size - 1
                store.save(); refreshAll()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun editRow(row: Row) {
        val box = store.current
        val input = EditText(this).apply { setText(row.name); hint = "Name"; setSelection(text.length) }
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        lateinit var dialog: AlertDialog
        val others = row.alt.filter { it != row.name }
        if (others.isNotEmpty()) {
            wrap.addView(TextView(this).apply { text = "Vorschläge vom Etikett:"; setPadding(0, 0, 0, 8) })
            for (a in others) wrap.addView(Button(this).apply {
                text = a
                isAllCaps = false
                setOnClickListener { setRowName(box, row, a); dialog.dismiss() }
            })
        }
        if (row.raw.isNotEmpty()) wrap.addView(TextView(this).apply {
            text = "Gelesener Text:\n" + row.raw.joinToString(" · ")
            textSize = 12f
            setPadding(0, 8, 0, 8)
        })
        wrap.addView(input)
        if (row.uri.isNotBlank()) wrap.addView(Button(this).apply {
            text = "Erneut lesen"
            isAllCaps = false
            setOnClickListener { row.name = ""; recognize(box, row, Uri.parse(row.uri)); dialog.dismiss() }
        })
        dialog = AlertDialog.Builder(this)
            .setTitle("Nr. ${box.rows.indexOf(row) + 1}")
            .setView(padded(wrap))
            .setPositiveButton("Speichern") { _, _ -> setRowName(box, row, input.text.toString().trim()) }
            .setNeutralButton("Löschen") { _, _ -> deleteRow(box, row) }
            .setNegativeButton("Abbrechen", null)
            .create()
        dialog.show()
    }

    private fun setRowName(box: Box, row: Row, name: String) {
        row.name = name
        store.save()
        renamePhoto(box, row)
        notifyRow(box, row)
    }

    private fun deleteRow(box: Box, row: Row, afterDelete: (() -> Unit)? = null) {
        AlertDialog.Builder(this)
            .setTitle("Zeile löschen?")
            .setMessage("Das Foto wird auch gelöscht.")
            .setPositiveButton("Löschen") { _, _ ->
                deletePhotoFile(row.uri)
                box.rows.remove(row)
                box.rows.forEach { renamePhoto(box, it) }
                thumbs.remove(row.uri)
                store.save(); refreshBox()
                say("Foto gelöscht")
                afterDelete?.invoke()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    /** Foto groß anzeigen, mit Löschen / Name ändern. */
    private fun showPhoto(row: Row) {
        val box = store.current
        val dlg = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val img = ImageView(this).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
        root.addView(img, FrameLayout.LayoutParams(-1, -1))
        val title = TextView(this).apply {
            text = "Nr. ${box.rows.indexOf(row) + 1}: ${row.name.ifBlank { "ohne Namen" }}"
            setTextColor(Color.WHITE); textSize = 18f
            setBackgroundColor(0x99000000.toInt())
            val p = (14 * resources.displayMetrics.density).toInt(); setPadding(p, p, p, p)
        }
        root.addView(title, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0x99000000.toInt())
            val p = (8 * resources.displayMetrics.density).toInt(); setPadding(p, p, p, p)
        }
        fun btn(label: String, color: Int, onClick: () -> Unit) = Button(this).apply {
            text = label; isAllCaps = false; setTextColor(color)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { onClick() }
        }
        bar.addView(btn("Löschen", 0xFFFF5A64.toInt()) { deleteRow(box, row) { dlg.dismiss() } }, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(btn("Name ändern", Color.WHITE) { dlg.dismiss(); editRow(row) }, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(btn("Schließen", Color.WHITE) { dlg.dismiss() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(bar, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        dlg.setContentView(root)
        dlg.show()

        val maxSide = maxOf(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
        io.execute {
            val bmp = try {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, Uri.parse(row.uri))) { dec, info, _ ->
                    val s = maxOf(info.size.width, info.size.height)
                    if (s > maxSide) dec.setTargetSampleSize(maxOf(1, s / maxSide))
                }
            } catch (_: Exception) { null }
            main.post { if (bmp != null) img.setImageBitmap(bmp) else title.text = "Foto nicht gefunden (evtl. in der Galerie gelöscht)" }
        }
    }

    private fun padded(v: View): View {
        val p = (20 * resources.displayMetrics.density).toInt()
        return LinearLayout(this).apply { setPadding(p, p / 2, p, 0); addView(v, LinearLayout.LayoutParams(-1, -2)) }
    }

    // ---------- Anzeige ----------

    private fun say(msg: String) { status.text = msg }

    private fun safely(what: String, block: () -> Unit) {
        say("$what angetippt …")
        try { block() } catch (e: Throwable) { say("Fehler ($what): ${e.javaClass.simpleName}: ${e.message}") }
    }

    private fun refreshAll() {
        updatingUi = true
        val labels = store.boxes.map { "${it.name}  (${it.rows.size})" }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        spinner.setSelection(store.currentIndex.coerceIn(0, store.boxes.size - 1))
        updatingUi = false
        refreshBox()
    }

    private fun updateSpinnerLabels() {
        val a = spinner.adapter as? ArrayAdapter<*> ?: return
        if (a.count != store.boxes.size) { refreshAll(); return }
        updatingUi = true
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, store.boxes.map { "${it.name}  (${it.rows.size})" })
        spinner.setSelection(store.currentIndex)
        updatingUi = false
    }

    private fun refreshBox() {
        updatingUi = true
        kons.check(if (store.current.kons == "fest") R.id.fest else R.id.fluessig)
        updatingUi = false
        adapter.notifyDataSetChanged()
        updateSpinnerLabels()
    }

    private fun notifyRow(box: Box, row: Row) {
        if (box !== store.current) return
        val i = box.rows.indexOf(row)
        if (i >= 0) adapter.notifyItemChanged(i)
    }

    private inner class RowHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nr: TextView = v.findViewById(R.id.nr)
        val thumb: ImageView = v.findViewById(R.id.thumb)
        val name: TextView = v.findViewById(R.id.name)
        val busy: ProgressBar = v.findViewById(R.id.busy)
    }

    private inner class RowAdapter : RecyclerView.Adapter<RowHolder>() {
        override fun getItemCount() = store.current.rows.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RowHolder(LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false))

        override fun onBindViewHolder(h: RowHolder, pos: Int) {
            val row = store.current.rows[pos]
            h.nr.text = "${pos + 1}"
            h.name.text = when {
                row.reading -> "wird gelesen …"
                row.name.isBlank() -> "– antippen zum Eintragen –"
                else -> row.name
            }
            h.busy.visibility = if (row.reading) View.VISIBLE else View.GONE
            h.itemView.setOnClickListener { safely("Zeile") { editRow(row) } }
            h.itemView.setOnLongClickListener { safely("Löschen") { deleteRow(store.current, row) }; true }
            h.thumb.isClickable = true
            h.thumb.setOnClickListener { safely("Foto") { if (row.uri.isNotBlank()) showPhoto(row) else editRow(row) } }
            h.thumb.setImageDrawable(null)
            h.thumb.tag = row.uri
            if (row.uri.isNotBlank()) {
                val cached = thumbs.get(row.uri)
                if (cached != null) h.thumb.setImageBitmap(cached)
                else io.execute {
                    val bmp = try { contentResolver.loadThumbnail(Uri.parse(row.uri), Size(200, 200), null) } catch (_: Exception) { null }
                    if (bmp != null) {
                        thumbs.put(row.uri, bmp)
                        main.post { if (h.thumb.tag == row.uri) h.thumb.setImageBitmap(bmp) }
                    }
                }
            }
        }
    }
}
