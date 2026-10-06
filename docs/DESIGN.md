# Design

> **Everything visual here is a placeholder.** The look (dark, terminal-like) was chosen to explore ideas and will be revisited. The *principles* and *behavior rules* are the lasting part.

## Principles

1. **Minimal.** Text first, few elements, lots of space.
2. **One thing.** One important task, one clear next action per screen.
3. **Friction is asymmetric.** Distracting apps are hard to open, work apps are one tap.
4. **Gentle.** Encourage, never shame. The user can always leave the gate screen with one tap.
5. **Honest and private.** Local data only, plain-language permission explanations.
6. **Students.** Language and features fit study, planning, and sleep.

## Visual language (placeholder)

| Token | Value | Use |
| --- | --- | --- |
| `Background` | `#0E0E0E` | Screen background |
| `Text` | `#D6D6D6` | Main text |
| `Muted` | `#7D7D7D` | Labels, secondary text (4.7:1 on Background) |
| `Line` | `#2A2A2A` | Thin dividers |
| `Danger` | `#FF6B5E` | **Gated apps only** (6.6:1 on Background) |

- **Typeface:** monospace. Currently the system monospace. Bundling an open-license font (for example JetBrains Mono, OFL) is a candidate task.
- **Language:** lowercase for app names and small labels, `> ` prefix for section labels.
- **Shape:** thin lines instead of cards, square-ish buttons with a 1px outline. The primary action can be a filled light button.
- **Accessibility:** red is never the only signal. Gated apps also carry a text tag such as "wait 60s". Touch targets are at least 48dp.

## Screens

| Screen | Status | Purpose |
| --- | --- | --- |
| Home | Built (basic) | Clock, the frog, the pet's line, pinned apps, links to Tools and All apps |
| All apps | Built (basic) | Search and launch. Planned: work apps first in bold, gated apps in red at the bottom |
| Tools (page 2) | Planned | Widgets: habit stack, quiz yourself, focus timer, phone time, exam countdown, brain dump |
| Gate | Planned | Friction before opening a gated app (see below) |
| Locked | Planned | Shown when a gated app's daily limit is reached |
| Evening shutdown | Planned | Ask whether today's frog was done, set tomorrow's frog |
| Settings | Planned | Pinned apps, gated apps and limits, friction rules, pet quiet mode |

## The frog

- Exactly **one** most important task. A second one cannot be added until the first is done or replaced.
- It should be the hardest or most avoided task (from *Eat That Frog*).
- Set it the evening before when possible (Ivy Lee method), so the morning needs no decisions.
- Actions: **Start 2 min** (starts tiny, from *Atomic Habits*) and an optional **25:00 focus** timer.
- If the frog is undone, the app only **reminds** the user. It does not lock anything because of it.

## Gated apps

The user picks which apps are gated. Planned friction, in order:

1. App names appear in **red** with a text tag.
2. Opening one shows the gate: the pet asks "is this the frog?", then
3. a **reason** (minimum 40 characters, no paste),
4. a **typed phrase** (random, one mistake resets it),
5. a **wait** (60 seconds, doubling with each open that day).
6. A session lasts 10 minutes, then returns to the launcher.
7. A **daily limit** locks the app until the next morning, with no override button.

Two rules keep this fair:

- **Cancel is always the easiest, brightest button** on the gate screen.
- **Weakening a rule takes effect after 24 hours**, so it cannot be switched off in a weak moment. Making it stricter is instant.

All numbers are defaults the user can change.

## The pet

A plain text face, currently `[^_^]`. Species and name are undecided.

**Voice:** friendly, short, specific, encouraging. Never guilt, never scary, no "failure" state, nothing dies or gets sad forever.

**Behavior-driven reminders** (planned):

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
