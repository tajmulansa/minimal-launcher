# Launcher (working title)

[![Build](../../actions/workflows/build.yml/badge.svg)](../../actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A minimal Android launcher made for students. It helps you study without your phone pulling you away: your next task is always on Home, distracting apps wait behind a short pause, and everything stays private on your phone.

> **Status: alpha.** It builds in CI on every change and is changing quickly. The app name, final icon and application ID are not decided yet (see [Open decisions](#open-decisions)).
>
> **Using an AI assistant to change this project?** Tell it to read [AGENTS.md](AGENTS.md) first. It explains the architecture, design system, rules and workflow.

## Why this exists

Phones are designed to pull attention away from what matters. Most "focus" apps either block everything (and get uninstalled) or only report screen time (and change nothing). This launcher takes a middle path:

- **One list, always in front of you.** Home shows your next task. Check it off from Home.
- **Friction where it counts.** Apps you mark as distracting wait behind a breath, a typed phrase and a time choice. Your everyday apps open in one tap.
- **Help for hard moments.** Focus sessions, Lock In, calming noise and a red button.
- **Private by design.** No account, no ads, no analytics, no internet permission.

## What is built

**Home** (page 1)
- Rolling 112sp clock, date, a greeting with your name ("Rest well." between your bedtime and wake time)
- "Up next": your first unfinished task. Tap to complete it
- Up to six Home apps you choose, with minutes used today
- Swipe up for **All apps**. Swipe down from the top (or tap the sliders icon) for **Quick actions**

**Study page** (page 2, swipe left or tap Study)
- Focus ring with a timer (-5 / Start / +5), week dots and streak, screen time by app
- **Tasks:** a plain to-do list. Tap to check, long-press to delete
- **Habit stack** widget: "After I wake up, I drink water". One check a day, with streaks. Add it with the + button
- Widgets from other apps, each resizable (slider) and removable

**All apps drawer**
- Bottom sheet with search and an A to Z list. Long-press an app to gate it, add it to Home or open its info

**Gate for distracting apps**
- One breath, then type "I choose to open <app>", then pick how long to stay (5, 10, 15, 30 or custom minutes)
- Daily gated limit (1 hour by default), optional longer wait on each open, floating timer over the app
- Making the gate easier takes 24 hours to apply. Making it stricter applies at once

**Quick actions**
- Silent (Do Not Disturb), Lock In (only Home apps, others found by typing the full name), Red button, Animations
- **Noise player:** previous / play-pause / next for white, pink and brown noise made in code (plays with the screen off)
- Theme (Auto, Light, Dark) and Look (Standard, Gray, Warm, Contrast)

**Focus and review**
- Focus screen, Evening shutdown (set from your bedtime), Weekly review
- Red button: a 5 minute full-screen break with only Call and Message

**Setup**
- Seven short steps: welcome, apps to pause, Home apps, your name, wake and bedtime, theme and look, privacy and permissions

**Look**
- "Still": flat, typographic, hairlines instead of boxes. Fonts: Geist, Geist Mono and Bricolage Grotesque (all SIL OFL). The prototype is [docs/design/still-prototype.html](docs/design/still-prototype.html). Details: [docs/DESIGN.md](docs/DESIGN.md)

Also in the code: Pip, a small text-face companion, switched off for now (`PIP_ENABLED`).

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
| Language | Kotlin 2.2 |
| UI | Jetpack Compose (Compose BOM 2025.10.00), custom flat components, Material 3 only for dialogs |
| Min / target SDK | 26 / 36 (Google Play requires targeting Android 16, API 36, for new apps and updates since 31 Aug 2026) |
| Build | Gradle (Kotlin DSL), AGP 8.13, version catalog in `gradle/libs.versions.toml` |
| CI | GitHub Actions: build, release, emulator screenshots |
| Storage | Local only (SharedPreferences file `launcher`) |
| Network | None. The app does not request the INTERNET permission |

## Project layout

```
.
├── AGENTS.md                       # read this first if you are an AI assistant
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # HOME intent filter, services, permissions
│       │   ├── res/                # strings, colors, fonts, icon
│       │   └── java/com/example/productivitylauncher/
│       │       ├── MainActivity.kt
│       │       ├── data/           # LauncherState, rules, app list, system helpers
│       │       ├── service/        # floating timer, red button, noise player
│       │       └── ui/             # theme, components, one folder per screen
│       └── test/                   # JVM unit tests
├── docs/                           # architecture, design, privacy, releasing, roadmap, prototype
└── .github/                        # workflows and the screenshot script
```

More detail: [AGENTS.md](AGENTS.md) and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Privacy and permissions

- No accounts, ads, analytics, trackers, or network access.
- All data (tasks, habits, notes, settings, your name and sleep times) stays on your device. `allowBackup` is off.
- Every permission is optional, explained in the app, and the app keeps working without it:
  - **Usage access:** screen time and gated time
  - **Display over other apps:** the floating timer and the optional red button
  - **Do Not Disturb access:** the Silent tile and focus sessions
  - **Notifications and foreground service (media playback):** only for the noise player's "Noise is playing" notification
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
