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
Natriumdodecylsulfat|Sodium dodecyl sulfate|Natriumlaurylsulfat|151-21-3
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
Acetophenon|Acetophenone|98-86-2
Acrylsäure|Acrylic acid|79-10-7
Acrylnitril|Acrylonitrile|107-13-1
Adipinsäure|Adipic acid|124-04-9
Allylalkohol|Allyl alcohol|107-18-6
Aluminiumchlorid|Aluminium chloride|Aluminum chloride|7446-70-0
Aluminiumoxid|Aluminium oxide|Aluminum oxide|1344-28-1
Aluminiumsulfat|Aluminium sulfate|10043-01-3
Ammoniumcarbonat|Ammonium carbonate|506-87-6
Ammoniumhydrogencarbonat|Ammonium bicarbonate|Ammonium hydrogen carbonate|1066-33-7
Ammoniumsulfat|Ammonium sulfate|7783-20-2
Ammoniumperoxodisulfat|Ammoniumpersulfat|Ammonium persulfate|Ammonium peroxodisulfate|APS|7727-54-0
Ammoniumheptamolybdat|Ammoniummolybdat|Ammonium molybdate|12054-85-2|12027-67-7
Ammoniumthiocyanat|Ammoniumrhodanid|Ammonium thiocyanate|1762-95-4
Ammoniumfluorid|Ammonium fluoride|12125-01-8
Ammoniumeisen(II)-sulfat|Mohrsches Salz|Ammonium iron(II) sulfate|7783-85-9
Ammoniumdichromat|Ammonium dichromate|7789-09-5
Anisol|Anisole|Methoxybenzol|100-66-3
Anthracen|Anthracene|120-12-7
Antimon(III)-chlorid|Antimontrichlorid|Antimony trichloride|10025-91-9
Arsen(III)-oxid|Arsentrioxid|Arsenic trioxide|1327-53-3
Bariumhydroxid|Barium hydroxide|17194-00-2|12230-71-6
Bariumnitrat|Barium nitrate|10022-31-8
Bariumsulfat|Barium sulfate|7727-43-7
Benzylalkohol|Benzyl alcohol|100-51-6
Benzylchlorid|Benzyl chloride|100-44-7
Benzoylchlorid|Benzoyl chloride|98-88-4
Dibenzoylperoxid|Benzoylperoxid|Benzoyl peroxide|94-36-0
Benzonitril|Benzonitrile|100-47-0
Biphenyl|Diphenyl|92-52-4
Bismut(III)-nitrat|Wismutnitrat|Bismuth(III) nitrate|10361-44-1|10035-06-0
Bisphenol A|80-05-7
Brom|Bromine|7726-95-6
Bromwasserstoffsäure|Hydrobromic acid|10035-10-6
Bromoform|Tribrommethan|Tribromomethane|75-25-2
1-Brombutan|1-Bromobutane|n-Butylbromid|109-65-9
Bromphenolblau|Bromophenol blue|115-39-9
Bromkresolgrün|Bromocresol green|76-60-8
n-Butylamin|Butylamine|109-73-9
2-Butoxyethanol|Butylglykol|Ethylene glycol monobutyl ether|111-76-2
gamma-Butyrolacton|Butyrolacton|gamma-Butyrolactone|GBL|96-48-0
Buttersäure|Butyric acid|Butansäure|107-92-6
Cadmiumchlorid|Cadmium chloride|10108-64-2
Cadmiumsulfat|Cadmium sulfate|10124-36-4
Caesiumchlorid|Cäsiumchlorid|Cesium chloride|Caesium chloride|7647-17-8
Calciumoxid|Branntkalk|Calcium oxide|1305-78-8
Calciumsulfat|Gips|Calcium sulfate|7778-18-9|10101-41-4
Calciumnitrat|Calcium nitrate|10124-37-5|13477-34-4
Calciumcarbid|Calcium carbide|75-20-7
Calciumfluorid|Calcium fluoride|7789-75-5
Cer(IV)-sulfat|Cerium(IV) sulfate|13590-82-4
Chloralhydrat|Chloral hydrate|302-17-0
Chlorsulfonsäure|Chlorosulfonic acid|7790-94-5
Chrom(III)-chlorid|Chromchlorid|Chromium(III) chloride|10025-73-7|10060-12-5
Chrom(VI)-oxid|Chromtrioxid|Chromium trioxide|Chromium(VI) oxide|1333-82-0
Chrom(III)-nitrat|Chromium(III) nitrate|13548-38-4|7789-02-8
Cobalt(II)-nitrat|Cobaltnitrat|Cobalt(II) nitrate|10141-05-6|10026-22-9
Cobalt(II)-sulfat|Cobaltsulfat|Cobalt(II) sulfate|10124-43-3
Kupfer(II)-chlorid|Kupferchlorid|Copper(II) chloride|7447-39-4|10125-13-0
Kupfer(II)-nitrat|Kupfernitrat|Copper(II) nitrate|3251-23-8
Kupfer(II)-oxid|Kupferoxid|Copper(II) oxide|1317-38-0
Kupfer(I)-chlorid|Copper(I) chloride|7758-89-6
Kupfer(II)-acetat|Kupferacetat|Copper(II) acetate|142-71-2
Cumol|Isopropylbenzol|Cumene|98-82-8
Cyclohexanol|108-93-0
Cyclohexen|Cyclohexene|110-83-8
Cyclopentan|Cyclopentane|287-92-3
n-Decan|Decan|Decane|124-18-5
Diacetonalkohol|Diacetone alcohol|123-42-2
1,2-Dichlorbenzol|1,2-Dichlorobenzene|o-Dichlorbenzol|95-50-1
1,4-Dichlorbenzol|1,4-Dichlorobenzene|p-Dichlorbenzol|106-46-7
Dicyclohexylcarbodiimid|N,N'-Dicyclohexylcarbodiimide|DCC|538-75-0
Diethylenglycol|Diethylenglykol|Diethylene glycol|111-46-6
Diethylphthalat|Diethyl phthalate|84-66-2
Diisopropylether|Diisopropyl ether|108-20-3
N,N-Diisopropylethylamin|DIPEA|Hünig-Base|N,N-Diisopropylethylamine|7087-68-5
N,N-Dimethylacetamid|Dimethylacetamid|N,N-Dimethylacetamide|DMAc|127-19-5
4-(Dimethylamino)pyridin|DMAP|4-Dimethylaminopyridine|1122-58-3
Dimethylsulfat|Dimethyl sulfate|77-78-1
Dimethylcarbonat|Dimethyl carbonate|616-38-6
1,2-Dimethoxyethan|1,2-Dimethoxyethane|Monoglyme|DME|110-71-4
2,4-Dinitrophenylhydrazin|2,4-Dinitrophenylhydrazine|DNPH|119-26-6
Diphenylamin|Diphenylamine|122-39-4
Dithiothreitol|DTT|3483-12-3
n-Dodecan|Dodecan|Dodecane|112-40-3
Eisen(III)-nitrat|Eisennitrat|Iron(III) nitrate|Ferric nitrate|10421-48-4|7782-61-8
Eisen(III)-sulfat|Iron(III) sulfate|10028-22-5
Eisen(II)-chlorid|Iron(II) chloride|7758-94-3|13478-10-9
Epichlorhydrin|Epichlorohydrin|106-89-8
Ethanolamin|2-Aminoethanol|Monoethanolamin|Ethanolamine|141-43-5
Diethanolamin|Diethanolamine|111-42-2
Triethanolamin|Triethanolamine|102-71-6
2-Ethoxyethanol|Ethylglykol|110-80-5
2-Methoxyethanol|Methylglykol|109-86-4
Ethylformiat|Ethyl formate|Ameisensäureethylester|109-94-4
Ethylenoxid|Ethylene oxide|75-21-8
Fluorescein|2321-07-5
Furfural|Furan-2-carbaldehyd|98-01-1
Gallussäure|Gallic acid|149-91-7
Glycin|Glycine|56-40-6
Guanidinhydrochlorid|Guanidine hydrochloride|Guanidiniumchlorid|50-01-1
Guanidinthiocyanat|Guanidine thiocyanate|Guanidiniumthiocyanat|593-84-0
HEPES|7365-45-9
Hexamethylentetramin|Urotropin|Hexamine|Methenamine|100-97-0
1-Hexanol|Hexanol|111-27-3
Hexamethyldisilazan|HMDS|Hexamethyldisilazane|999-97-3
Imidazol|Imidazole|288-32-4
Iodmethan|Methyliodid|Iodomethane|Methyl iodide|74-88-4
Isoamylalkohol|3-Methyl-1-butanol|Isopentylalkohol|Isoamyl alcohol|123-51-3
Isopropylacetat|Isopropyl acetate|108-21-4
Kaliumbromat|Potassium bromate|7758-01-2
Kaliumchlorat|Potassium chlorate|3811-04-9
Kaliumperchlorat|Potassium perchlorate|7778-74-7
Kaliumhexacyanoferrat(II)|Gelbes Blutlaugensalz|Kaliumferrocyanid|Potassium hexacyanoferrate(II)|Potassium ferrocyanide|14459-95-1|13943-58-3
Kaliumhexacyanoferrat(III)|Rotes Blutlaugensalz|Kaliumferricyanid|Potassium hexacyanoferrate(III)|Potassium ferricyanide|13746-66-2
Kaliumhydrogencarbonat|Potassium hydrogen carbonate|Potassium bicarbonate|298-14-6
Kaliumhydrogenphthalat|Potassium hydrogen phthalate|KHP|877-24-7
Kaliumiodat|Potassium iodate|7758-05-6
Kaliumnatriumtartrat|Seignettesalz|Potassium sodium tartrate|Rochelle salt|6381-59-5|304-59-6
Kaliumperoxodisulfat|Kaliumpersulfat|Potassium persulfate|7727-21-1
Kaliumsulfat|Potassium sulfate|7778-80-5
Kaliumthiocyanat|Kaliumrhodanid|Potassium thiocyanate|333-20-0
Kaliumfluorid|Potassium fluoride|7789-23-3
Kaliumacetat|Potassium acetate|127-08-2
Kalium-tert-butanolat|Kalium-tert-butylat|Potassium tert-butoxide|865-47-4
Lithiumchlorid|Lithium chloride|7447-41-8
Lithiumhydroxid|Lithium hydroxide|1310-65-2
Lithiumcarbonat|Lithium carbonate|554-13-2
Lithiumbromid|Lithium bromide|7550-35-8
n-Butyllithium|Butyllithium|n-Butyllithium solution|109-72-8
Magnesiumoxid|Magnesium oxide|1309-48-4
Magnesiumnitrat|Magnesium nitrate|10377-60-3|13446-18-9
Maleinsäureanhydrid|Maleic anhydride|108-31-6
Malonsäure|Malonic acid|141-82-2
Mangan(II)-chlorid|Manganchlorid|Manganese(II) chloride|7773-01-5
Mangan(II)-sulfat|Mangansulfat|Manganese(II) sulfate|7785-87-7
Mangan(IV)-oxid|Braunstein|Mangandioxid|Manganese dioxide|1313-13-9
2-Mercaptoethanol|beta-Mercaptoethanol|2-Mercaptoethanol|60-24-2
Methacrylsäure|Methacrylic acid|79-41-4
Methylmethacrylat|Methyl methacrylate|MMA|80-62-6
Methylcyclohexan|Methylcyclohexane|108-87-2
Methylrot|Methyl red|493-52-7
Milchsäure|Lactic acid|50-21-5
Murexid|Murexide|3051-09-0
Naphthalin|Naphthalene|91-20-3
1-Naphthol|alpha-Naphthol|90-15-3
2-Naphthol|beta-Naphthol|135-19-3
Natriumbenzoat|Sodium benzoate|532-32-1
Natriumhydrogensulfit|Natriumbisulfit|Sodium bisulfite|Sodium hydrogen sulfite|7631-90-5
Natriumdisulfit|Natriummetabisulfit|Sodium metabisulfite|Sodium disulfite|7681-57-4
Natriumchlorat|Sodium chlorate|7775-09-9
Trinatriumcitrat|Natriumcitrat|Sodium citrate|Trisodium citrate|68-04-2|6132-04-3
Natriumformiat|Sodium formate|141-53-7
Natriumhydrid|Sodium hydride|7646-69-7
Natriumiodid|Sodium iodide|7681-82-5
Natriummethanolat|Natriummethylat|Sodium methoxide|124-41-4
Natriummolybdat|Sodium molybdate|7631-95-0|10102-40-6
Natriumoxalat|Sodium oxalate|62-76-0
Natriumperchlorat|Sodium perchlorate|7601-89-0
Natriumperoxid|Sodium peroxide|1313-60-6
Trinatriumphosphat|Natriumphosphat|Trisodium phosphate|7601-54-9
Natriumsilikat|Wasserglas|Natronwasserglas|Sodium silicate|1344-09-8
Natriumsulfid|Sodium sulfide|1313-82-2
Natriumsulfit|Sodium sulfite|7757-83-7
Natriumtetraborat|Borax|Dinatriumtetraborat|Sodium tetraborate|1303-96-4|1330-43-4
Natriumwolframat|Sodium tungstate|10213-10-2
Natriumhydrogensulfat|Sodium hydrogen sulfate|Sodium bisulfate|7681-38-1
Natriumcyanoborhydrid|Sodium cyanoborohydride|25895-60-7
Nickel(II)-chlorid|Nickelchlorid|Nickel(II) chloride|7718-54-9|7791-20-0
Nickel(II)-nitrat|Nickelnitrat|Nickel(II) nitrate|13138-45-9|13478-00-7
Nitromethan|Nitromethane|75-52-5
4-Nitrophenol|p-Nitrophenol|4-Nitrophenol|100-02-7
n-Octan|Octan|Octane|111-65-9
1-Octanol|Octanol|111-87-5
Ölsäure|Oleic acid|112-80-1
Osmiumtetroxid|Osmium tetroxide|Osmium(VIII)-oxid|20816-12-0
Oxalylchlorid|Oxalyl chloride|79-37-8
Palladium(II)-chlorid|Palladiumchlorid|Palladium(II) chloride|7647-10-1
Palladium auf Aktivkohle|Palladium on carbon|Pd/C|7440-05-3
Paraformaldehyd|Paraformaldehyde|30525-89-4
1-Pentanol|Pentanol|n-Amylalkohol|71-41-0
Peressigsäure|Peroxyessigsäure|Peracetic acid|79-21-0
Phenylhydrazin|Phenylhydrazine|100-63-0
Phosphorpentoxid|Phosphor(V)-oxid|Phosphorus pentoxide|1314-56-3
Phosphorpentachlorid|Phosphorus pentachloride|10026-13-8
Phosphoroxychlorid|Phosphorylchlorid|Phosphorus oxychloride|10025-87-3
Phosphortrichlorid|Phosphorus trichloride|7719-12-2
Phthalsäureanhydrid|Phthalic anhydride|85-44-9
Pikrinsäure|Picric acid|2,4,6-Trinitrophenol|88-89-1
1,2-Propandiol|Propylenglycol|Propylenglykol|Propylene glycol|57-55-6
Propylencarbonat|Propylene carbonate|108-32-7
Resorcin|Resorcinol|108-46-3
Salicylaldehyd|Salicylaldehyde|90-02-8
Schwefelkohlenstoff|Kohlenstoffdisulfid|Carbon disulfide|75-15-0
Silberchlorid|Silver chloride|7783-90-6
Silbersulfat|Silver sulfate|10294-26-5
Stearinsäure|Stearic acid|57-11-4
Strontiumchlorid|Strontium chloride|10476-85-4
Strontiumnitrat|Strontium nitrate|10042-76-9
Sulfanilsäure|Sulfanilic acid|121-57-3
Amidosulfonsäure|Sulfaminsäure|Sulfamic acid|5329-14-6
Tetrabutylammoniumbromid|Tetrabutylammonium bromide|TBAB|1643-19-2
Tetraethylorthosilicat|Tetraethoxysilan|Tetraethyl orthosilicate|TEOS|78-10-4
Tetramethylsilan|Tetramethylsilane|75-76-3
N,N,N',N'-Tetramethylethylendiamin|TEMED|Tetramethylethylenediamine|110-18-9
Thioacetamid|Thioacetamide|62-55-5
Thymol|89-83-8
Thymolblau|Thymol blue|76-61-9
Titan(IV)-chlorid|Titantetrachlorid|Titanium tetrachloride|7550-45-0
Titandioxid|Titanium dioxide|13463-67-7
p-Toluolsulfonsäure|Toluol-4-sulfonsäure|p-Toluenesulfonic acid|Tosylsäure|104-15-4|6192-52-5
Tributylphosphat|Tributyl phosphate|126-73-8
Triphenylphosphin|Triphenylphosphine|603-35-0
Triton X-100|9002-93-1
Polysorbat 20|Tween 20|9005-64-5
Uranylacetat|Uranyl acetate|541-09-3|6159-44-0
Vanadium(V)-oxid|Vanadiumpentoxid|Vanadium pentoxide|1314-62-1
Zinkacetat|Zinc acetate|557-34-6|5970-45-6
Zinknitrat|Zinc nitrate|7779-88-6|10196-18-6
Zinkoxid|Zinc oxide|1314-13-2
Zinn(II)-chlorid|Zinnchlorid|Tin(II) chloride|Stannous chloride|7772-99-8|10025-69-1
Zinn(IV)-chlorid|Tin(IV) chloride|7646-78-8
Blei(II)-acetat|Bleiacetat|Lead(II) acetate|301-04-2|6080-56-4
Blei(II)-oxid|Bleioxid|Lead(II) oxide|1317-36-8
Quecksilber(II)-nitrat|Mercury(II) nitrate|10045-94-0
Quecksilber(II)-oxid|Mercury(II) oxide|21908-53-2
Quecksilber(II)-sulfat|Mercury(II) sulfate|7783-35-9
Agarose|9012-36-6
Coomassie Brillantblau R-250|Coomassie Brilliant Blue R-250|6104-59-2
Coomassie Brillantblau G-250|Coomassie Brilliant Blue G-250|6104-58-1
Eriochromschwarz T|Eriochrome Black T|1787-61-7
3,3'-Diaminobenzidin|Diaminobenzidin|3,3'-Diaminobenzidine|91-95-2
Sudan III|85-86-9
Safranin O|Safranin|477-73-6
Hämatoxylin|Hematoxylin|517-28-2
Trimethylchlorsilan|Chlortrimethylsilan|Chlorotrimethylsilane|75-77-4
Natriumnitroprussid|Sodium nitroprusside|13755-38-9
Terpentinöl|Turpentine oil|8006-64-2
L-Alanin|Alanin|L-Alanine|Alanine|56-41-7
L-Arginin|Arginin|L-Arginine|Arginine|74-79-3
L-Asparagin|Asparagin|L-Asparagine|70-47-3
L-Asparaginsäure|Asparaginsäure|L-Aspartic acid|Aspartic acid|56-84-8
L-Cystein|Cystein|L-Cysteine|Cysteine|52-90-4
L-Glutamin|Glutamin|L-Glutamine|Glutamine|56-85-9
L-Glutaminsäure|Glutaminsäure|L-Glutamic acid|Glutamic acid|56-86-0
L-Histidin|Histidin|L-Histidine|Histidine|71-00-1
L-Isoleucin|Isoleucin|L-Isoleucine|Isoleucine|73-32-5
L-Leucin|Leucin|L-Leucine|Leucine|61-90-5
L-Lysin|Lysin|L-Lysine|Lysine|56-87-1|657-27-2
L-Methionin|Methionin|L-Methionine|Methionine|63-68-3
L-Phenylalanin|Phenylalanin|L-Phenylalanine|Phenylalanine|63-91-2
L-Prolin|Prolin|L-Proline|Proline|147-85-3
L-Serin|Serin|L-Serine|Serine|56-45-1
L-Threonin|Threonin|L-Threonine|Threonine|72-19-5
L-Tryptophan|Tryptophan|73-22-3
L-Tyrosin|Tyrosin|L-Tyrosine|Tyrosine|60-18-4
L-Valin|Valin|L-Valine|Valine|72-18-4
D-Fructose|Fructose|Fruchtzucker|57-48-7
D-Galactose|Galactose|59-23-4
Lactose|Laktose|Milchzucker|63-42-3|64044-51-5
Maltose|69-79-4
D-Mannose|Mannose|3458-28-4
D-Sorbit|Sorbitol|D-Sorbitol|50-70-4
D-Mannit|Mannitol|D-Mannitol|69-65-8
Trehalose|D-Trehalose|99-20-7
D-Xylose|Xylose|58-86-6
D-Ribose|Ribose|50-69-1
Stärke|Starch|Kartoffelstärke|9005-25-8
Cellulose|Zellulose|9004-34-6
Dextran|9004-54-0
Chitosan|9012-76-4
Agar|Agar-Agar|9002-18-0
Adenosin-5'-triphosphat|ATP|Adenosine 5'-triphosphate|987-65-5
beta-NAD|NAD|Nicotinamidadenindinukleotid|Nicotinamide adenine dinucleotide|53-84-9
NADH|NADH-Dinatriumsalz|606-68-8
NADP|NADP-Dinatriumsalz|24292-60-2
NADPH|NADPH-Tetranatriumsalz|2646-71-1
Coenzym A|Coenzyme A|85-61-0
Adenin|Adenine|73-24-5
Guanin|Guanine|73-40-5
Cytosin|Cytosine|71-30-7
Thymin|Thymine|65-71-4
Uracil|66-22-8
Riboflavin|Vitamin B2|83-88-5
Thiaminhydrochlorid|Thiamine hydrochloride|Vitamin B1|67-03-8
Nicotinamid|Nicotinamide|Niacinamid|98-92-0
Nicotinsäure|Nicotinic acid|Niacin|59-67-6
Pyridoxinhydrochlorid|Pyridoxine hydrochloride|Vitamin B6|58-56-0
Biotin|Vitamin H|58-85-5
Folsäure|Folic acid|59-30-3
Cyanocobalamin|Vitamin B12|68-19-9
Retinol|Vitamin A|68-26-8
alpha-Tocopherol|Tocopherol|Vitamin E|59-02-9
Ergocalciferol|Vitamin D2|50-14-6
Cholecalciferol|Vitamin D3|67-97-0
Menadion|Menadione|Vitamin K3|58-27-5
Rinderserumalbumin|Bovine serum albumin|Albumin Fraktion V|BSA|9048-46-8
MOPS|3-(N-Morpholino)propansulfonsäure|3-(N-Morpholino)propanesulfonic acid|1132-61-2
MES|2-(N-Morpholino)ethansulfonsäure|2-(N-Morpholino)ethanesulfonic acid|4432-31-9|145224-94-8
PIPES|5625-37-6
Bicin|Bicine|150-25-4
Tricin|Tricine|5704-04-1
CAPS|1135-40-6
TAPS|29915-38-6
Tris-Hydrochlorid|Tris-HCl|Tris hydrochloride|1185-53-1
Bis-Tris|6976-37-0
CHAPS|75621-03-3
Polysorbat 80|Tween 80|9005-65-6
Glutathion|Glutathione|L-Glutathion reduziert|70-18-8
Lysozym|Lysozyme|12650-88-3
Trypsin|9002-07-7
Pepsin|9001-75-6
Proteinase K|39450-01-6
Phenylmethylsulfonylfluorid|PMSF|Phenylmethanesulfonyl fluoride|329-98-6
IPTG|Isopropyl-beta-D-thiogalactopyranosid|Isopropyl beta-D-1-thiogalactopyranoside|367-93-1
X-Gal|5-Brom-4-chlor-3-indolyl-beta-D-galactopyranosid|7240-90-6
Ampicillin-Natriumsalz|Ampicillin|Ampicillin sodium salt|69-52-3
Kanamycinsulfat|Kanamycin|Kanamycin sulfate|25389-94-0
Chloramphenicol|56-75-7
Tetracyclinhydrochlorid|Tetracyclin|Tetracycline hydrochloride|64-75-5
Streptomycinsulfat|Streptomycin|Streptomycin sulfate|3810-74-0
Penicillin G Kalium|Benzylpenicillin-Kalium|Penicillin G potassium salt|113-98-4
Gentamicinsulfat|Gentamicin|Gentamicin sulfate|1405-41-0
Hygromycin B|31282-04-9
Cycloheximid|Cycloheximide|66-81-9
Rifampicin|13292-46-1
Erythromycin|114-07-8
Vancomycinhydrochlorid|Vancomycin|1404-93-9
Polymyxin B-sulfat|Polymyxin B|1405-20-5
Nystatin|1400-61-9
Amphotericin B|1397-89-3
Spermidin|Spermidine|124-20-9
Spermin|Spermine|71-44-3
Putrescin|Putrescine|1,4-Diaminobutan|110-60-1
Cadaverin|Cadaverine|1,5-Diaminopentan|462-94-2
Taurin|Taurine|107-35-7
Betain|Betaine|107-43-7
Kreatin|Creatine|57-00-1
Koffein|Coffein|Caffeine|58-08-2
Nikotin|Nicotine|54-11-5
Ibuprofen|15687-27-1
Paracetamol|Acetaminophen|103-90-2
Acetylsalicylsäure|Acetylsalicylic acid|Aspirin|50-78-2
Cholesterin|Cholesterol|57-88-5
Lecithin|8002-43-5
DAPI|4',6-Diamidin-2-phenylindol|28718-90-3
Propidiumiodid|Propidium iodide|25535-16-4
Hoechst 33342|23491-52-3
Acridinorange|Acridine orange|65-61-2|494-38-2
Trypanblau|Trypan blue|72-57-1
Neutralrot|Neutral red|553-24-2
Kongorot|Congo red|573-58-0
Alizarin|72-48-0
Malachitgrün|Malachite green|569-64-2|2437-29-8
Bromkresolpurpur|Bromocresol purple|115-40-2
Phenolrot|Phenol red|143-74-8
Kresolrot|Cresol red|1733-12-6
Nilrot|Nile red|7385-67-3
Rhodamin B|Rhodamine B|81-88-9
Toluidinblau|Toluidine blue|92-31-9
Orange G|1936-15-8
Ponceau S|6226-79-5
Bernsteinsäure|Succinic acid|Butandisäure|110-15-6
Fumarsäure|Fumaric acid|110-17-8
Maleinsäure|Maleic acid|110-16-7
Glutarsäure|Glutaric acid|110-94-1
Itaconsäure|Itaconic acid|97-65-4
DL-Äpfelsäure|Äpfelsäure|Malic acid|DL-Malic acid|6915-15-7
Brenztraubensäure|Pyruvic acid|127-17-3
Natriumpyruvat|Sodium pyruvate|113-24-6
Glykolsäure|Glycolic acid|79-14-1
Glyoxylsäure|Glyoxylic acid|298-12-4
Lävulinsäure|Levulinic acid|123-76-2
Zimtsäure|Cinnamic acid|140-10-3
Ferulasäure|Ferulic acid|1135-24-6
Kaffeesäure|Caffeic acid|331-39-5
Vanillin|121-33-5
Eugenol|97-53-0
(R)-(+)-Limonen|Limonen|Limonene|D-Limonen|5989-27-5
Menthol|L-Menthol|89-78-1|2216-51-5
Campher|Kampfer|Camphor|76-22-2
Citral|5392-40-5
Citronellal|106-23-0
Geraniol|106-24-1
Linalool|78-70-6
Brenzcatechin|Catechol|Pyrocatechol|120-80-9
Pyrogallol|87-66-1
Phloroglucin|Phloroglucinol|108-73-6
p-Benzochinon|1,4-Benzochinon|p-Benzoquinone|106-51-4
Anthrachinon|Anthraquinone|84-65-1
Benzophenon|Benzophenone|119-61-9
Benzil|134-81-6
Benzoin|119-53-9
Indol|Indole|120-72-9
Chinolin|Quinoline|91-22-5
Pyrrol|Pyrrole|109-97-7
Thiophen|Thiophene|110-02-1
Furan|110-00-9
Pyrazin|Pyrazine|290-37-9
Pyrimidin|Pyrimidine|289-95-2
Piperazin|Piperazine|110-85-0
DABCO|1,4-Diazabicyclo[2.2.2]octan|1,4-Diazabicyclo[2.2.2]octane|280-57-9
DBU|1,8-Diazabicyclo[5.4.0]undec-7-en|6674-22-2
Tributylamin|Tributylamine|102-82-9
Hexamethylendiamin|1,6-Diaminohexan|Hexamethylenediamine|124-09-4
epsilon-Caprolactam|Caprolactam|105-60-2
Acetamid|Acetamide|60-35-5
Formamid|Formamide|75-12-7
N-Methylformamid|N-Methylformamide|123-39-7
Propionitril|Propionitrile|107-12-0
Benzylamin|Benzylamine|100-46-9
Cyclohexylamin|Cyclohexylamine|108-91-8
Isopropylamin|Isopropylamine|75-31-0
tert-Butylamin|tert-Butylamine|75-64-9
Ethylamin|Ethylamine|75-04-7
Methylamin|Methylamine|74-89-5
Dimethylamin|Dimethylamine|124-40-3
Diisopropylamin|Diisopropylamine|108-18-9
o-Phenylendiamin|1,2-Phenylendiamin|o-Phenylenediamine|95-54-5
p-Phenylendiamin|1,4-Phenylendiamin|p-Phenylenediamine|106-50-3
Benzidin|Benzidine|92-87-5
4-Aminophenol|p-Aminophenol|123-30-8
2-Aminophenol|o-Aminophenol|95-55-6
Sulfanilamid|Sulfanilamide|63-74-1
4-Nitroanilin|p-Nitroanilin|4-Nitroaniline|100-01-6
Bromethan|Ethylbromid|Bromoethane|74-96-4
Iodethan|Ethyliodid|Iodoethane|75-03-6
1-Chlorbutan|1-Chlorobutane|109-69-3
Benzylbromid|Benzyl bromide|100-39-0
Allylbromid|Allyl bromide|106-95-6
Dibrommethan|Dibromomethane|74-95-3
Diiodmethan|Diiodomethane|75-11-6
1,2-Dibromethan|1,2-Dibromoethane|106-93-4
Hexafluorisopropanol|1,1,1,3,3,3-Hexafluor-2-propanol|HFIP|920-66-1
2,2,2-Trifluorethanol|Trifluorethanol|2,2,2-Trifluoroethanol|TFE|75-89-8
Perfluorhexan|Tetradecafluorhexan|Perfluorohexane|355-42-0
Fluorbenzol|Fluorobenzene|462-06-6
Hexachlorethan|Hexachloroethane|67-72-1
(3-Aminopropyl)triethoxysilan|APTES|(3-Aminopropyl)triethoxysilane|919-30-2
(3-Glycidyloxypropyl)trimethoxysilan|GPTMS|(3-Glycidyloxypropyl)trimethoxysilane|2530-83-8
Triethylsilan|Triethylsilane|617-86-7
Tetramethylorthosilicat|TMOS|Tetramethyl orthosilicate|681-84-5
Dichlordimethylsilan|Dichlorodimethylsilane|75-78-5
Hexamethyldisiloxan|Hexamethyldisiloxane|HMDSO|107-46-0
Polydimethylsiloxan|PDMS|Silikonöl|Polydimethylsiloxane|63148-62-9
Tetrakis(triphenylphosphin)palladium(0)|Pd(PPh3)4|Tetrakis(triphenylphosphine)palladium(0)|14221-01-3
Palladium(II)-acetat|Palladiumacetat|Palladium(II) acetate|3375-31-3
Kupfer(I)-iodid|Kupferiodid|Copper(I) iodide|7681-65-4
Silber(I)-oxid|Silberoxid|Silver(I) oxide|20667-12-3
Tetrachlorogoldsäure|Gold(III)-chlorid|Gold(III) chloride|Chloroauric acid|16961-25-4|13453-07-1
Hexachloroplatinsäure|Hexachloroplatinic acid|16941-12-1
Ruthenium(III)-chlorid|Rutheniumchlorid|Ruthenium(III) chloride|10049-08-8
Diisobutylaluminiumhydrid|DIBAL-H|Diisobutylaluminum hydride|1191-15-7
Natriumtriacetoxyborhydrid|Sodium triacetoxyborohydride|56553-60-7
Boran-Tetrahydrofuran-Komplex|Borane tetrahydrofuran complex|14044-65-6
Tetrabutylammoniumfluorid|TBAF|Tetrabutylammonium fluoride|429-41-4
Lithiumdiisopropylamid|LDA|Lithium diisopropylamide|4111-54-0
N-Bromsuccinimid|NBS|N-Bromosuccinimide|128-08-5
N-Chlorsuccinimid|NCS|N-Chlorosuccinimide|128-09-6
N-Iodsuccinimid|NIS|N-Iodosuccinimide|516-12-1
Succinimid|Succinimide|123-56-8
3-Chlorperbenzoesäure|mCPBA|3-Chloroperbenzoic acid|937-14-4
2,3-Dichlor-5,6-dicyano-1,4-benzochinon|DDQ|84-58-2
TEMPO|2,2,6,6-Tetramethylpiperidinyloxyl|2564-83-2
Dess-Martin-Periodinan|Dess-Martin periodinane|87413-09-0
Oxone|Kaliumperoxomonosulfat|Potassium peroxymonosulfate|70693-62-8
Natriumperiodat|Sodium periodate|7790-28-5
Periodsäure|Periodic acid|10450-60-9
Trifluormethansulfonsäure|Triflic acid|Trifluoromethanesulfonic acid|1493-13-6
Trifluormethansulfonsäureanhydrid|Triflic anhydride|Trifluoromethanesulfonic anhydride|358-23-6
Methansulfonsäure|Methanesulfonic acid|75-75-2
Methansulfonylchlorid|Mesylchlorid|Methanesulfonyl chloride|124-63-0
p-Toluolsulfonylchlorid|Tosylchlorid|p-Toluenesulfonyl chloride|98-59-9
Di-tert-butyldicarbonat|Boc-Anhydrid|Di-tert-butyl dicarbonate|Boc2O|24424-99-5
Fmoc-Chlorid|Fmoc-Cl|9-Fluorenylmethyl chloroformate|28920-43-6
EDC-Hydrochlorid|EDC|EDCI|1-Ethyl-3-(3-dimethylaminopropyl)carbodiimide hydrochloride|25952-53-8
HOBt|1-Hydroxybenzotriazol|1-Hydroxybenzotriazole|2592-95-2|123333-53-9
HATU|148893-10-1
N,N'-Carbonyldiimidazol|CDI|1,1'-Carbonyldiimidazole|530-62-1
Triethylorthoformiat|Triethyl orthoformate|122-51-0
Trimethylorthoformiat|Trimethyl orthoformate|149-73-5
1-Butyl-3-methylimidazoliumchlorid|BMIM Cl|1-Butyl-3-methylimidazolium chloride|79917-90-1
1-Butyl-3-methylimidazoliumtetrafluoroborat|BMIM BF4|1-Butyl-3-methylimidazolium tetrafluoroborate|174501-65-6
1-Ethyl-3-methylimidazoliumacetat|EMIM OAc|1-Ethyl-3-methylimidazolium acetate|143314-17-4
Polyvinylpyrrolidon|PVP|Povidon|Polyvinylpyrrolidone|9003-39-8
Polyvinylalkohol|PVA|Polyvinyl alcohol|9002-89-5
Polyacrylamid|Polyacrylamide|9003-05-8
Polystyrol|Polystyrene|9003-53-6
Carboxymethylcellulose|CMC|Carboxymethylcellulose sodium|9004-32-4
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

    class Hit(val chem: Chem, val keyLength: Int, val lineIndex: Int, val exact: Boolean = true)

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
                            hits.add(Hit(c, k.length, li, exact = true))
                        } else if (k.length >= 8 && joined.length >= k.length - 2 && joined.length <= k.length + 5 &&
                            joined[0] == k[0] && joined[1] == k[1]) {
                            // lange Namen: ein paar falsch gelesene Buchstaben erlauben
                            val maxD = k.length / 8
                            var best = Int.MAX_VALUE
                            for (len in (k.length - maxD)..minOf(joined.length, k.length + maxD)) {
                                if (len <= 0) continue
                                best = minOf(best, lev(joined.substring(0, len), k))
                            }
                            if (best <= maxD) hits.add(Hit(c, k.length, li, exact = false))
                        }
                    }
                }
            }
        }
        // längster Treffer gewinnt bei gleicher Zeile, sonst die wichtigere Zeile
        return hits.sortedWith(compareBy<Hit> { it.lineIndex }.thenBy { if (it.exact) 0 else 1 }.thenByDescending { it.keyLength })
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

    private fun lev(a: String, b: String): Int {
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(cur[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            val t = prev; prev = cur; cur = t
        }
        return prev[b.length]
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
