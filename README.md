# Zen Garden TV (Mi Box S Gen 2)

## Build the APK (pick ONE)
A. Android Studio (easiest): File > Open this folder, wait for sync, Build > Build APK(s).
   Output: app/build/outputs/apk/debug/app-debug.apk
B. GitHub, nothing to install: upload this folder to a new repo, open Actions > "Build APK" > Run workflow,
   then download the artifact "ZenGarden-apk".
C. Command line: JDK 17 + Android SDK + Gradle 8.7, run: gradle assembleDebug

## Where the game folder goes
The game needs the PNGs in a folder named PVZGARDENVIEW (inside it: reanim/Agave_body.png, ...).
1. AFTER installing (what you asked for): open the app once (it creates the folder), then copy PVZGARDENVIEW to
   /sdcard/Android/data/com.zengarden.tv/files/PVZGARDENVIEW
   e.g.  adb connect <box-ip>
         adb push PVZGARDENVIEW /sdcard/Android/data/com.zengarden.tv/files/
2. BEFORE building: place it at app/src/main/assets/PVZGARDENVIEW (so reanim/ is inside) and build.
