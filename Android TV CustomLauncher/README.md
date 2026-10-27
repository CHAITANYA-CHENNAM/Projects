# Acer Custom Launcher

A simple custom Home screen for Android TV: three compact app columns on the left, a full-screen wallpaper behind the interface, and a clock. The right side stays clear of app tiles.

This repository contains the original Android Studio project and an existing debug APK from the author's project archive. The app package is **`com.custom.acertvlauncher`**. It is an independent project, not an official Acer or Google application.

**Quick start:** enable TV debugging → install Android Platform Tools on your computer → connect and authorize ADB → install `prebuilt/app-debug.apk` → open the app → optionally make it Home.

> Developer options alone are not enough: USB debugging, network debugging, or wireless debugging must also be enabled and the computer authorized. The setup was used on an Acer ATV R2 running Android TV 11. Other TVs and firmware may behave differently; universal startup compatibility is not guaranteed. The source declares Android 8.0 / API 26 as its minimum version.

## 1. Download and prepare

1. Download this repository using **Code → Download ZIP**, then extract it.
2. Find the project folder containing this `README.md` and the `prebuilt` directory.
3. Download [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools) for your computer and extract it. You do **not** need Android Studio to install the included APK.
4. Add the Platform Tools directory to your PATH so the following works:

```bash
adb version
```

Alternatively, open a terminal in the Platform Tools folder and use the full path to the APK during installation. In Windows PowerShell, if Platform Tools is not on PATH, use `./adb.exe` instead of `adb` (and use Windows-style paths as appropriate). On macOS/Linux, use `./adb` when running it from its own folder without PATH setup.

**The included APK is a debug/development build copied unchanged from the uploaded project archive, not a newly compiled or store-signed release.** An integrity checksum is provided in `prebuilt/SHA256SUMS.txt`. Only install an APK from a source you trust. Rebuilding on another computer normally uses a different debug signing key; see Troubleshooting before replacing an existing installation.

## 2. Enable debugging on the TV

Menu names vary by manufacturer:

1. Open **Settings → Device Preferences / System → About**.
2. Select **Build / Android TV OS build** repeatedly (usually seven times) until developer mode is enabled. Skip this if already enabled.
3. Open **Developer options** and enable **USB debugging / ADB debugging**.
4. If available, enable **Network debugging / Wireless debugging** too.
5. Connect the TV and computer to the same trusted network. Find the TV IP under its network settings.

Use the connection method your TV actually exposes. Enabling USB debugging does not automatically make port 5555 available on every TV. A USB method requires a device/OTG debugging port supported by the manufacturer; an ordinary TV USB-A media port may not work.

### Option A: network ADB (common on Android TV 11 firmware)

In **all** commands below, replace `TV_IP` with your TV's address. `TV_IP:5555` is a placeholder, not a literal device name. If the TV shows a different debugging port, use that port consistently.

```bash
adb connect TV_IP:5555
adb devices
```

Accept the **Allow USB debugging? / Allow debugging?** prompt on the TV. Despite its name, this authorization prompt can appear for a network connection. The device should show `device`, not `unauthorized` or `offline`.

### Option B: pairing-based wireless debugging

If your TV shows **Pair device with pairing code**, use the pairing address and code shown there:

```bash
adb pair TV_IP:PAIRING_PORT
```

Enter the displayed code when prompted. Then use the **connection port** from the main Wireless debugging screen; it may differ from the pairing port:

```bash
adb connect TV_IP:CONNECTION_PORT
adb devices
```

Replace `TV_IP:5555` in subsequent commands with that connection address.

### Option C: supported USB debugging connection

Use a compatible cable and manufacturer-supported debugging port, then run `adb devices` and authorize the computer. For USB, replace `-s TV_IP:5555` below with `-s DEVICE_SERIAL` from the device list.

## 3. Install the included APK — no build required

Open Terminal, Command Prompt, or PowerShell in this extracted project folder. Confirm that `prebuilt/app-debug.apk` exists.

```bash
adb -s TV_IP:5555 install -r prebuilt/app-debug.apk
```

Wait for **`Success`**. Then open the launcher:

```bash
adb -s TV_IP:5555 shell am start -n com.custom.acertvlauncher/.MainActivity
```

You should see the custom interface. First test D-pad navigation, selection, and opening apps. The grid lists apps with Android TV / Leanback launcher entries; mobile-only or hidden system apps may not appear. A plain background is normal until you supply a wallpaper.

**Do not disable the stock launcher until the custom UI opens and works.** Installing this APK does not itself disable Google or select a default Home app.

## 4. Make the custom interface Home

### First try the normal default setting

If your TV offers **Settings → Apps → Default apps → Home app**, select Acer Custom Launcher. Otherwise try:

```bash
adb -s TV_IP:5555 shell cmd package set-home-activity --user 0 com.custom.acertvlauncher/.MainActivity
```

Press **Home** on the remote. If a chooser appears, select **Acer Custom Launcher → Always**. If this works, keep the stock launcher enabled.

### Acer workaround: Google still opens when Home is pressed

On the author's Acer setup, changing the default preference did not control the Home button. Disabling **only the stock launcher package** allowed the custom UI to handle Home. This is optional and device-specific.

Before using it:

- Keep your authorized computer connected and the restore commands below available.
- Check the current Android user with `adb -s TV_IP:5555 shell am get-current-user`. These instructions assume user **0**. Stop and adapt the user ID if yours differs.
- Confirm the custom app is listed as an eligible Home activity:

```bash
adb -s TV_IP:5555 shell cmd package query-activities --brief --user 0 -a android.intent.action.MAIN -c android.intent.category.HOME
```

The list must contain **`com.custom.acertvlauncher/.MainActivity`**. The stock package in the Acer setup was **`com.google.android.tvlauncher`**. If your TV uses a different package, do not copy this disable command; identify and document the correct launcher and restore procedure first. Do not disable Settings, FallbackHome, Google Play services, or unrelated system packages. `isDefault=true` in the candidate list does not prove an app is currently the preferred Home.

**For the matching Acer/Google TV Launcher setup only:**

```bash
adb -s TV_IP:5555 shell pm disable-user --user 0 com.google.android.tvlauncher
```

Expected output ends with **`new state: disabled-user`**. Google Launcher remains installed, but its interface is unavailable to user 0 until re-enabled.

Press Home **before rebooting**. Test Home again from another app. If Home fails or you see a blank screen, immediately use the restore section below. Recovery through these commands requires working ADB access.

### Test startup and standby separately

Only after Home works:

```bash
adb -s TV_IP:5555 reboot
```

Observe which interface appears after startup. Reconnect ADB when the TV network returns. Then separately test remote Power off/on: standby wake is not necessarily a full reboot. The author's Home-button success does not prove that every firmware's reboot and standby behavior will match.

## 5. Restore the original Google interface

For the Acer package above:

```bash
adb connect TV_IP:5555
adb -s TV_IP:5555 shell pm enable --user 0 com.google.android.tvlauncher
adb -s TV_IP:5555 shell cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity
adb -s TV_IP:5555 shell am start -n com.google.android.tvlauncher/.MainActivity
```

Verify **`new state: enabled`** and that Google Home is visible. Press Home to check the preference. If you used a different stock package, use the matching commands recorded for that device.

**Always restore another working Home app before uninstalling or clearing data for Acer Custom Launcher.** After Google is enabled and works, optional removal is:

```bash
adb -s TV_IP:5555 uninstall com.custom.acertvlauncher
```

Uninstalling deletes the app's private wallpaper. If ADB is inaccessible, these commands cannot perform recovery; do not disable the stock UI unless you have confirmed debugging access.

## 6. Add your own wallpaper (optional)

This version reads **`wallpaper.jpg` from its private `files` directory**. It does not include a wallpaper picker or artwork. For the included debug APK, `run-as` lets ADB write that file without rooting the TV.

Place your image in the current computer folder as `wallpaper.jpg`. A landscape 16:9 JPG is recommended. Run the following from macOS/Linux Terminal or **Windows Command Prompt** (the `<` binary-input command is not PowerShell syntax):

```bash
adb -s TV_IP:5555 shell run-as com.custom.acertvlauncher mkdir -p files
adb -s TV_IP:5555 shell "run-as com.custom.acertvlauncher sh -c 'cat > files/wallpaper.jpg'" < wallpaper.jpg
adb -s TV_IP:5555 shell am start -n com.custom.acertvlauncher/.MainActivity
```

The wallpaper fills the whole screen behind the tiles using `centerCrop`; a different aspect ratio will crop. Large images are downsampled by the app. If the new wallpaper does not appear, press Home again or reopen the app. A custom signed **release** APK may not permit `run-as`; this procedure is for the supplied debuggable build.

To remove the wallpaper:

```bash
adb -s TV_IP:5555 shell run-as com.custom.acertvlauncher rm -f files/wallpaper.jpg
adb -s TV_IP:5555 shell am start -n com.custom.acertvlauncher/.MainActivity
```

## 7. Boot receiver and overlay access (optional)

The existing project declares `RECEIVE_BOOT_COMPLETED` and `SYSTEM_ALERT_WINDOW`. Its boot receiver attempts an immediate activity launch after boot. Android can restrict background activity starts, and the stock launcher can open afterward; the receiver is not a guaranteed default-Home solution.

The Home/default method above does not require an overlay window. If specifically testing the existing boot-receiver approach, open the normal special-access screen:

```bash
adb -s TV_IP:5555 shell am start -a android.settings.action.MANAGE_OVERLAY_PERMISSION -d package:com.custom.acertvlauncher
```

If supported, enable access for this app, then check:

```bash
adb -s TV_IP:5555 shell appops get com.custom.acertvlauncher SYSTEM_ALERT_WINDOW
```

`allow` indicates AppOps permits it; an old `rejectTime` can remain from earlier attempts. Grant this powerful special access only if you trust the app and need that experiment. Do not force it when the TV does not expose the setting. This README does not add a delayed receiver or require changes to your scripts.

## 8. Build from source (optional)

Skip this section to install the existing APK. The source/build configuration has been preserved from the uploaded project:

| Setting | Value in this archive |
| --- | --- |
| Application ID | `com.custom.acertvlauncher` |
| Minimum SDK | 26 |
| Compile / target SDK | 37 / 37 |
| Android Gradle Plugin | 9.4.1 |
| Gradle wrapper | 9.6.0 |
| Daemon JVM toolchain | 25 |
| Java source compatibility | 11 |
| Kotlin Compose plugin | 2.2.10 |

Use Android Studio and SDK tooling compatible with these declared versions. The included daemon toolchain configuration requests JDK 25; Java source compatibility 11 is a separate setting. Version availability and a clean build have not been verified here. If your SDK manager or repositories do not provide these versions, use the prebuilt APK or resolve the toolchain configuration deliberately rather than downgrading random individual settings.

1. Open the folder containing `settings.gradle.kts` in Android Studio.
2. Let Android Studio configure its local SDK path and sync Gradle. `local.properties` is machine-specific and intentionally excluded.
3. Install required SDK/toolchain components when prompted.
4. Run:

```bash
# macOS / Linux
chmod +x gradlew
./gradlew assembleDebug
```

```bat
:: Windows Command Prompt
gradlew.bat assembleDebug
```

PowerShell users can run `./gradlew.bat assembleDebug`. After `BUILD SUCCESSFUL`, the output is `app/build/outputs/apk/debug/app-debug.apk`. Install that path instead of the prebuilt path in section 3.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| `adb` not found | Install Platform Tools and add it to PATH, or run its executable by full path. |
| Cannot connect / connection refused | Verify IP, debugging mode, displayed port, same network, authorization, and router client isolation. Developer mode alone does not expose network ADB. |
| `unauthorized` | Accept the debugging prompt on the TV. Reconnect if necessary. |
| `offline` after reboot | Wait for startup, then reconnect using the current TV IP/port. |
| More than one device | Keep `-s` with the exact device identifier from `adb devices`. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Installed package and APK use different signing keys. Use the original key/build. Restore a working stock Home before considering uninstall; uninstall deletes private app data. |
| Version downgrade error | Install an appropriate equal/newer version with the same key. Do not uninstall the sole working Home app. |
| APK installs but Home opens Google | Try the Home preference, then the device-matched optional workaround above. Merely opening an app does not select it as Home. |
| Some apps missing | This version queries TV / Leanback launchable apps, not every installed package. |
| Blank wallpaper | No image is bundled. Follow section 6; check that the input image is valid and `run-as` succeeds. |
| `run-as: package not debuggable` | The wallpaper method requires a debug APK. Do not weaken TV security or root the device to force it. |
| Build fails | Check the exact preserved toolchain versions in section 8. Installing the bundled APK does not require a local source build. |

Read-only diagnostics:

```bash
adb -s TV_IP:5555 shell dumpsys activity activities
adb -s TV_IP:5555 logcat -d -v time -s AcerLauncher:D ActivityTaskManager:I ActivityManager:I
```

Look for the resumed activity and any launch denial or crash. A launch-request log is not proof the UI became visible. Do not publish unfiltered logs with personal data or device identifiers.

## Publish this project on GitHub

Create an empty repository and upload the **contents** of this folder, so `README.md`, `app`, `gradle`, and `settings.gradle.kts` are at its root. Keep the Gradle wrapper JAR. The `prebuilt` directory intentionally contains the existing APK for the easy installation path; you can also attach that same APK to a GitHub Release. If moving it, update section 3's download instructions.

IDE caches, generated build folders, macOS metadata, and the original machine-specific `local.properties` were omitted from this prepared copy. Application source, manifest, resources, and Gradle configuration were preserved. Keep signing keys and passwords private. No new software license has been assigned; choose a license before offering open-source reuse, and publish only assets you have rights to distribute.

The archive and APK integrity and source/documentation paths were checked. No TV was connected and no clean Android build was run while preparing this README. Device reports for the original setup do not guarantee compatibility with every Android TV.

After setup, disconnect ADB when finished. If you disable debugging, re-enable it before you need ADB-based recovery. Never expose network debugging directly to the internet.

Official references: [Platform Tools](https://developer.android.com/tools/releases/platform-tools), [ADB connection and installation](https://developer.android.com/tools/adb), [Android TV setup](https://developer.android.com/training/tv/start/start).
