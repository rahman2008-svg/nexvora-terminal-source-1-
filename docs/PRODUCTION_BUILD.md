# NexVora Terminal - build configurations and production steps

Two configurations. They are deliberately separate and enforced by Gradle (`verifyNexVoraBootstrap`),
CI, APK file names and an in-app banner.

| | A. Development Test | B. NexVora Production |
|---|---|---|
| applicationId | `com.termux` (set by `scripts/set-app-id.sh com.termux`, workspace only) | `com.nexvora.terminal` (repo default) |
| Branding | NexVora UI/name | NexVora UI/name |
| Bootstrap | official Termux bootstrap (downloaded) | custom, built for `/data/data/com.nexvora.terminal/files/usr` (`NEXVORA_BOOTSTRAP_DIR`) |
| Package repo | official (works, because ID is `com.termux`) | your own, required for `pkg install` |
| Signing | debug test key | NexVora release keystore (never committed) |
| APK name | `DEVTEST-NOT-FOR-DISTRIBUTION_nexvora-terminal_*.apk` + in-app warning | `nexvora-terminal_*.apk` |
| Release build | **refused** by Gradle | allowed only if bootstrap check passes |
| CI | `debug_build.yml` job `devtest`, Codemagic `nexvora-devtest` | `debug_build.yml` job `production-check` (no APK unless bootstrap passes), `release_build.yml`, Codemagic `nexvora-production` |

Never install a DEVTEST APK next to official Termux (same package name/signing mismatch) and never
publish one. Never publish a production-ID APK whose bootstrap check fails: the shell will not start.

## A. Local development test
```
scripts/set-app-id.sh com.termux
./gradlew assembleDebug        # app/build/outputs/apk/debug/DEVTEST-NOT-FOR-DISTRIBUTION_*.apk
scripts/set-app-id.sh com.nexvora.terminal   # switch back before committing
```
Uninstall official Termux first. On device: see testing checklist in docs/NEXVORA.md.

## B. Production
1. **Build bootstraps** for all four ABIs (aarch64, arm, i686, x86_64): run workflow
   `bootstrap_build.yml` (experimental) or follow docs/BOOTSTRAP_REPORT.md by hand. Check each zip:
   `scripts/verify-bootstrap.sh bootstrap-aarch64.zip com.nexvora.terminal` must pass.
2. **Test the bootstrap on a device** via a debug build (`NEXVORA_BOOTSTRAP_DIR=/path/to/zips ./gradlew assembleDebug`
   with the production ID): `bash` starts, `apt --version`, `dpkg -l`, `readelf -d $PREFIX/bin/bash`.
3. **Package repository:** build and host the packages, sign them with your own key, include key and
   `sources.list` in the bootstrap (BOOTSTRAP_REPORT.md section 7). Test `pkg update && pkg install nano`.
4. **Signing:** create a NexVora release keystore, keep it offline/secret store, set
   `NEXVORA_KEYSTORE_BASE64`, `NEXVORA_KEYSTORE_PASSWORD`, `NEXVORA_KEY_ALIAS`, `NEXVORA_KEY_PASSWORD`
   (GitHub) or the Codemagic keystore `nexvora_keystore`.
5. Upload the zips to a release in this repo, run `release_build.yml` with that tag (or Codemagic
   `nexvora-production`). The build fails if the bootstrap still references `com.termux`.
6. Smoke-test the signed APK on arm64 and one more ABI, then publish with checksums.

Emergency override (never for distribution): `-PnexvoraAllowIncompatibleBootstrap`.
