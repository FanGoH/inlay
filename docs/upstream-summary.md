# Upstream-friendly implementation summary

This fork adds **Dual-stick Absolute Selection** without replacing Classic navigation.

## Why a mode, not a rewrite

Inlay's highlight is a discrete grid cursor driven by `StickDirection` and `Repeater`. SteamOS-style typing is the opposite: two independent absolute pointers. Those models cannot share one navigator, so Dual-stick is a temporary route around Classic, gated by a setting that defaults to **Disabled**.

## What we did not change

- `StickDirection`, `startNav`, `moveFocus`, and Classic button bindings when Dual-stick is off
- Layout tables in `Layouts.kt`
- The IME contract (no extra permissions, no full-screen extract view)
- GPL-3.0

## Isolated additions

| File | Responsibility |
|---|---|
| `DualStickTypes.kt` | Mode, curve, `AnalogSelection`, `MappedKey` |
| `AnalogStickMath.kt` | Deadzone, curve, right-stick axis pick |
| `AbsoluteKeyMapper.kt` | Live geometry → nearest key |
| `DualStickActivation.kt` | Disabled / Hold / Toggle |
| `DualStickController.kt` | Left / right selection state |
| `*Test.kt` | Mapping, deadzone, activation, defaults |

`GridKeyboardService` only branches when `dualActivation.active` or the configured activation button is pressed. `KeyGridView` still draws one Classic highlight unless Dual-stick is showing.

## Button conflicts we resolved

| Proposed (handoff) | Inlay Classic | This fork |
|---|---|---|
| LT / RT confirm | LT = symbols, RT = shift | **LB / RB confirm**; LT / RT unchanged |
| Activation button | Almost every face / shoulder key is taken | Default **RS (R3)**, unused in 1.0.0 |
| Left stick | Classic highlight | Absolute aim only while the mode is on |

`RT` + `LB` / `RB` still jumps words while Dual-stick is on.

## Suggested upstream merge shape

1. Land the five new Kotlin files and their unit tests as-is.
2. Keep the `KeyboardSettings` fields defaulting to Disabled.
3. Treat the `GridKeyboardService` / `KeyGridView` / Settings diffs as the only integration surface.

No application-id change: the debug APK installs over official Inlay (`io.github.dakingeman.inlay`).
