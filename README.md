# Goshiki
Goshiki is an unofficial native Android app for [Koillection](https://github.com/benjaminjonard/koillection) servers. It is not affiliated with the Koillection project.

## Troubleshooting

### Login fails with "Error: 500" (server-side JWT problem)

The app logs in through `POST /api/authentication_token`, which returns a JWT. If the website works but the app gets a **500**, the server can't create the token. That is a server configuration problem, not an app bug.

**1. Confirm it's the server.** Run this with your real credentials:
```sh
curl -s -X POST https://<your-server>/api/authentication_token \
  -H "Content-Type: application/json" \
  -d '{"username":"<user>","password":"<password>"}'
```
- `{"token":"..."}` means the server is fine.
- A 500 response or an HTML error page means the problem is on the server.
- `401 Invalid credentials` means the username or password is wrong.

**2. Test token creation directly on the server.** This command skips the network and your password:
```sh
docker exec -it <koillection-container> sh -c 'cd /app/public && php bin/console lexik:jwt:generate-token <user>'
```
A long `xxxx.yyyy.zzzz` string means token creation works. An error tells you the cause:

| Error | Cause |
|---|---|
| `bad decrypt` / `pkcs12 cipherfinal error` | The JWT private key was encrypted with a different passphrase than the current `JWT_PASSPHRASE` (see below) |
| `Unable to load key` / file not found | The keys are missing: run `php bin/console lexik:jwt:generate-keypair` |

The server log is in the container at `/app/public/var/log/prod.log`.

**3. Fix `bad decrypt` permanently.** Koillection's Docker entrypoint ([`docker/entrypoint.sh`](https://github.com/benjaminjonard/koillection/blob/1.8/docker/entrypoint.sh)) handles a missing or empty `JWT_PASSPHRASE` badly:
- On every start it writes a new random `JWT_PASSPHRASE` to `.env.local`.
- It only creates keys with `--skip-if-exists`, so existing keys are kept.

The first restart therefore breaks the API login. Regenerating the keys only helps until the next restart.

`APP_SECRET` gets the same treatment. A new secret on every start logs everyone out and causes "Invalid CSRF token" errors.

Steps:
1. Generate two values: run `openssl rand -base64 32` twice.
2. Set both in the Koillection service in `docker-compose.yml`:
   ```yaml
   environment:
     JWT_PASSPHRASE: "<first value>"
     APP_SECRET: "<second value>"
   ```
3. Recreate the container and regenerate the keys with the now-fixed passphrase:
   ```sh
   docker compose up -d
   docker exec -it <koillection-container> sh -c 'cd /app/public && php bin/console lexik:jwt:generate-keypair --overwrite -n && php bin/console lexik:jwt:generate-token <user>'
   ```
4. Run `docker restart <koillection-container>`, then the `generate-token` command again. It should still print a token.

Related upstream reports: [#1600](https://github.com/benjaminjonard/koillection/issues/1600), [#1400](https://github.com/benjaminjonard/koillection/issues/1400), [#1410](https://github.com/benjaminjonard/koillection/issues/1410).

### Build requirements

The project uses Android Gradle plugin 9.4, Gradle 9.6 and Kotlin 2.4. Gradle needs JDK 17 or newer. The JDK that Android Studio includes (JDK 25) works.

From the command line, point `JAVA_HOME` at a JDK 17 or newer, then run `./gradlew assembleDebug`.

### Server address

Enter the server as just the domain, for example `collection.example.com`. The app adds `https://` itself. To reach a server on your local network over plain HTTP, type the `http://` prefix explicitly.

## Credits

- The app icon and the other icons in the app are [Material Icons](https://github.com/google/material-design-icons) by Google, used under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- [Koillection](https://github.com/benjaminjonard/koillection) is made by Benjamin Jonard.
