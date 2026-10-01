package at.packliste.app

/**
 * Gängige Laborchemikalien: deutscher Name (wird eingetragen) | weitere Schreibweisen | CAS-Nummer(n).
 * Die App sucht diese Namen im erkannten Etikett-Text.
 */
object Chemicals {
    private const val DATA = """
Aceton|Acetone|Acetona|Acétone|Propanon|67-64-1
Acetonitril|Acetonitrile|Acetonitrilo|Acétonitrile|Acetonitrilo|Methylcyanid|75-05-8
Ethanol|Ethyl alcohol|Ethylalkohol|Etanol|Éthanol|Alkohol|64-17-5
Methanol|Methyl alcohol|Methylalkohol|Metanol|Méthanol|67-56-1
2-Propanol|Isopropanol|Isopropylalkohol|Isopropyl alcohol|Propan-2-ol|IPA|67-63-0
1-Propanol|n-Propanol|Propan-1-ol|71-23-8
1-Butanol|n-Butanol|Butan-1-ol|71-36-3
2-Butanol|Butan-2-ol|sec-Butanol|78-92-2
tert-Butanol|2-Methyl-2-propanol|tert-Butyl alcohol|75-65-0
Isobutanol|2-Methyl-1-propanol|78-83-1
Ethylacetat|Ethyl acetate|Essigsäureethylester|Essigester|Acetato de etilo|141-78-6
Butylacetat|n-Butyl acetate|Butyl acetate|Essigsäurebutylester|123-86-4
Methylacetat|Methyl acetate|79-20-9
Diethylether|Diethyl ether|Ether|Ether|Éther|60-29-7
tert-Butylmethylether|MTBE|Methyl tert-butyl ether|tert-Butyl methyl ether|1634-04-4
Tetrahydrofuran|THF|Tetrahidrofurano|109-99-9
1,4-Dioxan|1,4-Dioxane|Dioxan|Dioxane|123-91-1
Dichlormethan|Dichloromethane|Methylenchlorid|Methylene chloride|Diclorometano|75-09-2
Chloroform|Trichlormethan|Trichloromethane|Cloroformo|67-66-3
Tetrachlorkohlenstoff|Carbon tetrachloride|Tetrachlormethan|56-23-5
1,2-Dichlorethan|1,2-Dichloroethane|107-06-2
Trichlorethylen|Trichloroethylene|Trichlorethen|79-01-6
Tetrachlorethylen|Tetrachloroethylene|Tetrachlorethen|Perchlorethylen|127-18-4
n-Hexan|Hexan|n-Hexane|Hexane|Hexano|110-54-3
n-Heptan|Heptan|n-Heptane|Heptane|142-82-5
Cyclohexan|Cyclohexane|Ciclohexano|110-82-7
n-Pentan|Pentan|Pentane|n-Pentane|109-66-0
Petrolether|Petroleum ether|Petroleumbenzin|8032-32-4|64742-49-0
Isooctan|2,2,4-Trimethylpentan|Isooctane|540-84-1
Toluol|Toluene|Tolueno|Toluène|108-88-3
Benzol|Benzene|Benceno|71-43-2
Xylol|Xylene|Xylole|Xileno|1330-20-7
Ethylbenzol|Ethylbenzene|100-41-4
Styrol|Styrene|100-42-5
Dimethylformamid|N,N-Dimethylformamide|Dimethylformamide|DMF|68-12-2
Dimethylsulfoxid|Dimethyl sulfoxide|Dimethylsulfoxide|DMSO|67-68-5
N-Methyl-2-pyrrolidon|N-Methyl-2-pyrrolidone|NMP|872-50-4
Pyridin|Pyridine|Piridina|110-86-1
Triethylamin|Triethylamine|121-44-8
Diethylamin|Diethylamine|109-89-7
Ethylendiamin|Ethylenediamine|107-15-3
Morpholin|Morpholine|110-91-8
Piperidin|Piperidine|110-89-4
Anilin|Aniline|62-53-3
Phenol|Fenol|108-95-2
Formaldehydlösung|Formaldehyde solution|Formalin|Formaldehyd|Formaldehyde|Formol|50-00-0
Acetaldehyd|Acetaldehyde|75-07-0
Glutaraldehyd|Glutaraldehyde|Glutardialdehyd|111-30-8
Benzaldehyd|Benzaldehyde|100-52-7
2-Butanon|Methylethylketon|Ethylmethylketon|Methyl ethyl ketone|Butanone|MEK|78-93-3
Methylisobutylketon|Methyl isobutyl ketone|4-Methyl-2-pentanon|MIBK|108-10-1
Cyclohexanon|Cyclohexanone|108-94-1
Acetylaceton|Acetylacetone|2,4-Pentandion|123-54-6
Essigsäure|Acetic acid|Eisessig|Glacial acetic acid|Ácido acético|Acide acétique|64-19-7
Ameisensäure|Formic acid|Ácido fórmico|64-18-6
Propionsäure|Propionic acid|79-09-4
Trifluoressigsäure|Trifluoroacetic acid|TFA|76-05-1
Trichloressigsäure|Trichloroacetic acid|TCA|76-03-9
Chloressigsäure|Chloroacetic acid|Monochloressigsäure|79-11-8
Essigsäureanhydrid|Acetic anhydride|Acetanhydrid|108-24-7
Acetylchlorid|Acetyl chloride|75-36-5
Thionylchlorid|Thionyl chloride|7719-09-7
Salzsäure|Hydrochloric acid|Chlorwasserstoffsäure|Ácido clorhídrico|Acide chlorhydrique|7647-01-0
Schwefelsäure|Sulfuric acid|Sulphuric acid|Ácido sulfúrico|Acide sulfurique|7664-93-9
Salpetersäure|Nitric acid|Ácido nítrico|Acide nitrique|7697-37-2
Phosphorsäure|Phosphoric acid|ortho-Phosphorsäure|o-Phosphorsäure|Ácido fosfórico|7664-38-2
Flusssäure|Fluorwasserstoffsäure|Hydrofluoric acid|7664-39-3
Perchlorsäure|Perchloric acid|7601-90-3
Borsäure|Boric acid|10043-35-3
Citronensäure|Zitronensäure|Citric acid|77-92-9|5949-29-1
Oxalsäure|Oxalic acid|144-62-7|6153-56-6
Weinsäure|Tartaric acid|L-Weinsäure|87-69-4
Ascorbinsäure|Ascorbic acid|Vitamin C|50-81-7
Benzoesäure|Benzoic acid|65-85-0
Salicylsäure|Salicylic acid|69-72-7
Natronlauge|Sodium hydroxide solution|Natriumhydroxid-Lösung
Natriumhydroxid|Sodium hydroxide|Ätznatron|Hidróxido de sodio|Hydroxyde de sodium|1310-73-2
Kalilauge|Potassium hydroxide solution|Kaliumhydroxid-Lösung
Kaliumhydroxid|Potassium hydroxide|Ätzkali|1310-58-3
Ammoniaklösung|Ammoniak|Ammonia solution|Ammonia|Ammoniumhydroxid|Ammonium hydroxide|1336-21-6|7664-41-7
Calciumhydroxid|Calcium hydroxide|Kalkhydrat|1305-62-0
Natriumchlorid|Sodium chloride|Kochsalz|7647-14-5
Kaliumchlorid|Potassium chloride|7447-40-7
Calciumchlorid|Calcium chloride|10043-52-4|10035-04-8
Magnesiumchlorid|Magnesium chloride|7786-30-3|7791-18-6
Ammoniumchlorid|Ammonium chloride|12125-02-9
Natriumcarbonat|Sodium carbonate|Soda|497-19-8
Natriumhydrogencarbonat|Sodium hydrogen carbonate|Sodium bicarbonate|Natron|144-55-8
Kaliumcarbonat|Potassium carbonate|Pottasche|584-08-7
Calciumcarbonat|Calcium carbonate|471-34-1
Natriumsulfat|Sodium sulfate|7757-82-6
Magnesiumsulfat|Magnesium sulfate|7487-88-9|10034-99-8
Kupfer(II)-sulfat|Kupfersulfat|Copper(II) sulfate|Copper sulfate|7758-98-7|7758-99-8
Eisen(III)-chlorid|Eisenchlorid|Iron(III) chloride|Ferric chloride|7705-08-0|10025-77-1
Eisen(II)-sulfat|Eisensulfat|Iron(II) sulfate|7720-78-7|7782-63-0
Zinksulfat|Zinc sulfate|7733-02-0|7446-20-0
Zinkchlorid|Zinc chloride|7646-85-7
Natriumnitrat|Sodium nitrate|7631-99-4
Kaliumnitrat|Potassium nitrate|7757-79-1
Silbernitrat|Silver nitrate|7761-88-8
Ammoniumnitrat|Ammonium nitrate|6484-52-2
Natriumnitrit|Sodium nitrite|7632-00-0
Kaliumpermanganat|Potassium permanganate|7722-64-7
Kaliumdichromat|Potassium dichromate|7778-50-9
Kaliumchromat|Potassium chromate|7789-00-6
Natriumhypochlorit-Lösung|Natriumhypochlorit|Sodium hypochlorite|Chlorbleichlauge|7681-52-9
Wasserstoffperoxid|Hydrogen peroxide|Perhydrol|7722-84-1
Natriumthiosulfat|Sodium thiosulfate|7772-98-7|10102-17-7
Natriumacetat|Sodium acetate|127-09-3|6131-90-4
Ammoniumacetat|Ammonium acetate|631-61-8
Kaliumiodid|Potassium iodide|7681-11-0
Iod|Jod|Iodine|7553-56-2
Kaliumbromid|Potassium bromide|7758-02-3
Natriumbromid|Sodium bromide|7647-15-6
Natriumfluorid|Sodium fluoride|7681-49-4
Natriumdihydrogenphosphat|Sodium dihydrogen phosphate|7558-80-7|10049-21-5|13472-35-0
Dinatriumhydrogenphosphat|Disodium hydrogen phosphate|7558-79-4|10028-24-7|10039-32-4
Kaliumdihydrogenphosphat|Potassium dihydrogen phosphate|7778-77-0
Dikaliumhydrogenphosphat|Dipotassium hydrogen phosphate|7758-11-4
Natriumazid|Sodium azide|26628-22-8
Natriumcyanid|Sodium cyanide|143-33-9
Kaliumcyanid|Potassium cyanide|151-50-8
Quecksilber(II)-chlorid|Quecksilberchlorid|Mercury(II) chloride|7487-94-7
Quecksilber|Mercury|7439-97-6
Blei(II)-nitrat|Bleinitrat|Lead(II) nitrate|10099-74-8
Bariumchlorid|Barium chloride|10361-37-2|10326-27-9
Cobalt(II)-chlorid|Cobaltchlorid|Cobalt(II) chloride|7646-79-9|7791-13-1
Nickel(II)-sulfat|Nickelsulfat|Nickel(II) sulfate|7786-81-4|10101-97-0
EDTA-Dinatriumsalz|Dinatrium-EDTA|Na2EDTA|Disodium EDTA|Titriplex III|139-33-3|6381-92-6
EDTA|Ethylendiamintetraessigsäure|Ethylenediaminetetraacetic acid|60-00-4
Tris|Trometamol|Tris(hydroxymethyl)aminomethan|Tris base|77-86-1
Harnstoff|Urea|57-13-6
D-Glucose|Glucose|Traubenzucker|50-99-7
Saccharose|Sucrose|57-50-1
Glycerin|Glycerol|Glycerine|56-81-5
Ethylenglycol|Ethylenglykol|Ethylene glycol|Ethan-1,2-diol|107-21-1
Polyethylenglycol|Polyethylene glycol|PEG|25322-68-3
Natriumdodecylsulfat|Sodium dodecyl sulfate|SDS|Natriumlaurylsulfat|151-21-3
Acrylamid|Acrylamide|79-06-1
Hydrochinon|Hydroquinone|123-31-9
Thioharnstoff|Thiourea|62-56-6
Hydrazin|Hydrazine|302-01-2|7803-57-8
Hydroxylammoniumchlorid|Hydroxylamine hydrochloride|Hydroxylaminhydrochlorid|5470-11-1
Natriumborhydrid|Sodium borohydride|Natriumtetrahydroborat|16940-66-2
Lithiumaluminiumhydrid|Lithium aluminium hydride|16853-85-3
Natrium|Sodium|7440-23-5
Kalium|Potassium|7440-09-7
Lithium|7439-93-2
Magnesium|7439-95-4
Zink|Zinc|7440-66-6
Aluminium|Aluminum|7429-90-5
Kupfer|Copper|7440-50-8
Eisen|Iron|7439-89-6
Schwefel|Sulfur|Sulphur|7704-34-9
Aktivkohle|Activated carbon|Activated charcoal|7440-44-0
Kieselgel|Silica gel|7631-86-9
Paraffinöl|Paraffin oil|8012-95-1
Paraffin|8002-74-2
Ethidiumbromid|Ethidium bromide|1239-45-8
Phenolphthalein|77-09-8
Methylorange|Methyl orange|547-58-0
Bromthymolblau|Bromothymol blue|76-59-5
Kristallviolett|Crystal violet|Gentianaviolett|548-62-9
Methylenblau|Methylene blue|61-73-4|7220-79-3
Eosin Y|Eosin|17372-87-1
Ninhydrin|485-47-2
Nitrobenzol|Nitrobenzene|98-95-3
Chlorbenzol|Chlorobenzene|108-90-7
Brombenzol|Bromobenzene|108-86-1
"""

    class Chem(val name: String, val keys: List<String>, val cas: List<String>)

    val all: List<Chem> by lazy {
        DATA.lines().map { it.trim() }.filter { it.isNotEmpty() }.map { line ->
            val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            val cas = parts.filter { CAS_RE.matches(it) }
            val names = parts.filterNot { CAS_RE.matches(it) }
            Chem(names.first(), names.map(::norm).filter { it.length >= 3 }.distinct(), cas)
        }
    }

    private val CAS_RE = Regex("\\d{2,7}-\\d{2}-\\d")
    private val CAS_FIND = Regex("(?<!\\d)(\\d{2,7})\\s?-\\s?(\\d{2})\\s?-\\s?(\\d)(?!\\d)")
    private val SPLIT = Regex("[\\s\\-/,;:]+")

    /** Kleinbuchstaben, nur Buchstaben und Ziffern, Umlaute/Akzente vereinheitlicht. */
    fun norm(s: String): String {
        val sb = StringBuilder()
        for (c in s.lowercase()) {
            when (c) {
                'ä' -> sb.append("ae"); 'ö' -> sb.append("oe"); 'ü' -> sb.append("ue"); 'ß' -> sb.append("ss")
                'á', 'à', 'â' -> sb.append('a'); 'é', 'è', 'ê', 'ë' -> sb.append('e'); 'í', 'ì', 'î', 'ï' -> sb.append('i')
                'ó', 'ò', 'ô' -> sb.append('o'); 'ú', 'ù', 'û' -> sb.append('u'); 'ñ' -> sb.append('n'); 'ç' -> sb.append('c')
                else -> if (c.isLetterOrDigit()) sb.append(c)
            }
        }
        return sb.toString()
    }

    class Hit(val chem: Chem, val keyLength: Int, val lineIndex: Int)

    /**
     * Sucht in den Zeilen (Reihenfolge = Wichtigkeit, größte Schrift zuerst) nach bekannten Chemikalien.
     * Ein Name zählt, wenn eine Wortfolge mit ihm beginnt und höchstens 3 Zeichen länger ist
     * (z. B. "Acetonitrile" → Acetonitril, aber "Methanol" ≠ Ethanol, "Natriumhydroxid" ≠ Natrium).
     */
    fun findByName(lines: List<String>): List<Hit> {
        val hits = mutableListOf<Hit>()
        lines.forEachIndexed { li, line ->
            val words = line.split(SPLIT).map(::norm).filter { it.isNotEmpty() }
            for (start in words.indices) {
                var joined = ""
                for (end in start until minOf(words.size, start + 5)) {
                    joined += words[end]
                    for (c in all) for (k in c.keys) {
                        if (joined.startsWith(k) && joined.length - k.length <= 3 &&
                            (joined.length == k.length || !joined[k.length].isDigit())) {
                            hits.add(Hit(c, k.length, li))
                        }
                    }
                }
            }
        }
        // längster Treffer gewinnt bei gleicher Zeile, sonst die wichtigere Zeile
        return hits.sortedWith(compareBy<Hit> { it.lineIndex }.thenByDescending { it.keyLength })
            .distinctBy { it.chem.name }
    }

    /** CAS-Nummern im Text finden (mit Prüfziffer) und zuordnen. */
    fun findByCas(allText: String): List<Chem> {
        val out = mutableListOf<Chem>()
        for (m in CAS_FIND.findAll(allText)) {
            val cas = "${m.groupValues[1]}-${m.groupValues[2]}-${m.groupValues[3]}"
            if (!casValid(cas)) continue
            all.firstOrNull { cas in it.cas }?.let { if (it !in out) out.add(it) }
        }
        return out
    }

    fun casValid(cas: String): Boolean {
        val digits = cas.filter { it.isDigit() }
        if (digits.length < 5) return false
        val check = digits.last().digitToInt()
        val body = digits.dropLast(1).reversed()
        var sum = 0
        body.forEachIndexed { i, ch -> sum += (i + 1) * ch.digitToInt() }
        return sum % 10 == check
    }
}
