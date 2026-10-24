# Launcher (working title)

[![Build](../../actions/workflows/build.yml/badge.svg)](../../actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A minimal Android launcher made for students. It helps you study without your phone pulling you away: do your most important task first, make distracting apps harder to open than useful ones, and keep everything private on your phone.

> **Status: alpha.** The launcher is built and builds in CI on every change, but it is still changing quickly and has only been tested on a few phones. The app name, final icon and application ID are not decided yet (see [Open decisions](#open-decisions)).

## Why this exists

Phones are designed to pull attention away from what matters. Most "focus" apps either block everything (and get uninstalled) or only report screen time (and change nothing). This launcher takes a middle path:

- **Do the one thing that matters.** A plain to-do list where one task is your *most important task* of the day. It is shown in bold at the top. Check it first, and the other tasks unlock.
- **Friction where it counts.** Apps you mark as distracting wait behind a short pause. Your everyday apps open in one tap.
- **Help for the hard moments.** Focus sessions, Lock In mode and a red button.
- **Private by design.** No account, no ads, no analytics, no internet permission. Everything stays on the phone.

## What is built

**Home and apps**
- Home screen with a numbers clock, date, screen time and six Home apps you choose
- Searchable list of all installed apps
- Dock with Phone, Messages, Do Not Disturb and Focus

**Gate for distracting apps**
- Choose which apps are gated. Opening one starts a breathing pause, a typed phrase and a choice of how long to stay, with a longer wait each time you open it
- A daily limit for gated apps (1 hour by default) and a small floating timer over the app while a session runs
- Settings that make the gate stricter apply at once. Settings that make it easier wait 24 hours
- Gated apps cannot be Home apps

**Widgets page**
- A grid of tiles: To-do list, Quick actions, Focus timer, Water, Gated apps time, Exam countdown, Quick note, Brain dump, Agenda and Habit stack
- Small tiles sit two to a row. Tap the pencil, then touch and hold a tile to drag it where you want it, or tap the minus to remove it
- Add widgets from other apps, grouped by app, with previews and resizing

**Quick actions**
- **Lock In:** while on, only your Home apps show. Any other app is found only by typing its full name
- **Break (red button):** a 5 minute full-screen timer with only Call and Message. It survives closing the app. Optionally, a small red dot can show over every app (Settings, Red button) and starts the same lock after two taps
- **Gray:** opens the phone's own grayscale setting
- **Light:** flashlight
- **Silent:** Do Not Disturb
- **Focus:** opens the focus screen

**Focus and review**
- Focus session with a timer, Do Not Disturb and gated apps locked
- Evening shutdown to review the day and set tomorrow's most important task
- Weekly review of screen time, finished tasks and gates backed out of

**Look**
- Themes on their own screen: Auto, Light, Dark, Pen blue, Midnight and Notebook (with ruled lines)
- Inter font, one calm Ceramic style. See [docs/DESIGN.md](docs/DESIGN.md)

**Also in the code**
- Pip, a small text-face companion. It is switched off for now (`PIP_ENABLED`), and its code and tests are kept

Full details and design rules: [docs/DESIGN.md](docs/DESIGN.md). Plans: [docs/ROADMAP.md](docs/ROADMAP.md).

## Get the app without a PC

You can build and install it entirely from a phone.

1. Open the **Actions** tab of this repository.
2. Open the latest green **Build** run (or choose **Build**, tap **Run workflow**, then confirm).
3. Scroll to **Artifacts** and download **debug-apk**. It arrives as a zip. Extract it with your phone's file manager.
4. Tap the `.apk` to install. Android will ask you to allow installs from your browser or file manager.
5. Go to **Settings, Apps, Default apps, Home app** and choose this launcher. To go back, choose your old launcher in the same place.

If Google Play Protect blocks the install, it is because debug builds come from outside the Play Store and the app asks for optional permissions. You can switch off **Scan apps with Play Protect** in the Play Store's Play Protect settings while you install, then switch it back on.

Debug builds are for testing only. Release builds for Google Play use the [release workflow](docs/RELEASING.md).

## Build on a computer

You need JDK 17 and Android Studio (or just the Android SDK).

```bash
git clone <your fork url>
cd <repo folder>
./gradlew assembleDebug        # build the APK into app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # run unit tests
./gradlew lintDebug            # run Android lint
```

Or open the folder in Android Studio and press Run.

## Tech stack

| | |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose (custom text-first components, Material 3 only for dialogs) |
| Min / target SDK | 26 / 36 (Google Play requires targeting Android 16, API 36, for new apps and updates since 31 Aug 2026) |
| Build | Gradle (Kotlin DSL), version catalog in `gradle/libs.versions.toml` |
| CI | GitHub Actions |
| Storage | Local only (SharedPreferences) |
| Network | None. The app does not request the INTERNET permission |

## Project layout

```
.
├── app/
│   ├── build.gradle.kts            # app module build script
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # HOME intent filter lives here
│       │   ├── res/                # strings, colors, fonts, icon
│       │   └── java/com/example/productivitylauncher/
│       │       ├── MainActivity.kt
│       │       ├── data/           # state, rules, app list, system helpers
│       │       ├── service/        # floating timer and red button overlays
│       │       └── ui/             # theme, components and one folder per screen
│       └── test/                   # JVM unit tests
├── docs/                           # design, architecture, roadmap, privacy, releasing
├── gradle/libs.versions.toml       # all dependency versions
└── .github/                        # workflows, issue and PR templates
```

More detail: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Contributing

Contributions of every size are welcome: code, design, testing on different devices, or improving these docs.

1. Read [CONTRIBUTING.md](CONTRIBUTING.md) (it includes a phone-only workflow).
2. Pick an issue labeled `good first issue`, or open one to propose your idea.
3. Open a pull request. The **Build** workflow must be green.

By taking part you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Privacy and permissions

- No accounts, ads, analytics, trackers, or network access.
- All data (your tasks, notes, habits and settings) stays on your device. `allowBackup` is off.
- Every permission is optional, is explained in the app before it is requested, and the app keeps working without it:
  - **Usage access:** screen time and gated time
  - **Display over other apps:** the floating timer and the optional red button
  - **Do Not Disturb access:** the Silent tile, the moon button and focus sessions
  - **Calendar (read only):** the Agenda widget
  - **Home app:** make it your launcher

Full policy: [docs/PRIVACY.md](docs/PRIVACY.md).

## Open decisions

These are intentionally unset, so please do not hard-code guesses:

- **App name.** "Launcher" is a working title.
- **Application ID.** `com.example.productivitylauncher` is a placeholder that Google Play rejects. It can never change after the first Play upload.
- **Final icon.** The look is chosen, but the app icon is not.
- **Study Together.** Studying with friends over a nearby connection, with no internet, is an idea under discussion and is not built.

## Inspiration

Ideas come from these books. This project is not affiliated with their authors or publishers.

*Eat That Frog!* (Brian Tracy), *Atomic Habits* (James Clear), *Deep Work* and *Digital Minimalism* and *How to Become a Straight-A Student* (Cal Newport), *The One Thing* (Gary Keller with Jay Papasan), *Indistractable* (Nir Eyal), *Make Time* (Jake Knapp and John Zeratsky), *Make It Stick* (Peter Brown, Henry Roediger, Mark McDaniel), *A Mind for Numbers* (Barbara Oakley), *Getting Things Done* (David Allen), *Mindset* (Carol Dweck).

## License

[MIT](LICENSE)
