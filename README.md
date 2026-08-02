# QuickScan Pro - QR & Barcode Scanner

This is a complete Android application for scanning QR codes and barcodes. It's built with a modern Material 3 UI and is designed to be lightweight, fast, and privacy-safe.

## Features

- QR code and barcode scanning
- CameraX live preview
- Torch ON/OFF
- Auto-focus
- Vibration and sound on scan
- Scan result page with copy, share, and open URL functionality
- Local scan history using Room database
- Dark mode support
- AdMob integration (banner and interstitial ads)

## Tech Stack

- Kotlin
- Jetpack Compose
- CameraX
- ML Kit Barcode Scanner
- Room Database
- MVVM Architecture
- Material 3

## Setup Instructions

1.  **Open the project in Android Studio.**
2.  **Configure the Android SDK:** If prompted, select a valid Android SDK location. You may need to create a `local.properties` file in the root of the project with the following content:
    ```
    sdk.dir=/path/to/your/android/sdk
    ```
3.  **Build the project:** Push changes to GitHub and use the repository's GitHub Actions workflow. Android APK/AAB artifacts are not built locally.

## How to Change App Name, Package Name, and AdMob IDs

### Change App Name

1.  Open `app/src/main/res/values/strings.xml`.
2.  Change the value of the `app_name` string resource.

### Change Package Name

1.  In the Android Studio project view, right-click on the `java` folder and select "Refactor" > "Rename".
2.  Choose "Rename package" and enter the new package name.
3.  Open `app/build.gradle` and update the `applicationId` to the new package name.
4.  Open `app/src/main/AndroidManifest.xml` and update the `package` attribute in the `<manifest>` tag.

### Change AdMob IDs

1.  Open `app/src/main/java/com/quickscanpro/config/AppConfig.kt`.
2.  Replace the placeholder ad unit IDs in the `AdMob` object with your own.
3.  Open `app/src/main/AndroidManifest.xml` and replace the placeholder AdMob App ID with your own.

## Build APK and AAB

Use the repository's GitHub Actions workflow for debug and release artifacts. Configure signing and production secrets in GitHub Actions secrets; do not generate APK or AAB files locally.
