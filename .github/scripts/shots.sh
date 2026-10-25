#!/bin/bash
mkdir -p shots
APK=$(ls app/build/outputs/apk/debug/*.apk | head -1)
P=com.example.productivitylauncher
adb install -r "$APK"
adb shell wm size
shot(){ sleep "${2:-2}"; adb exec-out screencap -p > "shots/$1.png"; }
# seed state before first launch
cat > /tmp/launcher.xml <<'X'
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
<boolean name="onboarded" value="true" />
<string name="tasks">[{"id":"1","text":"Read chapter 3","done":true},{"id":"2","text":"Email Prof. Rao","done":true},{"id":"3","text":"Finish chemistry chapter 4","done":false},{"id":"4","text":"Physics problem sheet","done":false},{"id":"5","text":"Plan revision","done":false}]</string>
<string name="home_apps">["com.android.settings","com.android.chrome","com.android.contacts","com.android.camera2"]</string>
<set name="gated"><string>com.android.chrome</string></set>
</map>
X
adb shell am start -n $P/.MainActivity; sleep 3; adb shell am force-stop $P
adb push /tmp/launcher.xml /data/local/tmp/launcher.xml
adb shell "run-as $P sh -c 'mkdir -p shared_prefs && cp /data/local/tmp/launcher.xml shared_prefs/launcher.xml'"
adb shell cmd package set-home-activity $P/.MainActivity
adb shell input keyevent 3; sleep 1
adb shell am start -n $P/.MainActivity
shot 01_home 7
adb shell input swipe 900 1200 150 1200 300
shot 02_today 3
adb shell input swipe 540 1900 540 700 400
shot 03_today_scrolled 2
adb shell input swipe 540 700 540 1900 400
adb shell input swipe 540 1200 540 1900 400
adb shell input swipe 150 1200 900 1200 300
sleep 2
adb shell input swipe 540 1700 540 800 250
shot 04_drawer 3
adb shell input tap 540 400
adb shell input text Chrome
shot 05_drawer_search 2
adb shell input tap 300 560
shot 06_gate 2
shot 07_gate_later 8
adb shell input keyevent 4
adb shell input keyevent 4
adb shell input keyevent 3
sleep 2
adb shell input tap 300 200
shot 08_quick 3
adb shell uiautomator dump /sdcard/ui.xml; adb pull /sdcard/ui.xml shots/ui.xml
adb logcat -d -s AndroidRuntime:E > shots/crash.txt
