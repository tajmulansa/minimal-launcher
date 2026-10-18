# Design

> **The visual direction is the Ceramic prototype**: [design/ceramic-prototype.html](design/ceramic-prototype.html). Open it in a browser and tap through the screens. The app, icon, and name are still open decisions. The *principles* and *behavior rules* below are the lasting part.

## Principles

1. **Minimal.** Text first, few elements, lots of space.
2. **One thing.** One important task, one clear next action per screen.
3. **Friction is asymmetric.** Distracting apps are hard to open, work apps are one tap.
4. **Gentle.** Encourage, never shame. The user can always leave the gate screen with one tap.
5. **Honest and private.** Local data only, plain-language permission explanations.
6. **Students.** Language and features fit study, planning, and sleep.

## Visual language (Ceramic)

Warm, soft, and quiet. Light and dark themes share the same structure. In the prototype every color is a CSS variable.

| Token | Light | Dark | Use |
| --- | --- | --- | --- |
| `bg-phone` | `#F2F0E9` | `#1B1A18` | Screen background |
| `bg-card` | `#FFFFFF` | `#252320` | Cards, rows, dock |
| `text-main` | `#363431` | `#EDEAE3` | Main text, primary button |
| `text-muted` | `#6F6C66` | `#A09B92` | Labels, secondary text |
| `accent-focus` | `#4F7165` | `#8DB8A7` | Frog, focus, done states (sage) |
| `accent-gate` | `#B85C4E` | `#E58E7E` | **Gated apps only** (terracotta) |
| `accent-gate-light` | `#FAECEB` | `#3A2723` | Background of a gated row |
| `line` | `#EAE8E3` | `#33302C` | Dividers, tracks |

- **Typeface:** Inter only, weights 400 to 700. Bundle it in the app (SIL Open Font License). Use tabular figures for the clock, timers, and screen time.
- **Shape:** large radii (16, 24, and 36 dp, plus full pills), soft shadows, no hard outlines.
- **Themes:** Light, Dark, and Auto (follows the system). The choice is in Settings and on the Welcome screen.
- **App icons:** a rounded square showing the app's first letter. Gated apps use the terracotta fill.
- **Accessibility:** red is never the only signal. Gated apps also carry a "Locked" tag. Touch targets are at least 44dp. Text meets 4.5:1 contrast in both themes.

## Screens

All of these are drawn in the prototype and now built in the Android app.

Known limits: the gate only guards gated apps opened from this launcher (Android does not let a launcher block an app opened from a notification or Recents without an accessibility service). The floating timer needs "Display over other apps", screen time needs "Usage access", the Agenda widget needs calendar permission, and the Moon button needs Do Not Disturb access. Each is optional and asked for in Settings.

| Screen | Status | Purpose |
| --- | --- | --- |
| Home | Built | Big centred round clock (opens Settings), date, today's screen time, six apps, dock with Phone, Messages, Do Not Disturb, Focus, and the floating Pip. The to-do list is **not** here |
| All apps | Built | Search, filter chips (All, Work, Gated), A to Z list |
| Widgets (page 2) | Built | To-do list (many tasks, one frog first), water, distraction budget, agenda, habit stack, quick note, brain dump, quick actions (a grid of square buttons; Lock In first) |
| Add widget | Built | Launcher widgets and other apps' widgets |
| Brain dump | Built | Capture thoughts, sort them later |
| Gate | Built | Three calm steps before a gated app opens (see below) |
| App session | Built | Small floating timer over the gated app, then Time's up |
| Daily limit reached | Built | Shown when a gated app's daily limit is used up |
| Focus session | Built | Timer ring for the frog, with Do Not Disturb and gated apps locked |
| Evening shutdown | Built | Review the day, set tomorrow's frog |
| Weekly review | Built | Screen time by day, frogs eaten, gates backed out of |
| Welcome | Built | Meet Pip, pick gated apps, first frog, theme |
| Settings | Built | Appearance (five themes: Auto, Light, Dark, Pen blue, Midnight, on their own Themes screen), rules, gate, focus, apps, Pip, permissions, data |
| Gated apps, Home apps | Built | Pickers for which apps are gated and which six sit on Home |

## The frog

- Exactly **one** most important task. A second one cannot be added until the first is done or replaced.
- It should be the hardest or most avoided task (from *Eat That Frog*).
- Set it the evening before when possible (Ivy Lee method), so the morning needs no decisions.
- Actions: **Start 2 min** (starts tiny, from *Atomic Habits*) and an optional **25:00 focus** timer.
- If the frog is undone, the app only **reminds** the user. It does not lock anything because of it.

## Gated apps

The user picks which apps are gated. Planned friction, in order:

1. Gated apps look different on Home and in All apps (terracotta, with a "Locked" tag).
2. Opening one starts the gate. **Step 1, breathe:** three slow breaths with a calm animation.
3. **Step 2, type:** a random phrase of five words, typed exactly, with pasting turned off.
4. **Step 3, set a limit:** the user chooses 5, 10, 15, or 30 minutes, or a custom time.
5. A small timer floats over the app. When time is up, the user returns to the launcher.
6. The **wait before the next open grows** (for example 1 minute, then 2, then 4).
7. A **daily limit** (default 1 hour) locks gated apps until the next morning, with no override button.

Two rules keep this fair:

- **Cancel is always the easiest, brightest button** on the gate screen.
- **Weakening a rule takes effect after 24 hours**, so it cannot be switched off in a weak moment. Making it stricter is instant.

All numbers are defaults the user can change.

## The pet

A plain text face, `[^_^]`, named **Pip**. Pip floats on Home and Widgets as a small bubble that blinks now and then. Tapping it opens a chat-style call-out: typing dots first, then the message types itself out. If nobody touches it for a few seconds it tucks itself against the right edge of the screen, half hidden and faded. A red dot on the tucked-away icon means Pip has something to say. Touch it and it slides back out; touch it again to read the message.

**Voice:** friendly, short, specific, encouraging. Never guilt, never scary, no "failure" state, nothing dies or gets sad forever.

**Pip's job:** reminder, supporter and motivator. It always knows the time of day: "Good morning" only in the morning, an afternoon check-in, an evening wind-down and sleep talk at night. It appreciates what you finish, and right after you eat the frog it says well done and suggests water and a short break. Pip runs on the phone with no network and is not an AI model.

**Behavior-driven reminders:**

| Trigger | Example line |
| --- | --- |
| Opening the launcher in the morning | "morning! start with just 2 minutes." |
| Frog undone after lunch / in the evening | "your frog is still waiting. 2 minutes?" |
| 45 minutes of continuous phone use (default) | "45 minutes straight. stretch your eyes?" |
| Phone use late at night | "it's 1 am. sleep helps you remember what you studied." |
| Gate cancelled | "urge passed. nice." |
| Focus timer finished | "good session. stand up for 2 minutes?" |
| Evening | "set tomorrow's frog before you sleep." |

**Limits that protect the user:**

- Hard cap of about **4 messages per day** in total
- A cooldown per message type
- If the user dismisses a message type 3 times, it speaks about it less
- Quiet hours and a one-tap **quiet mode**
- Usage-based triggers need the optional Usage Access permission. Without it, the pet uses only what the launcher itself can see (opens, unlocks, the frog)

## Planned widgets (Tools page)

| Widget | What it does | Source idea |
| --- | --- | --- |
| Habit stack | "After [habit], I will [action]" with a streak chain | *Atomic Habits* |
| Quiz yourself | After studying, write questions that come back later | *Make It Stick* (retrieval and spacing) |
| Focus timer | 25 minutes on, 5 off, with session count | *A Mind for Numbers* (Pomodoro) |
| Phone time | Today's phone time in plain numbers, no judgment | *Digital Minimalism* |
| Exam countdown | Days left, also feeds the pet's lines | Planning |
| Brain dump | One line to capture a thought and clear your head | *Getting Things Done* |
| Weekly review | Sunday summary: frogs done, gate openings cancelled | *Getting Things Done* |

The home screen stays calm. Widgets live on page 2, and the user chooses which are on.

## Evidence note

Prefer techniques with solid research behind them (starting small, planning ahead, retrieval practice, spaced study). Avoid popular claims that do not hold up, such as "habits take 21 days".

## Lock In mode

Turned on and off from the Quick actions widget. While it is on, Home shows only the Home apps. The All apps list stays empty until the exact full name of an app is typed, then that app appears. The Home app picker is locked and "Add an app" is hidden.

## Red button

A red tile in the Quick actions widget, for a moment when you know you should not be on your phone. After one confirmation it closes everything and shows a full-screen 5 minute timer with only a Call button and a Message button. Pressing Home or Back does not leave it, and it survives closing the app. Known limit: the launcher cannot stop an app that is opened from outside it (Recents, a notification), because Android only allows that with an accessibility service.

### Red button in every app

An optional setting (Settings > Red button). A small red dot sits against the right edge over every app. Tap it, then tap again within 3 seconds, and the launcher opens straight into the 5 minute lock. The dot is hidden while the launcher itself is on screen. It needs "Display over other apps". Known limit: it is drawn by a background service, so if Android stops that service the dot disappears until the launcher is opened again.

## iOS-style look (default)

Auto, Light and Dark follow Apple's interface conventions. The Ceramic look is still available as a theme.

- **Colour:** system grouped background (`#F2F2F7`, true black in dark), white or `#1C1C1E` cards, system blue as the tint, a darker red for text so it stays readable.
- **Titles:** large bold titles with tight tracking. Inner pages show a tint-coloured "Back" link above the title.
- **Lists:** flat grouped cards (no shadow), hairline separators, 17 sp row text, small uppercase section headers.
- **Controls:** a 51 x 31 green switch with a white thumb and a 48 dp tap area, a segmented control with a raised selected segment, tinted soft buttons, capsule chips, rings that fill in the tint colour.
- **Feel:** press-down feedback on every tap, springs that can be interrupted, a haptic tick on switches.
- Themes with `tinted = true` get this look. The accent colour then also tints buttons and selections.
