# Bootstrap compatibility report - com.nexvora.terminal

Date of research: 2026-11-05. Sources read: termux-packages wiki ("For maintainers", "Building packages",
"Termux execution environment", "Build environment"), termux-packages issue #24396, PR #10330, and the
third-party project robertkirkman/termux-generator. **The GitHub file view of `scripts/properties.sh`,
`scripts/generate-bootstraps.sh` and `scripts/build-bootstraps.sh` could not be opened** (GitHub blocks
automated file access and the build sandbox has no network), so flags below are limited to what the docs
and issue text state. Run `--help` on each script in your checkout to confirm.

## Result

| Question | Status |
|---|---|
| Is the official bootstrap compatible with `com.nexvora.terminal`? | **No.** Upstream docs: packages "would not work for any other app package name"; they must be compiled for the new prefix. |
| Can compatible bootstraps be built? | **Yes, per upstream docs** - not yet done/verified for NexVora. |
| Can `pkg install` work? | **Only with a package repository you build and host** (official mirrors publish `com.termux` packages). Without it only bootstrap-contained commands work (`pkg upgrade/install` fail - stated in #24396). |
| Does a compatible bootstrap exist today? | **No.** None has been built or tested. |

## What upstream documents (verified from the sources above)

1. **Package name variable.** Packages are built for the values in `scripts/properties.sh`:
   `$TERMUX_APP__PACKAGE_NAME`, `$TERMUX_APP__DATA_DIR` (= `/data/data/$TERMUX_APP__PACKAGE_NAME`),
   `$TERMUX__ROOTFS`, `$TERMUX__HOME`, `$TERMUX__PREFIX`, etc. (Wiki pages "Building packages",
   "Termux execution environment".) The "For maintainers" wiki page still uses the older name
   `TERMUX_APP_PACKAGE`; issue #24396 (Apr 2025) shows the current assignment is
   `TERMUX_APP__PACKAGE_NAME="com.termux.test"`. In the issue's quoted `properties.sh` comment,
   `TERMUX_APP__PACKAGE_NAME` is in the list of variables "safe to modify when forking" (I could not read the
   rest of that list).
2. **`TERMUX_REPO_*` variables** describe the values the *official hosted packages* were compiled for.
   Per "Building packages", `-i/-I` (download dependencies from the repo) is ignored/unusable when your values differ.
   Do not set them to your own values (my reading of the docs; the issue reporter did and still failed).
3. **Rebuild after changing the name:** run `./scripts/run-docker.sh ./clean.sh` (wiki) - otherwise
   previously built `com.termux` packages are reused (#24396, agnostic-apollo).
4. **Two bootstrap scripts** (wiki "For maintainers"):
   * `./scripts/generate-bootstraps.sh` - pulls debs from a *published apt repo* built for your package name.
     Options seen in docs: `--architectures`, `--repository`.
   * `./scripts/build-bootstraps.sh` - builds from local package sources, no repo needed.
     Documented usage: `./scripts/run-docker.sh ./scripts/build-bootstraps.sh --architectures aarch64 &> build.log`
     (also `--add <pkg>`). Use this first.
5. **Docker** is the recommended build environment (`./scripts/run-docker.sh`).
6. **Known pitfall (#24396):** after building with a custom name, `apt` worked but `pkg` failed with
   `/data/data/com.termux/files/usr/bin/termux-setup-package-manager: No such file or directory`.
   Cause stated in the thread: `termux-tools` was built with the default prefix (its `configure.ac` reads
   `TERMUX_PREFIX` but the build system does not export it). Suggested remedy in the thread:
   clean/reset everything, or rebuild with
   `scripts/run-docker.sh ./build-package.sh -I -f termux-tools` and put that deb into the bootstrap.
   Maintainers also say vanilla builds with a changed name hit errors like this frequently.
7. **Repository:** needs a web-served apt layout like `https://packages-cf.termux.dev/apt/termux-main/`;
   the thread names `aptly` as a helper; every package you want users to install must be built for your name
   and uploaded, and metadata regenerated. PR #10330 notes a missing `termux-keyring` leaves no keys in
   `$PREFIX/etc/apt/trusted.gpg.d/` and causes `NO_PUBKEY` errors - a custom repo needs its own signing key
   shipped in the bootstrap.
8. **Alternative (maintainer comment in #24396):** pre-build every package you need, put all of them in one
   large bootstrap zip, no repo. Downsides stated: APK can reach multi-GB (4 GB limit), versions frozen,
   `pkg upgrade` unavailable, app Java code adjusted to fit the big zip.
9. **Third-party reference:** robertkirkman/termux-generator builds Termux with a changed package name
   (`./build-termux.sh --name <pkg> --architectures aarch64`, README estimate ~3 h for one arch with extras,
   needs Docker). It patches Termux, so it is a reference for needed fixes, not a drop-in for the NexVora fork.

## How each requested item is achieved (plan; none executed yet)

| Item | Mechanism | Verification |
|---|---|---|
| Bootstrap archives for `com.nexvora.terminal` | termux-packages with `TERMUX_APP__PACKAGE_NAME="com.nexvora.terminal"`, `clean.sh`, `build-bootstraps.sh` (workflow `bootstrap_build.yml`, experimental) | `scripts/verify-bootstrap.sh` + on-device boot |
| Compatible package binaries | Same build; every package compiled by `build-package.sh` for the new prefix | spot-check with `readelf -d` and `grep -a` |
| ELF RUNPATH / executable paths | Set by the build system from `properties.sh` at compile time (wiki: packages "compiled specifically" for the prefix); not patched afterwards | `readelf -d $PREFIX/bin/bash` shows RUNPATH under `/data/data/com.nexvora.terminal`; verify script finds zero `com.termux` prefixes |
| dpkg / apt config | Generated by the same build (dpkg, apt, termux-tools packages). `termux-tools` is the known weak point (#24396) | after install: `dpkg --print-architecture`, `apt-config dump \| grep -i dir`, `cat $PREFIX/etc/apt/sources.list` must not mention `com.termux` |
| Package repository | Build all desired packages for the new name, host apt tree (e.g. aptly), sign with a NexVora GPG key, ship key + `sources.list` in the bootstrap | `apt update && pkg install nano` on device |

## Unverified items to resolve first (honest list)

* Exact current text of the "safe to modify when forking" list and whether other variables must change.
* Where `build-bootstraps.sh` writes its zips in current master (workflow searches `tp/**/bootstrap-*.zip`).
* Whether current master's `build-bootstraps.sh` still exists and accepts the documented flags.
* How/where the repo URL and signing key get into `sources.list`/keyring for a custom mirror (inspect `packages/termux-tools`, `termux-keyring`, `termux-core` build scripts).
* Whether the `termux-am` / `termux-exec` pieces used by the app work under the new prefix.
* Runtime: `login`/`motd` behavior, second-stage setup, app-side checks against the bootstrap variant (`TermuxBootstrap`).
* Build time and disk limits on GitHub-hosted runners (upstream implies hours; use a self-hosted runner if it fails).
