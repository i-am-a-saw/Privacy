## Privacy

**Privacy** is an Android messenger that aims to secure your identity and chats. It focuses on data encryption and preventing unauthorized access to your account, messages and sensitive data.

## Status - in development

The current milestone is **<u>Foundation</u>** (milestone 0 of 8)  
List of modules:
- `app`: Android app built with Kotlin, Jetpack Compose and Hilt.
- `core:model`: user, conversation and message models.
- `core:common`: clock, dispatcher and error abstractions.
- `core:designsystem`: app theme and reusable Compose components.
- `core:testing`: test clock, dispatcher rule and test data builders.
- `build-logic`: shared Gradle convention plugins.
- `supabase/config.toml`: local Supabase configuration.

**build-logic** is an included Gradle build with convention plugins (Android application, Android Library, Compose, JVM Library) that simplify maintaining the same configuration across modules.

## Design decisions

**Fixed brand palette instead of dynamic color.** Colors in this app carry security meaning  
(verified/unverified safety numbers, key-changing warnings). They must look the same on every device and have  
contrast I have tested, which dynamic color cannot guarantee. A fixed palette is also needed as a  
fallback for Android 11 and below (minSdk is 26).

## Stack

| Tool/Framework    | Used for              |
|-------------------|-----------------------|   
| Kotlin            | core language         |
| Jetpack Compose   | UI                    |
| Room              | local storage         |
| Hilt              | DI                    |
| Supabase          | local backend         |
| Docker Desktop    | local backend         |
| JUnit 4           | tests                 |
| Gradle Kotlin DSL | convention plugins    |
| Detekt + ktlint   | linting & code smells |



## Build

Open the project in Android Studio and install Android SDK 37. The project uses `compileSdk` and `targetSdk` 37, with `minSdk` 26. The Gradle wrapper uses Gradle 9.6.0, and its daemon configuration requests JDK 25.

From the repository root in PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :core:model:test
```

## Local backend

Docker Desktop must be running. From the repository root on Windows, use the pinned Supabase CLI:

```powershell
npx.cmd --yes supabase@2.122.0 start
npx.cmd --yes supabase@2.122.0 status
npx.cmd --yes supabase@2.122.0 stop
```

The default local API is at `http://127.0.0.1:54321` and Studio is at `http://127.0.0.1:54323`; use the URLs printed by `status` if they differ. The local test phone number and fixed OTP are defined in `supabase/config.toml`. See [local backend notes](docs/notes/local-backend.md) for the verified behavior and current Studio observation.


## Decisions and license

- [ADR-001: Supabase instead of Firebase](docs/adr/001-firebase-and-supabase.md)
- [ADR-002: Project license](docs/adr/002-project-license.md)

ADR-002 records the AGPLv3 decision for original backend code. The [full AGPLv3 text](https://www.gnu.org/licenses/agpl-3.0.html) is available from GNU.


## Author
- [i-am-a-saw](https://github.com/i-am-a-saw)
