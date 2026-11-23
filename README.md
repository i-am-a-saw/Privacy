# Privacy

A modular Android messenger built with Kotlin and Jetpack Compose.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-2196F3?style=flat-square)
![Room 3](https://img.shields.io/badge/Room_3-3DDC84?style=flat-square&logo=android&logoColor=white)
![DataStore](https://img.shields.io/badge/DataStore-008577?style=flat-square)
![Supabase](https://img.shields.io/badge/Supabase-3FCF8E?style=flat-square&logo=supabase&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)

![CameraX](https://img.shields.io/badge/CameraX-008577?style=flat-square)
![ML Kit](https://img.shields.io/badge/ML_Kit-4285F4?style=flat-square&logo=google&logoColor=white)
![ZXing](https://img.shields.io/badge/ZXing-222222?style=flat-square)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![JUnit 4](https://img.shields.io/badge/JUnit_4-25A162?style=flat-square&logo=junit4&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)

## Project status

**Milestone 1 — Accounts and identity · In progress**

The current application supports authentication, profiles, and device-local contacts. Messaging and end-to-end encryption are planned.

Implemented functionality:

- Phone authentication with fixed OTPs for local test accounts.
- Session restoration and an authentication gate.
- Profile creation with username validation and availability checks.
- Exact username lookup through a PostgreSQL RPC.
- Contacts saved locally in Room and observed through flows.
- Versioned contact QR generation and camera scanning.
- Camera permission handling and scan error states.
- Sign-out cleanup of local contacts, user preferences, and the saved session, with pending-cleanup recovery.

Current work focuses on verifying sign-out behavior and interrupted-cleanup recovery on devices.

## Technology

| Area | Technologies |
|---|---|
| Language and concurrency | Kotlin, coroutines, StateFlow |
| Interface | Jetpack Compose, Material 3, Navigation 3 |
| Dependency injection | Hilt, KSP |
| Local storage | Room 3, bundled SQLite, Preferences DataStore |
| Backend | Supabase, PostgreSQL, supabase-kt, Ktor/OkHttp |
| QR codes | CameraX, bundled ML Kit barcode scanning, ZXing |
| Testing | JUnit 4, Turbine, coroutine tests, Compose tests, pgTAP |
| Build and quality | Gradle Kotlin DSL, convention plugins, Detekt, Spotless, ktlint |
| Automation | GitHub Actions, Android emulator database tests |

## Architecture

Features communicate through shared models, repositories, and navigation keys. Feature modules do not depend on one another.

| Module | Responsibility |
|---|---|
| `app` | Application entry point, root navigation, authentication gate |
| `core:model` | User, session, conversation, and message models |
| `core:common` | Results, errors, dispatchers, clock, username rules |
| `core:designsystem` | Theme, typography, reusable components |
| `core:testing` | Shared test utilities |
| `core:navigation` | Serializable navigation keys |
| `core:network` | Supabase client, DTOs, remote data sources |
| `core:data` | Repositories, mappings, sign-out orchestration |
| `core:database` | Room database, contact DAO, exported schemas |
| `core:datastore` | Application preferences and cleanup recovery flag |
| `feature:auth` | Phone entry and OTP verification |
| `feature:onboarding` | Profile and username setup |
| `feature:contacts` | Contact list, username search, QR display and scanning |
| `build-logic` | Shared Gradle convention plugins; an included build |

Backend configuration, migrations, and database tests live in `supabase/`.

## Development setup

### Requirements

- Android Studio and Android SDK **37**.
- Android device or emulator running API **26** or later.
- JDK **25** for the Gradle daemon.
- Docker Desktop with Linux containers.
- Node.js and npm for the pinned Supabase CLI.

The project uses Gradle **9.6.0**, `compileSdk` **37**, `targetSdk` **37**, and `minSdk` **26**.

### Start the local backend

Start Docker Desktop, then run from the repository root:

```powershell
npx.cmd --yes supabase@2.122.0 start
npx.cmd --yes supabase@2.122.0 status
```

Default service addresses:

| Service | Address |
|---|---|
| API | `http://127.0.0.1:54321` |
| Supabase Studio | `http://127.0.0.1:54323` |
| PostgreSQL | `127.0.0.1:54322` |

Use the addresses printed by `status` if they differ.

Set the local publishable key in your user-level Gradle properties file, `%USERPROFILE%\.gradle\gradle.properties`:

```properties
supabase.local.key=YOUR_LOCAL_PUBLISHABLE_KEY
```

The application has `local`, `dev`, and `demo` flavors. Use **localDebug** for the local backend.

### Run on a device or emulator

The local application connects to `http://127.0.0.1:54321`. Forward that port from the target device to your computer:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices
& $adb -s DEVICE_SERIAL reverse tcp:54321 tcp:54321
```

Replace `DEVICE_SERIAL` with the identifier reported by `devices`.

In Android Studio, select the `app` configuration, the `localDebug` variant, and your target device, then click **Run**.

### Local test accounts

These accounts use fixed OTPs and require no real SMS delivery.

| Account | Phone number | OTP |
|---|---|---|
| Alice | `+16505550101` | `123456` |
| Bob | `+16505550102` | `123457` |
| Carol | `+16505550103` | `123458` |

The configuration is defined in `supabase/config.toml`. See [local backend notes](docs/notes/local-backend.md) for setup details.

## Build and test

Build the local debug APK:

```powershell
.\gradlew.bat :app:assembleLocalDebug
```

Run project checks:

```powershell
.\gradlew.bat check
```

Test the convention plugins:

```powershell
.\gradlew.bat -p build-logic :convention:check
```

Run instrumentation tests with a connected device:

```powershell
.\gradlew.bat :app:connectedLocalDebugAndroidTest :core:database:connectedDebugAndroidTest
```

Run backend database tests while Supabase is running:

```powershell
npx.cmd --yes supabase@2.122.0 test db
```

GitHub Actions runs build and quality checks, unit tests, and Room DAO tests on an Android emulator.

## Design decisions

- Contacts are stored on the device rather than uploaded as a server-side contact list.
- Profile discovery uses exact username lookup rather than general profile listing.
- The interface uses fixed light and dark palettes rather than dynamic color.
- Local databases, preferences, and DataStore files are excluded from backup and device transfer.

Architecture decisions:

- [ADR-001 — Supabase instead of Firebase](docs/adr/001-firebase-and-supabase.md)
- [ADR-002 — Project licensing](docs/adr/002-project-license.md)
- [ADR-003 — Device-local contacts](docs/adr/003-local-contacts.md)
- [ADR-004 — Sign-out policy](docs/adr/004-sign-out-policy.md)

## Author

[i-am-a-saw](https://github.com/i-am-a-saw)
