# NEXORA KEY

A starter Android IME (keyboard) built with Kotlin and `InputMethodService`.

## MVP scope
- English, basic Sinhala character keys, and basic Arabic character keys
- Local keyboard UI with a dark neon theme
- No Internet permission and no typed-text logging/storage
- Language switch and basic editing keys

This is an early prototype, not a full production keyboard. Sinhala transliteration, complete layouts, Arabic diacritics, prediction, autocorrect, and accessibility testing remain future work.

## Build on GitHub from a phone
1. Create a **private** GitHub repository named `NEXORA-KEY`.
2. Upload these project files and folders to the repository root, preserving paths.
3. Open **Actions** and enable workflows if GitHub asks.
4. Choose **Build NEXORA KEY APK** and press **Run workflow** (or push a commit to `main`).
5. Open the completed run and download `NEXORA-KEY-debug-apk` from Artifacts.
6. Extract the ZIP and install `app-debug.apk` on your Android phone.

The included workflow uses GitHub-hosted Linux runners to build the APK; no Android SDK is required on the phone.

## Enable and test
1. Open NEXORA KEY.
2. Tap **Enable NEXORA KEY** and enable it in Android's keyboard settings.
3. Return to the app and tap **Select keyboard**.
4. Open Notes or a draft message and test English, Sinhala, Arabic, backspace, space, and enter.
5. Confirm Arabic text direction in the target app. RTL presentation depends partly on the app receiving the text.

## Privacy
The manifest intentionally declares no Internet permission. This MVP does not upload, log, or persist typed text. Do not add analytics, networking, clipboard collection, or text learning without explicit privacy design and consent.

## Technical notes
- Application ID: `com.ntech.nexorakey`
- Minimum Android API: 23
- Compile/target SDK: 35
- Android Gradle Plugin: 8.7.3
- Gradle: 8.9
- JDK: 17

A debug APK is for testing, not Play Store distribution. Create a properly signed release build before publishing.
