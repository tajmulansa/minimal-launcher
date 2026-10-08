# Roadmap

A rough plan, not a promise. Priorities may change as we learn. Tick a box in a pull request when you finish an item.

## M0: Scaffold (done)

- [x] Launcher registers as a home screen
- [x] Clock, frog field, searchable app list
- [x] GitHub Actions build with debug APK, unit tests and lint
- [x] README, contributing guide, design and architecture docs

## M1: Solid home and apps

- [ ] First CI run is green on the scaffold (fix any version issues)
- [ ] Pinned apps on Home (user chooses 4 to 5)
- [ ] Work apps shown first and in bold in the app list
- [ ] Restyle the app to the Ceramic prototype (light and dark themes, Inter bundled)
- [ ] Empty states and basic accessibility pass (TalkBack labels, font scaling)

## M2: The frog

- [ ] One-task rule enforced (replace or complete before adding another)
- [ ] "Start 2 min" button and 25-minute focus timer
- [ ] Mark done, with a calm confirmation
- [ ] Evening shutdown screen to set tomorrow's frog

## M3: Gated apps

- [ ] Settings to choose gated apps and limits
- [ ] Red names with a text tag in the app list
- [ ] Gate screen: reason (min 40 characters, no paste), typed phrase, doubling wait
- [ ] Session length and daily limit with a Locked screen
- [ ] 24-hour delay when weakening a rule
- [ ] Pure Kotlin rule engine with unit tests

## M4: Pet

- [ ] Pet engine with daily cap, cooldowns and quiet mode (pure Kotlin, fully tested)
- [ ] Launcher-only triggers (opens, frog undone, evening)
- [ ] Notifications (optional permission, explained in-app)
- [ ] Optional Usage Access for long phone-use and late-night triggers
- [ ] Decide pet species and name

## M5: Tools page

- [ ] Page 2 with user-selectable widgets
- [ ] Habit stack
- [ ] Quiz yourself (spaced questions)
- [ ] Phone time and exam countdown
- [ ] Brain dump
- [ ] Weekly review

## M6: Release prep

- [ ] Final app name and application ID (the ID can never change after first upload)
- [ ] Final app icon
- [ ] Privacy policy page
- [ ] Play listing (no third-party brand names in title, description or screenshots)
- [ ] Signed release build, internal testing track, then production

## Good first issues

Small, well-defined tasks for new contributors:

- Add a unit test for an edge case in `filterApps`
- Improve `contentDescription`s for TalkBack
- Show a friendly message when the search has no results
- Add a README screenshot once the look is stable
- Write 10 pet lines following the rules in `CONTRIBUTING.md`
- Try the debug APK on your phone and report what you find
- Investigate bumping to Android Gradle Plugin 9 on a branch

## Open questions

- App name and application ID
- Pip's lines and exact behavior
- App icon
- Whether the pet should ever use AI, or stay fully rule-based and offline (current answer: offline)
