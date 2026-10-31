# AGENTS.md: everything an AI (or new contributor) needs to know

Read this file first. It describes what the project is, how it is built, the rules you must not break, and how to change things safely. `CLAUDE.md` just points here.

## 1. What this is

A minimal Android **launcher for students** (working title "Launcher", visual identity "Still"). It replaces the phone's home screen. Goals: do the next task first, put friction in front of distracting apps, keep everything private.

- Kotlin + Jetpack Compose, single module `app`, single activity.
- Package / applicationId `com.example.productivitylauncher` (a placeholder; Google Play rejects `com.example`; it cannot change after the first Play upload, so do not "fix" it without the owner).
- `minSdk 26`, `compileSdk/targetSdk 36`, Compose BOM 2025.10.00, Kotlin 2.2.21, AGP 8.13.2, JDK 17.
- **No INTERNET permission, ever.** No analytics, accounts, ads, network libraries.
- Repo: `github.com/tajmulansa/minimal-launcher`. License MIT.

## 2. The owner and how to work with them

- The owner is a student developer who has **no PC**. Everything is done from a phone. Builds happen only in **GitHub Actions** (`.github/workflows/build.yml`); the APK is downloaded from the run's artifacts.
- When you give code to the owner in chat, give the **file path and the full file/code inline**, not a zip. When you can edit the repo yourself, do that and push.
- The owner wants the app to **match the prototype exactly** (`docs/design/still-prototype.html`): sizes, spacing, copy, motion. When in doubt, compare against it. See section 9 for how to run both and compare screenshots.
- The owner reports feedback as numbered lists. Do every item, say plainly what you did, and flag anything you interpreted.
- Never put the owner's personal data into the repo.

## 3. Hard rules (do not break)

1. No `INTERNET` permission, no network code, no tracking libraries. Optional permissions must degrade gracefully when denied.
2. Gentle, never shaming. No "failed" states, no guilt copy.
3. Gates are chosen by the user. Making a gate **easier** takes 24 hours to apply (`pending_changes`); making it stricter applies at once. Never remove that delay.
4. Gate colour (`AppColors.gate`, terracotta) marks gated apps and urge/lock screens only.
5. English only for now. Student-first features only.
6. All animation goes through `Motion` (section 6) so the Settings "Animations" switch can turn everything into snaps.
7. Do not hard-code the final app name, icon or applicationId. They are open decisions.
8. Keep the UI flat and typographic: no filled cards, no shadows except the drawer sheet, hairline separators.

## 3a. UI rule: everything new must look like the app

Every new screen, widget, dialog, button, row, setting, text or animation must match the existing Still design exactly. Before building UI:
1. Open a similar existing screen and copy its structure (e.g. a new settings row copies `SettingRow`; a new page copies `PageHeader` + 28dp gutters).
2. Use only `AppColors`, the fonts (`Display`/`Sans`/`Mono`), the type scale and the components in section 6. Never raw colours, new font sizes, Material widgets, filled cards, shadows or rounded boxes.
3. Same role = same size everywhere (titles 34sp or 44sp, body 17sp, secondary 15sp, captions 11sp mono). Match alignment, spacing and hairlines of neighbouring elements.
4. Animate only with `Motion.*` so the Animations switch works.
5. Support light and dark and all four Looks. Check it with the screenshot workflow against the prototype and neighbouring screens before merging.
If a needed component does not exist, build it in `ui/components/Components.kt` in the same flat style and document it in section 6.

## 4. Repository map

```
app/src/main/
  AndroidManifest.xml          HOME intent filter, services, permissions
  res/font/                    Geist (4 weights), Geist Mono (2), Bricolage Grotesque static instances (4)
  java/com/example/productivitylauncher/
    MainActivity.kt            single activity; scales density so the screen is 390 units wide
    data/                      state, rules, app list, system helpers (no UI)
    service/                   SessionOverlayService, UrgeButtonService, NoiseService
    ui/                        theme, components, one folder per screen
app/src/test/                  JVM unit tests (AppFilterTest, LogicTest)
docs/                          ARCHITECTURE, DESIGN, PRIVACY, RELEASING, ROADMAP, font licences, design/still-prototype.html
.github/workflows/             build.yml (CI), release.yml (signed AAB), screenshots.yml (emulator screenshots)
.github/scripts/shots.sh       the screenshot script run inside the emulator
```

### Source files and what they do

| File | Role |
| --- | --- |
| `MainActivity.kt` | Edge-to-edge activity, `singleTask`. Provides `LocalDensity` scaled by `screenWidthDp / 390` so every proportion equals the prototype. `onNewIntent` bumps `homeSignal` (Home button returns to page 0). Hosts `WidgetHost`. |
| `data/LauncherState.kt` | **The single state holder.** Compose `mutableStateOf` fields backed by SharedPreferences file `launcher`. Everything user-visible lives here (section 7). |
| `data/Models.kt` | `Habit`, `Task`, `Dump`, `LauncherWidget`, `MAIN_ID`, `DEFAULT_WIDGETS`, `URGE_LOCK_MINUTES`. |
| `data/Logic.kt` | Pure Kotlin helpers (day keys, phrase matching, growing wait, formatting incl. `formatTimeOfDay`). Unit tested. |
| `data/AppRepository.kt`, `AppEntry.kt`, `AppFilter.kt` | List launchable apps via `PackageManager` (uses `<queries>`, not `QUERY_ALL_PACKAGES`); search/filter (`exactNameMatches` for Lock In). |
| `data/Usage.kt` | `UsageReader`: screen time and per-app time from UsageStatsManager (optional "Usage access"). |
| `data/System.kt` | Intents and helpers: launch app, open phone/messages, DND, overlay, default-launcher, torch, grayscale. |
| `data/WidgetHost.kt` | `AppWidgetHost` (id 1024) for widgets from other apps. |
| `data/Calendar.kt` | Leftover from the removed Agenda widget (unused; `READ_CALENDAR` is still in the manifest and can be removed). |
| `service/SessionOverlayService.kt` | Floating timer over a gated app during a session (needs "Display over other apps"). |
| `service/UrgeButtonService.kt` | Optional red dot over every app; two taps start the 5 minute lock. |
| `service/NoiseService.kt` | Foreground media service; generates white/pink/brown noise from random numbers with `AudioTrack`; `Noise` object holds UI state (`playing`, `kind`). |
| `ui/LauncherApp.kt` | Root composable: routing (`AnimatedContent` over `Route`), the two-page `HorizontalPager`, bottom bar, Quick panel, All apps drawer, Pip, Look colour filter, Back handling, app launching, urge override. |
| `ui/Nav.kt` | `Route` sealed interface and `Nav(go, back)`. |
| `ui/theme/Theme.kt` | `Palette` (light/dark), `AppColors`, fonts `Sans`/`Display`/`Mono`, `ThemeMode`, `Look` + `lookFilter`. |
| `ui/theme/Motion.kt` | The animation system (section 6), `enterStagger`, `Intro.played`. |
| `ui/components/Components.kt` | Design-system pieces (section 5). `Icons.kt`: `Ic` enum of SVG path icons. |
| `ui/home/HomeScreen.kt` | Home page, `BottomBar` (dock + Home/Study tabs), `Hair`, `verticalSwipes`. |
| `ui/home/QuickPanel.kt` | Top sheet: Silent, Lock in, Red button, Animations toggles; noise player; Theme and Look segmented controls. |
| `ui/today/TodayScreen.kt` | The **Study** page (file name is historical): focus ring, week dots, screen-time bars, to-do list, habit stack, external widgets. |
| `ui/apps/AppListScreen.kt` | `AppDrawer`: the All apps bottom sheet (search, A-Z, long-press options). `PickerScreens.kt`: gated-apps and home-apps pickers. |
| `ui/gate/GateScreens.kt` | Gate (3 steps), Locked, Time's up. |
| `ui/urge/UrgeScreen.kt` | Full-screen "Take a break" lock. |
| `ui/focus/FocusScreens.kt`, `ReviewScreens.kt` | Focus session, Evening shutdown, Weekly review. |
| `ui/settings/SettingsScreen.kt`, `Privacy.kt` | Settings and policy text. |
| `ui/onboarding/OnboardingScreen.kt` | 7-step setup. |
| `ui/widgets/AddWidgetScreen.kt`, `BrainDumpScreen.kt` | Add widget (Habit stack + other apps' widgets); brain dump (only reachable from Pip, which is off). |
| `ui/pip/Pip.kt` | Text-face companion. `PIP_ENABLED = false`; code and tests kept. |

## 5. Screens and behaviour

Navigation: `Route.Main` is two horizontal pages in a `HorizontalPager`: **Home** (page 0) and **Study** (page 1, formerly "Today"). Other `Route`s are full screens that replace Main with a soft fade. Overlays on Main: bottom bar, Quick panel, All apps drawer.

- **Home:** date (plain mono text, 13sp), settings-sliders icon at top right (opens **Quick actions**), 112sp odometer clock, greeting ("Good morning/afternoon/evening, <name>." or "Rest well." between bedtime and wake time), "Up next" (first undone task; tap checks it after 520 ms), up to six Home apps (28sp, no scrolling, usage minutes on the right, gated apps are excluded from this list). Swipe up anywhere opens All apps; swipe down from the top 320dp opens Quick actions (`verticalSwipes` observes at the Initial pass and never consumes, so the pager keeps working).
- **Bottom bar** (both pages): "All apps" pill, Phone, Messages, Focus (scrolls to Study) and the Home/Study tabs whose underline follows the pager position and label widths.
- **All apps drawer:** 88% height sheet, 30dp top corners, drag the handle down >110dp to close, veil 28%, 46dp search pill, sticky letters, 24sp rows, right-aligned "gated" caption. Lock In makes search exact-name only. Long-press: gate/ungate (24h delay), add/remove Home, app info. Tapping an app closes the drawer, waits 250 ms, then `openApp`.
- **Study page:** header "Study" + "d/N done" + "+" (Add widget) + settings icon (Settings). H1 "Focus", 236dp ring, −5 / Start / +5, week dots + streak, screen-time bars (needs Usage access), **Tasks** (prototype list: 30dp check circle, animated strike line, 40% opacity when done, hairline per row, "+ Add a task"; long-press deletes), optional **Habit stack** widget, then widgets from other apps (each with a Resize slider and Remove).
- **Habit stack widget:** added from Add widget (built-in row at the top). "After <anchor> → <action>", one check per day, streak in days, chain line between rows, long-press deletes. Data: `Habit` in `LauncherState.habits`.
- **Quick actions:** Silent (DND), Lock in, Red button, Animations; **Noise player** (previous / play-pause / next cycles White, Pink, Brown); Theme (Auto/Light/Dark) and Look (Standard/Gray/Warm/Contrast).
- **Gate** (for gated apps): 1) breathe (`breaths` setting, default 3, orb 250dp, 3 s cubic-bezier(.45,0,.3,1); optional growing wait of 5 s per open that day) then auto-advance; 2) type the fixed phrase **"I choose to open <App>"** (case-insensitive, no paste, keyboard type Password to disable suggestions); 3) choose minutes (5/10/15/30 limited by the daily limit, or custom). A floating timer then runs; when time is up, **Time's up**. Daily gated limit default 60 min. Focus sessions can lock gated apps (**Locked** screen).
- **Urge (red button) lock:** 5 minutes, full screen, only Call and Message; survives closing the app (`urge_end`).
- **Focus, Evening shutdown, Weekly review:** see `FocusScreens.kt` / `ReviewScreens.kt`.
- **Setup (onboarding)** steps: 0 welcome, 1 pick gated apps, 2 pick Home apps, 3 name, 4 wake and bedtime (also sets evening shutdown to bed hour − 1), 5 theme and look, 6 privacy and permissions.

## 6. Design system (must be used, not re-invented)

- **Palette:** `AppColors.*` (`phone`, `card`, `text`, `muted`, `focus`, `gate`, `line`, `dot`, ...). Light: phone `#ECEDEA`, text `#111312`, muted `#7B7F79`, gate `#B5502C`. Dark: phone `#0B0C0B`, text `#EEF0EC`, muted `#777B74`, gate `#E27A52`. Hairline = text at ~13% alpha. Never use raw colours in screens.
- **Look** (Standard/Gray/Warm/Contrast) is a colour filter on the root layer (`graphicsLayer` + `ColorFilter` + offscreen compositing), not part of the palette.
- **Fonts:** `Display` = Bricolage Grotesque (large text), `Sans` = Geist (body), `Mono` = Geist Mono (captions, numbers, times). `AppText` picks Display automatically at >= 19sp unless `display` is set. Use `tnum = true` for clocks and timers.
- **Type scale** (keep titles identical everywhere): page title 44sp Medium (-2sp tracking); step/screen heading 34sp Medium; big numbers 56-60sp Light; clock 112sp Light; list names 28sp (Home) / 24sp (drawer, pickers); body 17sp; secondary 15sp (muted); small 13sp; `Caption` 11sp mono uppercase, 1sp tracking.
- **Spacing:** 28dp screen gutters; 22dp around hairline dividers; rows 14dp vertical padding.
- **Components** (`ui/components/Components.kt`): `AppText`, `Caption`, `Rule`, `Hair` (animated hairline), `Modifier.hairlineBelow()`, `clickableRole`, `PageHeader(title, onBack, label, trailing)`, `IconButtonLarge` (40dp circle), `Segmented`, `Toggle`, `Stepper`, `CheckCircle`, `CButton`, `TextLink`, `AppField`, `SettingRow` (+ `LocalRowLines` to hide row hairlines), `SettingsGroup`, `ValueChevron`, `Rolling`. Icons are SVG path strings in `Ic`; add new ones there.
- **Motion** (`Motion.kt`): `Motion.state/enter/exit/morph/fill` are already slowed 1.9x to match the prototype; `Motion.slow(ms)` is exact; `Motion.press/lift` are springs. When `Motion.enabled` is false every spec becomes `snap()`. Use `tween<Float>(...)`/`snap()` explicitly when type inference fails. Staggered entrance: `Modifier.enterStagger(index, once, rise)`. The Settings "Animations" toggle and Quick actions toggle write `state.animations`.
- There is **no app-opening animation** (removed on request). Apps launch straight away.

## 7. Data and persistence

Everything is in `LauncherState` (constructor reads SharedPreferences `launcher`; setters call `save { ... }`). Patterns: `var x by mutableStateOf(str("key"))` with `private set` plus `fun updateX(...)` (do not name a function `setX` for a property `x`: JVM signature clash).

Keys: `onboarded, user_name, wake_min, bed_min, theme, look, animations, frog, frog_done_day, frog_done_at, tomorrow_frog, frog_minutes, eaten_days, tasks, habits, dumps, note, widgets, widgets_v3, widget heights, home_apps, gated, pending_changes, limit, breaths, phrase_words (no longer used by the gate), growing_wait, opens_day, session_pkg, session_end, session_notified, booked_day, booked_ms, focus_end, focus_total, focus_dnd, focus_lock, lock_in, urge_button, urge_end, pip_on, water, water_goal, exam_name, exam_day, screen_goal, evening_hour, evening_done, last_day`.

- **Tasks:** the UI shows one plain list `state.allTasks`. Historically one task was the "frog" (most important): it is stored in `frog` and shown first (`MAIN_ID`), the rest in `tasks`. Pip, Focus, Evening and the streak (`eatenDays`, set when the first task is done) still use the frog. Use `addAny/toggleAny/removeAny/nextTask`; `tasksLocked` is always false.
- `rollover()` resets daily counters; `refreshUsage()` fills `screenTimeMs` and `appMsToday`; `applyPending()` applies due gate changes; `syncSession/consumeTimesUp` manage gated sessions.
- "Delete all my data" calls `resetAll()` then shows onboarding.

## 8. Build, CI and release

- Local: `./gradlew assembleDebug testDebugUnitTest lintDebug`.
- **CI** (`build.yml`) runs on push to `main`, PRs and manual dispatch: assemble debug, unit tests, lint, uploads `debug-apk`. On failure it writes the compiler errors as an annotation titled "Build errors".
- **Workflow used for every change:** branch `still-ui-N` -> commit -> push -> run Build (workflow_dispatch on the branch) -> read result/annotations -> fix -> fast-forward merge into `main`.
- `workflow_dispatch` only works for workflows that exist on `main`.
- **Release** (`release.yml`): signed AAB/APK using repository secrets (`docs/RELEASING.md`).
- Commit trailers used in this project:
  `Co-Authored-By: Claude <noreply@anthropic.com>` (adjust to the model used).

## 9. Comparing the app with the prototype (visual QA)

There is no emulator on the owner's phone-only setup or in most AI sandboxes, so the repo has a screenshot workflow:

1. `screenshots.yml` (manual dispatch, any branch) builds the APK, boots an API 34 Pixel 5 emulator (KVM), runs `.github/scripts/shots.sh` and force-pushes the PNGs to the branch **`shots-out`** (artifacts are not downloadable from some sandboxes, a git branch is).
2. `shots.sh` seeds SharedPreferences (name, tasks, home apps, a gated app, habits), then captures Home, Study, drawer, search, gate, quick actions. Edit the seed there to test other states.
3. Render the prototype with Playwright (Chromium) at 390x844 with device scale ~2.77, take the same screens, and put the two images side by side.
4. Known gotchas: the script taps tabs by text (it now taps `STUDY`; update it if the label changes); the emulator has no usage data, so screen time shows empty.

## 10. Testing and quality

- Unit tests (`app/src/test/.../data`) cover app filtering and the pure logic in `Logic.kt`. Add tests when you add pure logic.
- UI is verified with the screenshot workflow and on a real phone.
- Check: dark and light, all four Looks, animations off, small and large font size, a 360dp wide phone.

## 11. Compose and Kotlin pitfalls seen here

- `@Volatile` is not allowed on delegated (`by mutableStateOf`) properties.
- `KeyboardType` has `Password` (no `VisiblePassword`); Password also disables suggestions.
- Duplicate or ambiguous imports (e.g. two `Offset`) break the build; Kotlin accepts duplicate identical imports, not different classes with the same name.
- A `Column` inside a fixed-height `Box` is clipped to the box: use a custom `layout` (see the odometer in `HomeScreen.kt`) or `wrapContentSize(unbounded = true)`.
- `AppText` ellipsizes; in a too-narrow box digits turn into "...". Give text room.
- Do not read `pager.currentPageOffsetFraction` in composition of a big tree; read it in `graphicsLayer {}` or in a small composable.
- Pages in the pager must be `clipToBounds()` before parallax `translationX`, or the next page peeks in.

## 12. Open decisions and known leftovers

- App name, application ID and final icon are not decided.
- `data/Calendar.kt` and the `READ_CALENDAR` permission are unused since the Agenda widget was removed; the Add widget screen no longer lists the old built-in widgets (`LauncherWidget` still defines them).
- Brain dump is only reachable from Pip (disabled).
- `phrase_words` / `GateSetting.PhraseWords` are unused now.
- Study Together (nearby, no internet) is only an idea.
- The page file/class names `TodayScreen`/`TodayScreen.kt` and docs sometimes still say "Today"; the user-facing name is **Study**.

## 13. Checklist for adding a feature

1. Does it respect section 3? (No network, gentle, user in control.)
2. Add state to `LauncherState` with a pref key and an `updateX` function; read it in the UI.
3. Build the UI from the design system (section 6); use `Motion.*` for animation; match the prototype type scale.
4. Put pure rules in `data/Logic.kt` and add a unit test.
5. Update this file, `README.md` and `docs/PRIVACY.md` if data or permissions change.
6. Branch, push, run CI, run the screenshot workflow for UI changes, then merge.
