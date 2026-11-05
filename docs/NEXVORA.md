# NexVora Terminal - engineering notes

NexVora Terminal is a branded fork of [Termux](https://github.com/termux/termux-app) (GPLv3).
Developer: Prince AR Abdur Rahman - Publisher: NexVora Lab's Ofc - (c) 2026 NexVora Lab's Ofc. All Rights Reserved.
Upstream licenses and copyright notices are preserved (LICENSE.md, source headers).

| | |
|---|---|
| App name | NexVora Terminal |
| applicationId | `com.nexvora.terminal` |
| Version | 1.0.0 (code 10000) |
| Java namespace | `com.termux` (unchanged on purpose, see below) |

## Phase status (2026-11-05)

* Source verification: done statically (see "Source verification" below). **Still not compiled.**
* GitHub Actions: workflows prepared; **no workflow run has happened, so there is no build evidence and no APK.**
* Bootstrap: researched in docs/BOOTSTRAP_REPORT.md; **no compatible bootstrap built yet.**
* Build configurations: devtest vs production, see docs/PRODUCTION_BUILD.md.

## Source verification (static)

Checked by script/reading: all `R.*` and `@type/name` references resolve (only Material library styles are external);
manifest is well-formed, new activities use the `com.termux` namespace and the LAUNCHER entry/shortcuts moved to
`NexVoraLauncherActivity`; all 14 JNI symbols (`Java_com_termux_*`) map to existing classes in the unchanged
`com.termux` namespace; package ID is consistent in Gradle, `TermuxConstants`, both `strings.xml` entity sets and
shortcuts (`scripts/validate-package-id.sh`, plus the `validateNexVoraPackageId` Gradle task); constants used by
`NexVoraStyle` exist with the expected types. Fixed: an error-path bug in the package-ID Gradle task; added an
Android 12+ splash background (`values-v31`). Not possible without the SDK: compilation, lint, instrumentation.

## STATUS - read this first

**Not built or run in the environment where this was prepared** (no Android SDK, NDK or network).
Source edits were checked for XML well-formedness and brace balance only, **not compiled**.
The first CI run is the first real compile; expect to fix small errors.

**Bootstrap compatibility with `com.nexvora.terminal` is NOT verified, and the official bootstrap is
known to be incompatible.** Termux bootstrap archives (downloaded by `app/build.gradle` from
termux-packages) contain binaries and config with `/data/data/com.termux/files/usr` compiled in
(ELF RUNPATH, shebangs, dpkg/apt config, SYMLINKS.txt). Android gives each app `/data/data/<applicationId>`,
so with the new ID the official bootstrap installs but `bash` and the libraries it needs will not start.
Consequences:

* A build with `com.nexvora.terminal` + official bootstrap gives you the branded UI, but **no working shell**.
* To get a working shell under the new ID you need a bootstrap **and package repository** built with
  the prefix `/data/data/com.nexvora.terminal/files/usr` (termux-packages supports this via its
  `TERMUX_APP__PACKAGE_NAME` property; follow its "building for forks/custom package name" docs - the
  exact flags change between revisions and could not be checked here). Packages from the official
  `packages.termux.dev` repo are built for `com.termux` and will break under the new prefix, so `pkg install`
  also needs your own repo.
* `scripts/verify-bootstrap.sh <bootstrap.zip> com.nexvora.terminal` is the check to run on any candidate
  bootstrap; the debug CI workflow runs it and prints the result in the job summary.
* **Working-shell test path today:** `scripts/set-app-id.sh com.termux`, build, uninstall official Termux,
  install. This runs the real engine with the NexVora UI and the official bootstrap. It must never be
  distributed as NexVora Terminal (it collides with Termux's package name and signing key).
  Switch back with `scripts/set-app-id.sh com.nexvora.terminal`.

## Existing architecture (analysed before changes)

* `terminal-emulator` - VT100/xterm emulator + JNI (`termux.c`): pty creation, fork/exec, window size.
  JNI symbols are `Java_com_termux_terminal_JNI_*`.
* `terminal-view` - Android `TerminalView` (rendering, selection, gestures, input).
* `termux-shared` - constants (`TermuxConstants`: package name, prefix `/data/data/<pkg>/files/usr`, paths),
  settings/preferences, extra keys, shell environment, utilities.
* `app` - `TermuxActivity` (UI, drawer, sessions), `TermuxService` (foreground service owning sessions),
  `TermuxInstaller` (extracts the bootstrap zip embedded via `libtermux-bootstrap.so`, `termux-bootstrap.c`
  /`.S`, into the prefix on first run), settings fragments, `RunCommandService`, file receivers.
* Bootstrap zips are downloaded at build time (`downloadBootstraps` task, SHA-256 pinned) and linked into a
  native library. First run therefore needs no network, but the **build** does.
* The applicationId used to default to the namespace (`com.termux`); `TermuxConstants.TERMUX_PACKAGE_NAME`
  must equal it and is used for the prefix path, manifest `sharedUserId`, permission names and plugin IDs.

## Modification plan and what was done

1. **Identity** - explicit `applicationId`, version, app name (`TERMUX_APP_NAME`), manifest placeholders,
   `strings.xml` entities, shortcut `targetPackage`, APK file names (`nexvora-terminal_*.apk`).
2. **Package-ID safety** - `validateNexVoraPackageId` Gradle task (runs before compile) fails the build if
   applicationId, `TermuxConstants`, both `strings.xml` entity sets and shortcuts disagree.
   Java packages and JNI names stay `com.termux` so no native code needed renaming (verified by grep:
   the only package-dependent native symbols are `Java_com_termux_*`).
3. **Branding** - dark-luxury palette (`nexvora_*` colors: deep black, cyan `#00E5FF`, purple `#8B5CF6`),
   terminal activity/drawer/extra-keys themes, new adaptive + legacy launcher icons, splash
   (`Theme.NexVora.Splash` window background, no new dependency).
4. **New screens** (`com.termux.app.nexvora`): launcher/splash, first-run welcome, settings dashboard,
   appearance (color themes, font file, text size), startup greeting, About NexVora (version, developer,
   contact links, license notice). The original Termux `SettingsActivity` remains reachable
   from the dashboard.
5. **Engine untouched** - emulator, view, session/service code, installer and native libraries are unchanged.
   Only hook into engine code: `TermuxActivity.onStart()` re-applies styles after the Appearance screen
   changes them; the two "Settings" entry points now open the NexVora dashboard.
6. **CI** - GitHub Actions (debug, release, existing tests) and `codemagic.yaml`.

### How the requested features map to code

| Feature | Implementation |
|---|---|
| Real terminal, Bash, sessions, extra keys, copy/paste, selection | Original Termux code, unchanged |
| Color themes | `NexVoraStyle` writes `~/.termux/colors.properties` (the standard Termux mechanism) |
| Font customization | picks a TTF/OTF -> `~/.termux/font.ttf`; text size via the existing preference |
| Custom startup greeting | writes `$PREFIX/etc/motd`, printed by Termux's `login` (needs bootstrap installed; on-device behavior to be confirmed) |
| Settings dashboard / About / Developer / Version | `NexVoraDashboardActivity`, `NexVoraAboutActivity` |

## Modified files

Added: `app/src/main/java/com/termux/app/nexvora/*` (9 classes), `app/src/main/res/drawable/nexvora_splash.xml`,
`scripts/{validate-package-id,verify-bootstrap,set-app-id}.sh`, `.github/workflows/release_build.yml`,
`codemagic.yaml`, `docs/NEXVORA.md`.
Modified: `app/build.gradle`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/termux/app/TermuxActivity.java`,
`app/src/main/res/values/{colors,strings,themes}.xml`, `app/src/main/res/values-night/themes.xml`,
`app/src/main/res/drawable/ic_foreground.xml`, `app/src/main/res/mipmap-anydpi-v26/ic_launcher{,_round}.xml`,
`app/src/main/res/mipmap-*dpi/ic_launcher{,_round}.png`, `app/src/main/res/xml/shortcuts.xml`,
`termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java`,
`termux-shared/src/main/res/values/strings.xml`, `.github/workflows/debug_build.yml`, `.gitignore`, `README.md`.
Removed: `.github/workflows/attach_debug_apks_to_release.yml`, `.github/workflows/trigger_library_builds_on_jitpack.yml`
(upstream release plumbing that targets Termux's repos and APK names).

## Build instructions

Requirements: JDK 17, Android SDK (platform 36, build-tools 36.0.0), NDK `29.0.14206865`, network access
(bootstrap download, Gradle/Maven dependencies).

```
export ANDROID_HOME=/path/to/sdk
./gradlew assembleDebug          # app/build/outputs/apk/debug/nexvora-terminal_*.apk
./gradlew assembleRelease        # needs signing env vars below, else signed with the public debug test key
```

Release signing env vars: `NEXVORA_KEYSTORE_FILE`, `NEXVORA_KEYSTORE_PASSWORD`, `NEXVORA_KEY_ALIAS`,
`NEXVORA_KEY_PASSWORD`. Never commit keystores.

* **GitHub Actions:** `debug_build.yml` on push/PR/manual; `release_build.yml` on `v*` tags/manual. Secrets:
  `NEXVORA_KEYSTORE_BASE64`, `NEXVORA_KEYSTORE_PASSWORD`, `NEXVORA_KEY_ALIAS`, `NEXVORA_KEY_PASSWORD`.
* **Codemagic:** `codemagic.yaml` (workflows `nexvora-debug`, `nexvora-release`); add an Android keystore
  with reference name `nexvora_keystore`.
* Optional env: `TERMUX_PACKAGE_VARIANT` (`apt-android-7` default, `apt-android-5`), `TERMUX_APP_VERSION_NAME`.

## Testing instructions

1. `scripts/validate-package-id.sh` -> all `OK`.
2. `./gradlew test` (original emulator/shared unit tests).
3. `scripts/verify-bootstrap.sh app/src/main/cpp/bootstrap-aarch64.zip com.nexvora.terminal` -> expected
   **INCOMPATIBLE** with the official bootstrap; must pass with your custom bootstrap.
4. On a device/emulator (Android 7+; targetSdk 28 as upstream):
   launch -> splash -> welcome (first run only) -> terminal; bootstrap installs; `echo $SHELL`, `ls $PREFIX/bin | head`;
   open a 2nd session from the drawer; extra keys (Ctrl+C, arrows, Tab); long-press to select/copy/paste;
   `pkg update` (only meaningful with a matching repo); drawer -> Settings -> Appearance (apply theme, font, size,
   return to terminal); Startup greeting (save, open a new session); About (links open WhatsApp/Facebook/Instagram/mail);
   Settings -> Terminal settings (original screens).
5. Test a `com.termux` build (`scripts/set-app-id.sh com.termux`) to confirm everything works with the official bootstrap.

## Known limitations

* Not compiled or run by the author of these changes; CI must be the first build.
* New package ID is **not** bootstrap-compatible with official Termux bootstraps/repos (see STATUS).
* Release APKs use the public debug test key unless signing secrets are provided.
* WhatsApp links assume the numbers are Bangladesh (+880) numbers; adjust `NexVoraBrand` if not.
* Greeting relies on the `login` script printing `etc/motd`; theme/font apply when returning to the terminal.
* Settings dashboard screens are programmatic Views (no new dependencies); Termux's own settings keep the original look.
* Termux plugin apps (API/Boot/Widget...) are namespaced under `com.nexvora.terminal.*` and do not exist for this fork.
* Remaining upstream strings/docs/fastlane/site files still mention Termux; the About screen credits it as required by the license.
