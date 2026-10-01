package at.packliste.app

import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppTest {
    @get:Rule val camera: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    private val ctx get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun clean() {
        File(ctx.filesDir, "daten.json").delete()
    }

    private fun waitMs(ms: Long) = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription() = "warten $ms ms"
        override fun perform(ui: UiController, view: View) = ui.loopMainThreadForAtLeast(ms)
    }

    private fun clickChild(id: Int) = object : ViewAction {
        override fun getConstraints(): Matcher<View>? = null
        override fun getDescription() = "Kind-View $id antippen"
        override fun perform(ui: UiController, view: View) { view.findViewById<View>(id).performClick() }
    }

    private fun launch(): ActivityScenario<MainActivity> {
        val i = android.content.Intent(ctx, MainActivity::class.java).putExtra("nocamera", true)
        return ActivityScenario.launch(i)
    }

    private fun rows(s: ActivityScenario<MainActivity>): List<Row> {
        var r: List<Row> = emptyList()
        s.onActivity { r = Store(it).apply { load() }.current.rows.toList() }
        return r
    }

    private fun shown(s: ActivityScenario<MainActivity>): Int {
        var n = -1
        s.onActivity { n = it.findViewById<RecyclerView>(R.id.list).adapter!!.itemCount }
        return n
    }

    /** Etikettbild wie ein geteiltes Foto in die App geben und auf die Erkennung warten. */
    private fun addLabel(s: ActivityScenario<MainActivity>, asset: String) {
        val testCtx = InstrumentationRegistry.getInstrumentation().context
        val f = File(ctx.cacheDir, asset)
        testCtx.assets.open(asset).use { inp -> f.outputStream().use { inp.copyTo(it) } }
        s.onActivity { it.importImage(android.net.Uri.fromFile(f)) }
        for (k in 0 until 40) {
            Thread.sleep(500)
            val r = rows(s)
            if (r.isNotEmpty() && r.last().name.isNotBlank()) break
        }
        Thread.sleep(500)
    }

    @Test fun erkennungUndAntippenUndLoeschen() {
        launch().use { s ->
            addLabel(s, "label_panreac.jpg")
            addLabel(s, "label_aceton.jpg")
            val names = rows(s).map { it.name }
            Log.i("PACKTEST", "Namen in der Liste: $names")
            assertEquals("angezeigte Zeilen", 2, shown(s))
            assertEquals(listOf("Acetonitril", "Aceton"), names)

            // Vorschaubild mit echtem Touch antippen -> Großansicht
            onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, object : ViewAction {
                override fun getConstraints(): Matcher<View>? = null
                override fun getDescription() = "Touch auf Vorschaubild"
                override fun perform(ui: UiController, view: View) { click().perform(ui, view.findViewById(R.id.thumb)) }
            }))
            onView(withText("Schließen")).inRoot(isDialog()).check(matches(isDisplayed()))
            Log.i("PACKTEST", "Großansicht geöffnet")
            onView(withText("Löschen")).inRoot(isDialog()).perform(click())
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
            Thread.sleep(800)
            assertEquals("Zeilen nach Löschen in Großansicht", 1, rows(s).size)
            assertEquals("angezeigt nach Löschen", 1, shown(s))
            Log.i("PACKTEST", "Löschen in Großansicht ok")

            // Zeile antippen -> Namen-Dialog
            onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            onView(withText("Speichern")).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText("Abbrechen")).inRoot(isDialog()).perform(click())
            Log.i("PACKTEST", "Namen-Dialog ok")

            // lange drücken -> löschen
            onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick()))
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
            Thread.sleep(800)
            assertEquals("Zeilen nach lang drücken", 0, rows(s).size)
            Log.i("PACKTEST", "Lang drücken ok")
        }
    }

    private fun ocr(asset: String): List<String> {
        val ctxTest = InstrumentationRegistry.getInstrumentation().context
        val bmp = ctxTest.assets.open(asset).use { BitmapFactory.decodeStream(it) }
        val text = Tasks.await(TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(InputImage.fromBitmap(bmp, 0)))
        val raw = text.textBlocks.flatMap { b -> b.lines.map { it.text } }
        val picked = NamePicker.pick(text)
        Log.i("PACKTEST", "$asset gelesen=$raw  gewählt=$picked")
        return picked
    }

    @Test fun erkennungPanreac() = assertEquals("Acetonitril", ocr("label_panreac.jpg").firstOrNull())

    @Test fun erkennungAceton() = assertEquals("Aceton", ocr("label_aceton.jpg").firstOrNull())
}
