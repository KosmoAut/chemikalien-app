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

    private fun rowCount(s: ActivityScenario<MainActivity>): Int {
        var n = -1
        s.onActivity { n = it.findViewById<RecyclerView>(R.id.list).adapter!!.itemCount }
        return n
    }

    private fun takePhoto() {
        onView(withId(R.id.capture)).perform(click())
        onView(isRoot()).perform(waitMs(6000))
    }

    @Test fun fotoAntippenUndLoeschen() {
        ActivityScenario.launch(MainActivity::class.java).use { s ->
            onView(isRoot()).perform(waitMs(4000))
            takePhoto()
            assertEquals("Zeile nach Foto", 1, rowCount(s))
            // Vorschaubild antippen -> Großansicht mit Löschen
            onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, clickChild(R.id.thumb)))
            onView(isRoot()).perform(waitMs(1500))
            onView(withText("Schließen")).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText("Löschen")).inRoot(isDialog()).perform(click())
            onView(isRoot()).perform(waitMs(800))
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
            onView(isRoot()).perform(waitMs(1500))
            assertEquals("Zeilen nach Löschen", 0, rowCount(s))
        }
    }

    @Test fun zeileAntippenUndLangDruecken() {
        ActivityScenario.launch(MainActivity::class.java).use { s ->
            onView(isRoot()).perform(waitMs(4000))
            takePhoto()
            // echtes Antippen der Zeile (wie mit dem Finger)
            onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            onView(isRoot()).perform(waitMs(1000))
            onView(withText("Speichern")).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText("Abbrechen")).inRoot(isDialog()).perform(click())
            // lange drücken -> löschen
            onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick()))
            onView(isRoot()).perform(waitMs(800))
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
            onView(isRoot()).perform(waitMs(1500))
            assertEquals("Zeilen nach lang drücken", 0, rowCount(s))
        }
    }

    @Test fun vorschaubildMitFingerAntippen() {
        ActivityScenario.launch(MainActivity::class.java).use { s ->
            onView(isRoot()).perform(waitMs(4000))
            takePhoto()
            // echter Touch auf die Mitte des Vorschaubilds
            onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, object : ViewAction {
                override fun getConstraints(): Matcher<View>? = null
                override fun getDescription() = "Touch auf Vorschaubild"
                override fun perform(ui: UiController, view: View) {
                    val t = view.findViewById<View>(R.id.thumb)
                    click().perform(ui, t)
                }
            }))
            onView(isRoot()).perform(waitMs(1500))
            onView(withText("Schließen")).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText("Schließen")).inRoot(isDialog()).perform(click())
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
