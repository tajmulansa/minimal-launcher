# Launcher (working title)

[![Build](../../actions/workflows/build.yml/badge.svg)](../../actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A minimal, text-first Android launcher that helps students focus. It is built on ideas from productivity books: do the hardest task first, start small, and make distracting apps harder to open than useful ones.

> **Status: pre-alpha.** This is an early scaffold. The home screen and app list are written, but **the project has not been through its first CI build yet**, so expect small build fixes first (roadmap M1). The gate, pet, and widgets described below are planned and not built yet. The visual direction is the **Ceramic prototype** in [docs/design](docs/design/README.md) (open it in a browser). The app does not use it yet. See [docs/ROADMAP.md](docs/ROADMAP.md).

## Why this exists

Phones are designed to pull attention away from what matters. Most "focus" apps either block everything (and get uninstalled) or only report screen time (and change nothing). This launcher takes a middle path:

- **One important task per day.** Not a to-do list, just a single "frog" (the idea from *Eat That Frog*). It sits at the top of the home screen.
- **Friction where it counts.** Apps the *user* marks as distracting are slow to open. Work apps open in one tap.
- **Gentle, never shaming.** A small text-face pet nudges the user based on what they are doing. It cheers wins and never punishes.
- **Private by design.** No account, no ads, no analytics, no internet permission. Everything stays on the device.

## What exists today

- Registers as an Android home screen (HOME launcher)
- Live clock and date
- One persistent "frog" field (stored locally)
- Searchable, text-only list of all installed apps, tap to launch
- GitHub Actions build that produces an installable debug APK, plus unit tests and lint

## Planned

| Area | Idea | Inspired by |
| --- | --- | --- |
| Frog card | One most important task, a "start 2 minutes" button, an optional 25-minute focus timer | *Eat That Frog*, *Atomic Habits* |
| Gated apps | User-chosen distracting apps shown in red; opening one asks for a reason, a typed phrase, and a wait that grows with each open | *Indistractable*, *The One Thing* |
| Pet | A friendly text-face companion that reacts to behavior (long phone use, frog undone, late night) with a hard daily cap | *Atomic Habits*, *Mindset* |
| Evening shutdown | Review the day and set tomorrow's frog before sleep | *Deep Work*, Ivy Lee method |
| Tools page | Habit stack, quiz yourself, focus timer, phone-time mirror, exam countdown, brain dump, weekly review | *Make It Stick*, *A Mind for Numbers*, *Getting Things Done*, *Digital Minimalism* |

Full details and design rules: [docs/DESIGN.md](docs/DESIGN.md).

## Get the app without a PC

You can build and install it entirely from a phone.

1. Open the **Actions** tab of this repository.
2. Choose the **Build** workflow, tap **Run workflow**, then confirm.
3. Wait about 5 to 10 minutes for it to finish (green check).
4. Open the finished run, scroll to **Artifacts**, and download **debug-apk**. It arrives as a zip. Extract it with your phone's file manager.
5. Tap the `.apk` to install. Android will ask you to allow installs from your browser or file manager.
6. Go to **Settings, Apps, Default apps, Home app** and choose this launcher. To go back, choose your old launcher in the same place.

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
| UI | Jetpack Compose (Material 3 theme, custom text-first components) |
| Min / target SDK | 26 / 36 (Google Play requires targeting Android 16, API 36, for new apps and updates since 31 Aug 2026) |
| Build | Gradle (Kotlin DSL), version catalog in `gradle/libs.versions.toml` |
| CI | GitHub Actions |
| Storage | Local only (SharedPreferences now, DataStore if needed later) |
| Network | None. The app does not request the INTERNET permission |

## Project layout

```
.
├── app/
│   ├── build.gradle.kts            # app module build script
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # HOME intent filter lives here
│       │   ├── res/                # strings, colors, icon
│       │   └── java/com/example/productivitylauncher/
│       │       ├── MainActivity.kt
│       │       ├── data/           # app list, filtering, local storage
│       │       └── ui/             # theme, components, home, apps
│       └── test/                   # JVM unit tests
├── docs/                           # design, architecture, roadmap, releasing
├── gradle/libs.versions.toml       # all dependency versions
└── .github/                        # workflows, issue and PR templates
```

More detail: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Contributing

Contributions of every size are welcome: code, design, writing the pet's lines, testing on different devices, or improving these docs.

1. Read [CONTRIBUTING.md](CONTRIBUTING.md) (it includes a phone-only workflow).
2. Pick an issue labeled `good first issue`, or open one to propose your idea.
3. Open a pull request. The **Build** workflow must be green.

By taking part you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Privacy and permissions

- No accounts, ads, analytics, trackers, or network access.
- All data (such as your frog text) stays on your device. `allowBackup` is off.
- Planned optional permissions, each explained in-app before it is requested: **Usage Access** (total phone time) and **Notifications** (pet reminders). The app must keep working without them.

## Open decisions

These are intentionally unset, so please do not hard-code guesses:

- **App name.** "Launcher" is a working title.
- **Application ID.** `com.example.productivitylauncher` is a placeholder that Google Play rejects. It can never change after the first Play upload.
- **Final icon and name.** The look is chosen (Ceramic, Inter, light and dark), but the app icon and name are not.
- **Pip.** The pet is a text face named Pip. More lines and behavior still to design.

## Inspiration

Ideas come from these books. This project is not affiliated with their authors or publishers.

*Eat That Frog!* (Brian Tracy), *Atomic Habits* (James Clear), *Deep Work* and *Digital Minimalism* and *How to Become a Straight-A Student* (Cal Newport), *The One Thing* (Gary Keller with Jay Papasan), *Indistractable* (Nir Eyal), *Make Time* (Jake Knapp and John Zeratsky), *Make It Stick* (Peter Brown, Henry Roediger, Mark McDaniel), *A Mind for Numbers* (Barbara Oakley), *Getting Things Done* (David Allen), *Mindset* (Carol Dweck).

## License

[MIT](LICENSE)
