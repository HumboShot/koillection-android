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

The GitHub workflow in section 5 sets the build number. A build by hand uses build number 3.

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
- **Phone screenshots:** at least 2. `screenshots.py` takes 6 of them with your private details hidden:
  1. Install a debug build on a device or emulator, and log in.
  2. Run it from the repository root, with a collection and an item from your server:
     ```bash
     python docs/play-store/screenshots.py --device emulator-5554 --collection Movies --item "Wolf Children" --hide <your username> --hide <your server>
     ```
  3. The app starts in screenshot mode and shows sample account details and placeholder images. Any `--hide` text that still shows is blurred, and the script lists it.
  4. The files are in `build/play-screenshots/`. Check each one before you upload it.
  - Screenshot mode also replaces every image from the server with a colored placeholder, because covers of films, books or games belong to their publishers. Item names still show, so pick a collection whose names you are happy to show.

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

## 5. Automatic uploads to the internal track

The workflow `.github/workflows/android.yml` runs on GitHub Actions:

- On each pull request: unit tests and lint.
- On each commit on `main`: the same checks, then a signed app bundle goes to the **internal testing** track.
- The release notes ("What's new") list the titles of the commits in the push, up to Google Play's 500 characters. Write commit titles for users, for example "Sort collections and items". Commits whose title starts with a tag in square brackets, for example `[ci]` or `[docs]`, are left out. When no commit is left, the note says "Small improvements." The notes are in English (`en-US`). If the default language of your store listing is different, set the `PLAY_NOTES_LANGUAGE` variable, for example to `en-GB`.

Each upload gets a build number of 100 + the run number (101, 102, ...). It is the `versionCode`, the version name that users see, and the release name in the Play Console. The offset keeps it above the versions that you uploaded by hand. Do not upload builds by hand anymore.

### Set it up (once)

1. **Make a service account.**
   1. In the [Google Cloud console](https://console.cloud.google.com/), select or make a project, and enable the **Google Play Android Developer API**.
   2. Under IAM & Admin → Service accounts, make a service account. It needs no Cloud roles.
   3. Under the service account → Keys, add a JSON key and download it.
2. **Give the service account access in the Play Console.**
   1. Go to Users and permissions → Invite new users, and enter the service account's email address.
   2. Under App permissions, add Goshiki with **Release apps to testing tracks** and **View app information**.
3. **Add the secrets to GitHub.** Run these in PowerShell, in the folder with the key files. `--repo` is needed when you are outside the repository. For the passwords and the alias, `gh` asks for the value, so it does not show in the shell history.
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\upload-key.jks")) | gh secret set UPLOAD_KEYSTORE_BASE64 --repo HumboShot/koillection-android
   ```
   ```powershell
   gh secret set UPLOAD_STORE_PASSWORD --repo HumboShot/koillection-android
   ```
   ```powershell
   gh secret set UPLOAD_KEY_ALIAS --repo HumboShot/koillection-android
   ```
   ```powershell
   gh secret set UPLOAD_KEY_PASSWORD --repo HumboShot/koillection-android
   ```
   ```powershell
   Get-Content .\service-account.json -Raw | gh secret set PLAY_SERVICE_ACCOUNT_JSON --repo HumboShot/koillection-android
   ```
   - In Git Bash, `base64 -w0 upload-key.jks | gh secret set UPLOAD_KEYSTORE_BASE64` and `gh secret set PLAY_SERVICE_ACCOUNT_JSON < service-account.json` do the same.
   - `keytool -list -keystore upload-key.jks` shows the key alias.
   - `gh secret list --repo HumboShot/koillection-android` shows the names of the secrets, not the values.

   Then delete the downloaded JSON key, or keep it in your password manager.
4. **Until Google has reviewed one release of the app,** the Play API only accepts draft releases. Set this variable, and remove it after the first review:
   ```powershell
   gh variable set PLAY_RELEASE_STATUS --body draft --repo HumboShot/koillection-android
   ```
   A draft release does not reach testers until you roll it out in the Play Console.

### Security

- The secrets are only used in the `publish` job, which runs only for commits on `main`. Pull requests from forks do not get the secrets.
- The job deletes the key file at the end.
