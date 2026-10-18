# Notes for coding agents

## Project

- Goshiki is an unofficial Android client for [Koillection](https://github.com/benjaminjonard/koillection) servers.
- Kotlin, Jetpack Compose and Material 3. Retrofit and Moshi for the Koillection API (API Platform, JSON-LD).
- The application ID `com.humboshot.koillection` is on Google Play. Never change it.

## Build and check

Run this before each commit. All tests must pass, and lint must have 0 errors.

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

## Commits and releases

- Each push to `main` uploads a signed build to the internal testing track (`.github/workflows/android.yml`).
- The Google Play release notes are the commit titles of the push (`.github/scripts/release_notes.py`). Testers read them.
  - Write the title of a commit that changes the app for its users, for example "Sort collections and items".
  - Start the title of an internal commit with a tag in square brackets, for example `[ci]`, `[docs]`, `[build]` or `[test]`. These commits are left out of the release notes.
- Write commit messages in Simplified Technical English. Add no AI attribution lines.
- CI sets the build number from the run number. It is both the `versionCode` and the `versionName`, for example 104. Do not upload builds by hand.

## Devices and the server

- The emulator `emulator-5554` is logged in to the owner's real server. Use it read-only: open screens and go back.
  - Never tap Save, Delete, Lend, Mark returned, Upload or Log out, and never type into the login screen.
  - The owner's phone can be connected over adb at the same time. Set `ANDROID_SERIAL=emulator-5554` for every adb command.
- For store screenshots, use `docs/play-store/screenshots.py`. It starts debug builds in screenshot mode, which hides the account details and the server images.

## Koillection API facts

- The API has no sort or filter parameters. The app loads all pages (`getAllPages`) and sorts or filters itself.
- Each query returns only the data of the logged-in user (`OwnershipExtension` on the server).
- PATCH uses `application/merge-patch+json`, and Moshi leaves out null fields. So the app cannot clear a field by sending null.
