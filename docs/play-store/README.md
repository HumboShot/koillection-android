# Google Play release guide

## 1. Make the upload key (once)

Run this from the repository root. Choose a strong password and keep it in your password manager.

```bash
keytool -genkeypair -v -keystore upload-key.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
```

Then create `keystore.properties` in the repository root:

```properties
storeFile=upload-key.jks
storePassword=<your store password>
keyAlias=upload
keyPassword=<your key password>
```

- Git ignores both files. Never commit them.
- Keep a backup of `upload-key.jks` outside this computer. With Play App Signing, Google can reset a lost upload key, but that takes time.

## 2. Build the app bundle

```bash
./gradlew bundleRelease
```

The bundle is `app/build/outputs/bundle/release/app-release.aab`. The release build uses R8, so also upload the mapping file `app/build/outputs/mapping/release/mapping.txt`. Play then shows readable crash reports.

For each new upload, raise `versionCode` in `app/build.gradle.kts` by 1 and update `versionName`.

## 3. Play Console

### App details

- **App name:** Goshiki. "Koillection" is the name of Benjamin Jonard's project, so the descriptions say that Goshiki is an unofficial client.
- **Category:** Productivity.
- **Contact email:** required. It is shown on the store page.

### Short description (80 characters at most)

> Browse and manage your self-hosted Koillection collections on Android.

### Full description

> Goshiki is an unofficial Android client for Koillection, the self-hosted collection manager.
>
> Connect to your own Koillection server and take your collections with you:
>
> • Browse collections, sub-collections and items, with images and custom fields
> • Add, edit and delete collections, items and fields
> • Tags, wishlists, albums, loans, templates and choice lists
> • History, statistics and inventories
> • The app follows the features that you switch on or off on the web
>
> Your login stays safe:
>
> • The password is encrypted with keys in the Android Keystore
> • Optional fingerprint or screen lock unlock
> • The app connects only to your own server. No analytics, no ads, no tracking.
>
> You need your own Koillection server and an account on it. Goshiki is not affiliated with the Koillection project.

### Graphics

- **App icon:** `play-icon-512.png` (512 × 512).
- **Feature graphic:** `feature-graphic-1024x500.png` (1024 × 500).
- `make_icons.py` makes these two files and the launcher PNGs again, for example after a color change.
- **Phone screenshots:** at least 2. Take them on the emulator or your phone, for example of the collections, an item, the login screen and the side menu.

### Privacy policy

Use the link to the policy in this repository:
https://github.com/HumboShot/koillection-android/blob/main/docs/PRIVACY.md

### App access

The app needs a server and an account, so choose "All or some functionality is restricted" and give the reviewers:

- The server address.
- A username and password of a demo account. Do not use your own account.

### Ads

No ads.

### Content rating

Answer the questionnaire. The app has no violence, no gambling, no user chat and no purchases. User-generated content is only shown to the user who made it.

### Target audience

18 and older is the simplest choice. The app is not made for children.

### Data safety

The developer does not collect or share data: there is no analytics, no crash service and no developer server. The app sends the user's login and collection data only to the Koillection server that the user enters.

Read Google's definitions in the Data safety form before you answer. If you decide that data sent to the user's own server counts as "collected", declare:

- **Personal info: name/user IDs** (username): app functionality, required.
- **Photos** (when the user uploads images): app functionality, optional.
- **Other user-generated content** (collections and items): app functionality, required.

In both cases:

- Data is encrypted in transit (HTTPS), when the server uses HTTPS.
- Users can delete their data on their server. The app removes its local data when the user logs out or uninstalls the app.

## 4. Testing before production

Personal developer accounts made after 13 November 2023 must run a closed test with at least 12 testers for 14 days in a row before the production release. Start the closed test as early as possible.
