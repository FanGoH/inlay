# Dual-stick Absolute Selection

Optional aiming mode for the FanGoH Inlay fork. Classic D-pad and left-stick navigation stay the default.

## What it does

While the mode is active:

- The **left stick** aims at keys in the left half of the *rendered* keyboard.
- The **right stick** aims at keys in the right half.
- Each stick has its own highlight (the usual accent on the left, a shifted accent on the right).
- Holding a stick still does not walk the highlight. Moving a stick does not type.
- **LB** types the left selection, **RB** types the right selection, **A** types the last key you aimed at.

The split uses live key rectangles, so staggered Classic rows and size / orientation changes stay correct. The two halves meet at the centre with no overlap; a key belongs to the stick that owns its centre. Analog aiming only includes letter and punctuation keys. Space, Shift, symbols/layer, Delete, Enter, and the tool strip stay on their existing buttons.

## Turn it on

1. Open **Inlay → Controls → Dual-stick selection**.
2. Set **Activation** to **Hold** or **Toggle** (default is **Disabled**).
3. Leave **Activation button** on **RS** unless that click is captured on your device.
4. Enable Inlay as the current keyboard, open any text field, then:

**Hold:** press and hold RS. Both sticks aim. Let go and Classic navigation returns immediately.

**Toggle:** press RS once to enter, again to leave. Repeats from a held button do not bounce the mode.

Letting the keyboard hide, or starting a new field, always leaves the mode so Hold cannot stick.

## Bindings while the mode is active

| Input | Action |
|---|---|
| Left stick | Aim at the left region |
| Right stick | Aim at the right region |
| LB | Type the left selection |
| RB | Type the right selection |
| A | Type the last aimed key |
| X | Delete |
| Y | Space |
| Start | Enter |
| LT | Letters / symbols |
| RT | Shift · hold for combos |
| RT + LB / RB | Jump word (same as Classic) |
| D-pad left / right | Move the text cursor |
| Select | Select mode |
| LS | Keyboard size (unless LS is the activation button) |

The handoff suggested LT / RT as confirm buttons. Those already mean symbols and shift in Inlay, so this fork keeps them and uses the shoulders for confirm instead.

## Thor Max test checklist

Install the debug APK over the same application id as upstream Inlay (`io.github.dakingeman.inlay`).

A debug APK from this branch is produced by `tools/build-debug.sh` at `app/build/outputs/apk/debug/app-debug.apk` (about 13 MB). On an ARM64 Linux host, AGP 9 needs a native `aapt2`; this Pi build used the 37.0.0 binary from [Commit451/android-arm-build-tools](https://github.com/Commit451/android-arm-build-tools) via `android.aapt2FromMavenOverride`.

1. **Classic regression.** Activation = Disabled. D-pad, left stick, A / X / Y / LT / RT / LB / RB / Select / Start behave as 1.0.0.
2. **Hold.** Activation = Hold, button = RS. Hold RS: two highlights, no typing from the sticks. Release RS: Classic returns, no extra character.
3. **Toggle.** One RS press in, one press out. Hold RS: the mode must not flicker.
4. **Dismiss.** Open a field, hold RS, press Home or Back. Re-open the keyboard: Dual-stick must be off.
5. **Both sticks.** Left stick picks keys on the left, right stick on the right, independently. Space, Shift, layer, Delete, Enter, and the tool strip are not analog targets.
6. **Confirm.** LB / RB / A type the aimed keys. Rapid left/right alternation should not drop or double characters.
7. **Editing.** X, Y, Start, LT, RT, clipboard and select mode still work. RT + LB / RB still jumps words.
8. **Layouts.** Try Grid and Classic, Normal / Large / Largest, letters and symbols, portrait and landscape.

### Controller modes on AYN devices

Try both **Android** and **Xbox** controller modes if the device offers them.

- Xbox-style pads usually report the right stick as `AXIS_Z` / `AXIS_RZ`.
- Some Android mappings use `AXIS_RX` / `AXIS_RY`.
- This build reads both pairs and uses the one that is actually moving.

If the right stick does nothing, switch controller mode before changing code. If RS (R3) never reaches the IME (no highlight change when held), pick **Select** or **LS** as the activation button. Some firmwares swallow stick-clicks.

## Odin 2 Portal Pro

Same APK and settings. Upstream Inlay was tuned on an Odin 2 Portal, so Classic behaviour should match. Compare Dual-stick precision against the Thor: if one device feels tighter, raise the deadzone or try the **Soft** response curve.

## Settings

| Setting | Default | Notes |
|---|---|---|
| Activation | Disabled | Hold or Toggle to use the mode |
| Activation button | RS | Also LS, Select, LT, RT |
| Stick deadzone | Default (3) | 0 is tight, 10 is wide |
| Center reach | Default (6) | Raise this if T/G/Y/H feel too far inward |
| Stick response | Linear | Soft = more precise near rest; Sharp = faster to the middle letters |
| Remember last key | Off | Off: centre = no selection. On: the last key stays confirmable |

## Known limits

- This is a normal Android IME. No root, Shizuku, accessibility service, or extra permissions.
- The IME already received the left stick and triggers through `MotionEvent`. The right stick is expected on the same joystick stream; it is not used by Classic Inlay, so this is the first time that path is exercised.
- If a focused game or the AYN overlay eats RS / Select, the IME never sees the activation button. Choose another button.
- Holding the activation button must not block analog or trigger events. If a firmware does that, use Toggle instead of Hold.
- Toggle is also cleared when the keyboard hides, so a dismissed field cannot come back still in Dual-stick.
- Dual-stick confirm remaps LB / RB. Use the D-pad or `RT` + shoulders for the caret while the mode is on.
- Debug aim dots (small accent circles) show the mapped stick position. They are a testing aid, not a second cursor.

## Architecture (upstream-friendly)

The Classic navigator (`StickDirection`, `startNav`, `moveFocus`) is untouched when Activation is Disabled. New types live in their own files:

| Type | Role |
|---|---|
| `DualStickActivation` | Disabled / Hold / Toggle, repeats, cancel |
| `AnalogStickMath` | Deadzone, curve, axis pick, 0…1 mapping |
| `AbsoluteKeyMapper` | Region split and nearest live key |
| `DualStickController` | Independent left / right `AnalogSelection` |
| `KeyGridView` | Dual highlights + key snapshots |
| `GridKeyboardService` | Routes input only while the mode is active |

`KeyboardSettings` stores the five Dual-stick fields in the existing `gridime` preferences.

Licence remains GNU GPL-3.0. Please keep this notice if you rebase onto upstream.
