# VegiTrp

VegiTrp is an Android application built with Java and XML layouts. The
project includes Firebase integration and uses Google Maps-related
services. This README documents the currently known setup steps; confirm
app-specific Firebase services and runtime behavior in the source code
before relying on them.

## Technology Stack

-   Android Studio
-   Java
-   XML layouts
-   Gradle
-   Firebase (project configuration is supplied through
    `app/google-services.json`)
-   Google Maps SDK and Directions API
-   Volley for HTTP networking

## Prerequisites

-   Android Studio with a compatible Android SDK
-   A JDK version supported by the project's Gradle wrapper and Android
    Gradle Plugin
-   A configured Android emulator or a physical Android device
-   Access to the Firebase project used by this app
-   A Google Cloud project with the required Maps APIs enabled, if using
    map and route features

## Project Setup

### 1. Clone the repository

``` bash
git clone https://github.com/ShoebPatel-20/VegiTrp.git
cd VegiTrp
```

Open the project in Android Studio and allow Gradle sync to finish.

### 2. Configure Firebase

Check that `app/google-services.json` belongs to the Firebase project
intended for your build. If you use a different Firebase project,
download its Android configuration file and replace the local file.

Review Firebase Authentication, database, messaging, and storage usage
in the source code and enable only the services the application actually
uses. Configure Firebase Security Rules appropriately; Android client
configuration is not a substitute for backend authorization.

### 3. Configure Google Maps

The project has used a `MAPS_API_KEY` setting in local configuration.
Add the key using the configuration method expected by the current
Gradle files (for example, `local.properties` if the project reads it
there). Do not commit private development configuration.

In Google Cloud Console, enable the Maps SDK for Android and Directions
API if those features are used. Restrict the key to the Android
application package and signing certificate SHA-1 where applicable. Do
not publish unrestricted keys.

### 4. Sync and build

From the project root on Windows PowerShell:

``` powershell
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

The debug APK is normally generated under:

``` text
app\build\outputs\apk\debug\app-debug.apk
```

The Gradle build was previously reported successful; run it again after
changing configuration to confirm the current checkout builds.

### 5. Run on a device

1.  Start an Android emulator or connect a device with USB debugging
    enabled.
2.  Select the app's run configuration in Android Studio.
3.  Install and launch the app.
4.  Test the flows relevant to your build, including
    sign-in/registration, map display, location permission, route
    drawing, and any payment or notification features that are present.

Runtime verification of map markers, routes, Directions responses, and
polylines should be completed on an emulator or physical device before
release.

## Repository Structure

Important files and folders include:

-   `app/src/main/java/com/example/vegitrp/` --- Java application source
-   `app/src/main/` --- Android resources and manifest
-   `app/google-services.json` --- Firebase Android client configuration
-   `gradle.properties` --- Gradle project properties
-   `gradlew.bat` --- Windows Gradle wrapper

The exact set of screens and services should be confirmed in the current
source tree.

## Security Notes

-   Keep `local.properties`, signing keys, keystore passwords, and other
    private configuration out of Git.
-   Do not hardcode passwords, private tokens, or service-account
    private keys in the Android app.
-   Configure Firebase rules and Google API key restrictions before
    deploying.
-   Treat Firebase client configuration as public client configuration,
    not as proof that the Firebase project is secure.
-   Review commit history if a real credential was ever committed;
    deleting a file in a later commit does not remove it from earlier
    commits.

## Current Status

The project has been pushed to GitHub on the `main` branch. The last
reported Git status was clean and synchronized with `origin/main`.

## Contributing / Development Workflow

1.  Create a feature branch.
2.  Make a focused change.
3.  Build with `.\gradlew.bat assembleDebug`.
4.  Test the changed feature on a device or emulator.
5.  Commit the change and push the branch.
6.  Open a pull request if using a collaborative workflow.

## License

No license information has been confirmed. Add a `LICENSE` file if you
intend to grant others permission to use, modify, or distribute this
project.
