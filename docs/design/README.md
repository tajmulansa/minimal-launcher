# Design prototype

`ceramic-prototype.html` is the chosen visual direction, called **Ceramic**. It is one self-contained HTML file (no build step, no network), so you can open it in any phone or desktop browser.

It has 19 tap-through screens: Home, All apps, Widgets, Add widget, Brain dump, the three gate steps, the app session timer, Time's up, Daily limit reached, Focus session, Evening shutdown, Weekly review, Welcome, Settings, the two app pickers, and a Pip message. Use the **Light / Dark / Auto** switch in the top bar to see both themes.

## How to use it

- Treat it as the visual spec for the Android app. Match spacing, radii, colors, and copy.
- Colors, radii, and shadows are CSS variables at the top of the file. Use the same names in `ui/theme/Theme.kt`.
- The page only imitates behavior (breathing guide, phrase check, timers) so the screens can be judged. The real rules live in [../DESIGN.md](../DESIGN.md).
- The Inter font is embedded in the file as a Latin subset. Inter is under the SIL Open Font License, the same license the app will bundle.

## Changing it

Edit the HTML directly and open a pull request. Please keep:

- one typeface (Inter),
- red only for gated apps, always with a text tag,
- both themes working (every color is a variable, no hard-coded colors in components),
- touch targets of at least 44px.
