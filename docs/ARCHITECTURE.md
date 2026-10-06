# Architecture

A small single-module Android app. The goal is to stay simple until a feature needs more.

## How a launcher works

Android treats any app with an activity that declares these categories as a possible home screen:

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

Our `MainActivity` declares it (plus `LAUNCHER`, so it also appears in other launchers). The user then picks it under **Settings, Apps, Default apps, Home app**.

`launchMode="singleTask"` means pressing Home while we are open sends a new intent to the same activity. `MainActivity.onNewIntent` increments a counter that the UI observes to return to the home screen.

## Listing apps

`data/AppRepository.kt` asks `PackageManager` for every activity that handles `MAIN` + `LAUNCHER`. On Android 11 and newer, apps are hidden from queries unless declared, so the manifest has a narrow `<queries>` block. This avoids the broad `QUERY_ALL_PACKAGES` permission, which Google Play restricts.

## Code layout

```
com.example.productivitylauncher
├── MainActivity.kt            single activity, edge-to-edge, Home handling
├── data/
│   ├── AppEntry.kt            one launchable app
│   ├── AppFilter.kt           pure search logic (unit tested)
│   ├── AppRepository.kt       PackageManager query
│   └── FrogStore.kt           local storage for the frog text
└── ui/
    ├── LauncherApp.kt         screen switching and Back/Home handling
    ├── theme/Theme.kt         AppColors and the Material theme
    ├── components/            AppText, Rule (shared building blocks)
    ├── home/HomeScreen.kt
    └── apps/AppListScreen.kt
```

## Conventions

- **Screens** are composables in `ui/<feature>/`. They receive plain callbacks and state, not an Activity.
- **Navigation** is a tiny enum in `LauncherApp`. Introduce a navigation library only when screens need arguments or a back stack.
- **State** uses `remember` and `rememberSaveable`. Introduce `ViewModel` when logic outgrows a composable, for example the gate flow.
- **Logic without Android** (filtering, friction rules, pet decisions, scheduling) goes in plain Kotlin classes so it can be tested on the JVM.
- **Storage** is local only. SharedPreferences for single values. Move to DataStore or Room if the data grows.
- **Theme** values come only from `AppColors`. Text uses `AppText`.

## Planned building blocks

These are not built yet. They show where new code should go.

| Feature | Likely home |
| --- | --- |
| Gate rules (reason length, phrase, doubling wait, daily limit) | `domain/gate/` pure Kotlin + `ui/gate/` |
| Gated and pinned app settings | `data/` store + `ui/settings/` |
| Pet engine (triggers, caps, cooldowns, line selection) | `domain/pet/` pure Kotlin, so every rule is unit tested |
| Reminders and notifications | `notifications/` with `WorkManager` or `AlarmManager` |
| Phone usage (total time) | `data/usage/` with `UsageStatsManager`, behind the optional Usage Access permission |
| Tools widgets | `ui/tools/<widget>/` |

## Permissions policy

| Permission | State |
| --- | --- |
| `INTERNET` | Never requested |
| `QUERY_ALL_PACKAGES` | Not used. Use `<queries>` |
| Usage Access | Planned, optional, explained in-app |
| `POST_NOTIFICATIONS` | Planned, optional, explained in-app |

Any new permission needs an issue and a clear user benefit.

## Build

- Gradle Kotlin DSL, versions in `gradle/libs.versions.toml`.
- `compileSdk` and `targetSdk` are 36 because Google Play requires new apps and updates to target Android 16 since 31 August 2026. Check the Play requirement again before each release.
- CI: `.github/workflows/build.yml` builds the debug APK, runs unit tests and lint on every push and pull request. `.github/workflows/release.yml` builds the release APK and AAB.
