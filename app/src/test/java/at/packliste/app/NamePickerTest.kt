package at.packliste.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NamePickerTest {
    private fun l(t: String, h: Float, top: Float = 0f, angle: Float = 0f) = NamePicker.LineInfo(t, h, top, angle)

    @Test fun panreacAcetonitril() {
        val r = NamePicker.pickFromLines(listOf(
            l("PanReac AppliChem", 90f, 0f, -90f),
            l("361881.1612", 40f, 10f),
            l("Acetonitrile for UV, IR, HPLC, ACS", 22f, 60f),
            l("Acetonitril", 14f, 100f),
            l("Acetonitrilo", 14f, 115f),
            l("Lot:", 12f, 5f),
        ))
        assertEquals("Acetonitril", r.first())
    }

    @Test fun panreacHorizontalBrand() {
        val r = NamePicker.pickFromLines(listOf(l("PanReacAppliChem", 90f), l("Acetonitrile for UV", 22f, 60f)))
        assertEquals("Acetonitril", r.first())
    }

    @Test fun acetonLabel() {
        val r = NamePicker.pickFromLines(listOf(
            l("ACETON 99%", 30f, 0f), l("rein", 25f, 35f), l("C3H6O - 58,08 g/mol", 25f, 70f),
            l("Nettogewicht: 1 l", 22f, 100f), l("GEFAHR", 30f, 130f), l("EG-Nr.: 200-662-2", 18f, 300f),
        ))
        assertEquals("Aceton", r.first())
    }

    @Test fun methanolIsNotEthanol() {
        assertEquals("Methanol", NamePicker.pickFromLines(listOf(l("Methanol HPLC grade", 30f))).first())
    }

    @Test fun longestNameWins() {
        assertEquals("Natriumhydroxid", NamePicker.pickFromLines(listOf(l("Natriumhydroxid Plätzchen", 30f))).first())
        assertEquals("Eisen(III)-chlorid", NamePicker.pickFromLines(listOf(l("Eisen(III)-chlorid Hexahydrat", 30f))).first())
        assertEquals("n-Hexan", NamePicker.pickFromLines(listOf(l("n-Hexane for HPLC", 30f))).first())
    }

    @Test fun casFallback() {
        val r = NamePicker.pickFromLines(listOf(l("Lösung A", 30f), l("CAS 7647-01-0", 12f, 50f)))
        assertEquals("Salzsäure", r.first())
    }

    @Test fun unknownUsesBiggestNonBrand() {
        val r = NamePicker.pickFromLines(listOf(l("Merck", 50f), l("Spezialreiniger XY-Konzentrat for lab use", 30f, 60f)))
        assertTrue(r.first(), r.first().startsWith("Spezialreiniger"))
    }

    @Test fun casChecksum() {
        assertTrue(Chemicals.casValid("67-64-1"))
        assertTrue(!Chemicals.casValid("67-64-2"))
    }
}
