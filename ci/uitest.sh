#!/bin/bash
# UI-Test im Emulator: Foto, Großansicht, Löschen, Etiketten per "Teilen" erkennen.
set -x
mkdir -p shots
U="python3 ci/ui.py"
PKG=at.packliste.app
adb install -r Packliste.apk
adb shell pm grant $PKG android.permission.CAMERA
adb logcat -c
adb shell am start -n $PKG/.MainActivity
sleep 12
$U shot 01_start; $U texts 01_start

# 1) Foto mit der Emulator-Kamera
$U tap_id capture; sleep 8
$U shot 02_nach_foto; $U texts 02_nach_foto

# 2) Vorschaubild antippen -> Großansicht
$U tap_id thumb 0; sleep 3
$U shot 03_foto_antippen; $U texts 03_foto_antippen

# 3) Löschen in der Großansicht -> Rückfrage -> bestätigen
$U tap_text "Löschen"; sleep 2
$U shot 04_rueckfrage; $U texts 04_rueckfrage
$U tap_id button1; sleep 3
$U shot 05_nach_loeschen; $U texts 05_nach_loeschen

# 4) noch ein Foto, dann Zeile antippen (Namen-Dialog)
$U tap_id capture; sleep 8
$U tap_id name 0; sleep 2
$U shot 06_zeile_antippen; $U texts 06_zeile_antippen
adb shell input keyevent KEYCODE_BACK; sleep 1
# lange drücken -> Löschen
$U longpress_id name 0; sleep 2
$U shot 07_lang_druecken; $U texts 07_lang_druecken
$U tap_id button1; sleep 2

# 5) Etikettbilder per Teilen an die App schicken
for L in label_panreac label_aceton; do
  adb push ci/$L.jpg /sdcard/Pictures/$L.jpg
  adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/$L.jpg
  sleep 3
  ID=$(adb shell content query --uri content://media/external/images/media --projection _id:_display_name | grep "$L.jpg" | sed -E 's/.*_id=([0-9]+).*/\1/' | head -1)
  echo "Media-ID $L: $ID"
  adb shell am start -a android.intent.action.SEND -t image/jpeg --eu android.intent.extra.STREAM content://media/external/images/media/$ID --grant-read-uri-permission -n $PKG/.MainActivity
  sleep 10
  $U shot 10_$L; $U texts 10_$L
done
# letzte Zeile antippen -> gelesener Text
$U tap_id name 0; sleep 2
$U shot 11_dialog_erste_zeile; $U texts 11_dialog_erste_zeile
adb shell input keyevent KEYCODE_BACK

adb logcat -d -v brief > shots/logcat_voll.txt
grep -E "AndroidRuntime|packliste|FATAL|Exception" shots/logcat_voll.txt | head -300 > shots/logcat_fehler.txt
true
