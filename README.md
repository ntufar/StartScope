# StartScope

**StartScope** is an Android app that turns Android 15’s [`ApplicationStartInfo`](https://developer.android.com/reference/android/app/ApplicationStartInfo) APIs into an on-device timeline: *why* processes started, *cold / warm / hot* classification, and **TTID** / **TTFD**-style timings—without sending data to the cloud.

Full product context, personas, and roadmap live in **[`docs/PRD.md`](docs/PRD.md)**.

---

## Table of contents

- [Features](#features)
- [Requirements](#requirements)
- [Getting started](#getting-started)
- [Project structure](#project-structure)
- [Architecture](#architecture)
- [Permissions & privacy](#permissions--privacy)
- [Localization](#localization)
- [CI/CD](#cicd)
- [Useful Gradle tasks](#useful-gradle-tasks)
- [Roadmap (from PRD)](#roadmap-from-prd)
- [Contributing](#contributing)

---

## Features

### Implemented (MVP ~v0.1)

| Area | Description |
|------|-------------|
| **Historical collection** | `WorkManager` periodically polls `ActivityManager.getHistoricalProcessStartReasons(200)` (idle constraints per PRD). |
| **Live completions** | `StartCollectorService` (foreground `dataSync`) registers `addApplicationStartInfoCompletionListener` and persists completed starts. |
| **Parsing** | Maps start type, reason, and `getStartupTimestamps()` into TTID / TTFD-style durations and metadata. |
| **Storage** | **Room** table `starts`, unique `dedupeKey`, retention **7 days** by default (configurable 1–30 in Settings). |
| **Timeline** | **Jetpack Compose** + **Material 3**: reverse-chronological list grouped by calendar day; filter by app (chips). |
| **App detail** | Per-package averages (cold / warm / hot TTID over 7 days), counts by reason, last 20 starts. |
| **Start detail** | Single-start view with phase-style progress bars and incomplete-start messaging. |
| **Usage access** | Onboarding directs users to **Usage access** settings; collection gates on that permission. |
| **Offline-only** | No `INTERNET` / `ACCESS_NETWORK_STATE` in the manifest; processing stays on device. |
| **i18n** | **English** (`values`) and **Greek** (`values-el`). |

### Not in scope yet (see PRD)

Autostart “Doctor” cards, rule-based suggestions, export (JSON/CSV), widgets, baseline alerts, intent inspector—tracked as **v0.2+** in the PRD.

---

## Requirements

| Item | Version / note |
|------|----------------|
| **Android** | **15 (API 35)+** only (`minSdk` / `compileSdk` / `targetSdk` = 35). |
| **JDK** | **17** (Temurin or compatible). Gradle Kotlin DSL and AGP are validated against Java 17. |
| **Android SDK** | **API 35** platform + a recent **Build-Tools 35.x** (CI installs `platforms;android-35` and `build-tools;35.0.0`). |

Older devices are intentionally unsupported in v1 (see PRD §4, §14).

---

## Getting started

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd StartScope
```

### 2. Android SDK path

Create **`local.properties`** in the repo root (it is gitignored) so Gradle can find the SDK:

```properties
sdk.dir=/absolute/path/to/Android/sdk
```

On macOS, that is often:

```properties
sdk.dir=/Users/<you>/Library/Android/sdk
```

### 3. Use JDK 17

If your default `java` is newer (e.g. 21+), point `JAVA_HOME` at 17 before invoking Gradle:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # macOS
./gradlew :app:assembleDebug
```

### 4. Run a debug build

```bash
chmod +x gradlew   # if needed
./gradlew :app:assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 5. Install on a device or emulator

- Use **API 35** system images / physical devices on Android 15+.
- After install: complete **onboarding**, grant **Usage access** for StartScope, optionally allow **notifications** (foreground service), then use other apps so the system can emit start records.

---

## Project structure

```text
StartScope/
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions: assemble + lint + APK artifact
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/startscope/coldstart/   # Kotlin sources (namespace)
│       └── res/
│           ├── values/         # English strings, themes
│           └── values-el/      # Greek strings
├── docs/
│   └── PRD.md                  # Product requirements
├── gradle/
│   ├── libs.versions.toml      # Version catalog (Kotlin, Compose BOM, Room, WorkManager, …)
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── README.md
```

**Note:** The **application ID** and Kotlin **package** are `com.startscope.coldstart`; the **product name** shown in the UI is **StartScope**.

---

## Architecture

| Layer | Role |
|-------|------|
| **`StartScopeApplication`** | Wires `AppContainer`, schedules periodic `WorkManager` work. |
| **`StartCollectorService`** | Foreground `dataSync` service: notification channel, `addApplicationStartInfoCompletionListener`, writes via repository. |
| **`StartWorker`** | Polls `getHistoricalProcessStartReasons`, inserts via `StartRepository`. |
| **`WorkScheduling`** | Unique periodic + one-shot work; idle-related constraints per PRD. |
| **`StartRepository` + Room** | `StartEntity` / `StartDao` / `AppDatabase` (`startscope.db`), prune by retention from **DataStore** prefs. |
| **`ApplicationStartInfoMapper`** | Maps `ApplicationStartInfo` → entity (TTID/TTFD, wall-clock anchor for display, dedupe key). |
| **Compose UI** | `AppRoot` + **Navigation**: onboarding, timeline, settings, app detail, start detail; **Material 3** theme `StartScopeTheme`. |
| **`BootCompleteReceiver`** | Re-schedules work and restarts collector when appropriate after boot. |

Key Android APIs:

- `ActivityManager.getHistoricalProcessStartReasons(int)`
- `ActivityManager.addApplicationStartInfoCompletionListener(Executor, Consumer<ApplicationStartInfo>)`

---

## Permissions & privacy

| Permission / capability | Purpose |
|-------------------------|--------|
| **Usage access** (`PACKAGE_USAGE_STATS` via settings, not a normal install-time permission) | Required so the system exposes cross-app process start history to StartScope. |
| **`FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_DATA_SYNC`** | Keep the completion listener active with a visible notification. |
| **`POST_NOTIFICATIONS`** | Runtime prompt on Android 13+ for that notification. |
| **`RECEIVE_BOOT_COMPLETED`** | Restore scheduling and optional collector start after reboot. |

**Not declared:** `INTERNET`, `ACCESS_NETWORK_STATE` (see PRD F7).

Data stays on device unless you add an explicit export feature later (v0.2+). Retention and “background collection” toggles live under **Settings**.

---

## Localization

- **English:** `app/src/main/res/values/strings.xml`
- **Greek:** `app/src/main/res/values-el/strings.xml`

Add a new locale by copying `values` → `values-<language code>` and translating strings.

---

## CI/CD

GitHub Actions workflow **[`.github/workflows/ci.yml`](.github/workflows/ci.yml)** runs on **push** and **pull_request** to **`main`** or **`master`**, and **`workflow_dispatch`**.

It:

1. Sets up **JDK 17** and the **Android SDK** (API 35 + build-tools).
2. Runs **`./gradlew assembleDebug`** and **`./gradlew lintDebug`**.
3. Uploads the **debug APK** as a workflow artifact named **`app-debug`**.

To run CI on other default branches, extend the `branches:` lists in the workflow file.

---

## Useful Gradle tasks

| Task | Description |
|------|-------------|
| `./gradlew :app:assembleDebug` | Debug APK (no upload signing). |
| `./gradlew :app:assembleRelease` | Release APK/AAB; configure signing for real distribution. |
| `./gradlew :app:lintDebug` | Android Lint for the debug variant. |
| `./gradlew :app:kspDebugKotlin` | Room / KSP code generation (runs as part of compile). |

---

## Roadmap (from PRD)

High-level next steps are in **[`docs/PRD.md`](docs/PRD.md)** (v0.2 Doctor engine, export, widgets; v0.3 baselines, intent inspector). Issues and milestones in GitHub can mirror those IDs (**F1–F13**) if you track work there.

---

## Contributing

1. **Branch** from `main` / `master` (or your default branch).
2. **Keep PRs focused**; match existing Kotlin / Compose style.
3. **Run** `./gradlew :app:assembleDebug :app:lintDebug` locally before pushing.
4. **Document** user-visible changes in the PR description; update **README** / **PRD** when behavior or requirements shift.

There is no `LICENSE` file in the repository yet; the PRD mentions open-sourcing the collector under **Apache 2.0** as a future goal—add a `LICENSE` when you pick a policy for the whole app.

---

## Acknowledgements

Design and requirements are driven by **[`docs/PRD.md`](docs/PRD.md)** (StartScope / Android 15 startup transparency, EU-focused use cases).
