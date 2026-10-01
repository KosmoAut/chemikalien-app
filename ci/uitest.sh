#!/bin/bash
# Läuft im Emulator: Espresso-Tests (Klicks, Löschen, Texterkennung) + Screenshot
mkdir -p shots
adb logcat -c
gradle connectedDebugAndroidTest --no-daemon --continue > shots/test_ausgabe.txt 2>&1
echo "Exit: $?" >> shots/test_ausgabe.txt
adb exec-out screencap -p > shots/ende.png
adb logcat -d -v brief | grep -E "PACKTEST|AndroidRuntime|FATAL|TestRunner" > shots/logcat_test.txt
for f in app/build/outputs/androidTest-results/connected/debug/*.xml app/build/outputs/androidTest-results/connected/*.xml; do [ -f "$f" ] && cp "$f" "shots/$(basename "$f").txt"; done
tail -60 shots/test_ausgabe.txt
true
