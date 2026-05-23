# App Permission Analyzer

App Permission Analyzer is a local-first Android app for reviewing installed apps
and APK files before you trust them. It explains requested permissions in plain
language, detects common tracker SDK signatures, highlights risky permission
combinations, and generates a simple privacy score.

The app does not request internet access. Analysis runs on the device.

## Features

- Scan installed apps and inspect declared Android permissions.
- Analyze APK files locally before installation.
- Detect common tracker, advertising, attribution, and messaging SDK signatures.
- Explain permission privacy impact and legitimate use cases.
- Highlight suspicious permission combinations such as SMS with contacts,
  background location with trackers, and overlay with sensitive communication.
- Generate a privacy score with clear score factors.
- Share or copy a local analysis report.

## Editions

| Flavor | Application ID | Purpose |
| --- | --- | --- |
| `free` | `id.biz.rrndev.appanalyzer` | Public free build with the Play Store upgrade CTA. |
| `pro` | `id.biz.rrndev.appanalyzer.pro` | Pro build for direct distribution or release builds without the upgrade CTA. |

## Privacy

- No internet permission.
- No app-owned ads SDK.
- No app-owned analytics SDK.
- No Google Play Services, AdMob, Play Billing, Firebase, Crashlytics, or other
  proprietary Google SDK dependencies.
- APK analysis uses temporary local cache only while parsing the selected file.

Tracker names such as Firebase Analytics and AdMob appear in the source because
the app detects those SDK signatures in other apps. They are not dependencies of
this app.

## Requirements

- Android Studio or Android command-line tools.
- JDK 21, as used by the Gradle wrapper environment.
- Android SDK 36.

## Build

Use the Gradle wrapper from the repository root:

```bash
./gradlew :app:assembleFreeDebug
./gradlew :app:assembleProDebug
````

Release builds:

```bash
./gradlew :app:assembleFreeRelease
./gradlew :app:bundleFreeRelease
./gradlew :app:assembleProRelease
./gradlew :app:bundleProRelease
```

Tests:

```bash
./gradlew :app:testFreeDebugUnitTest :app:testProDebugUnitTest
```

## License

This project is copyright (C) 2026 RRNDev and licensed under the GNU General
Public License v3.0 or later (`GPL-3.0-or-later`). See `LICENSE` for details.

App name, logo, icon, screenshots, store listing assets, and other branding
materials are not licensed for reuse as branding or trademarks without explicit
permission from RRNDev.