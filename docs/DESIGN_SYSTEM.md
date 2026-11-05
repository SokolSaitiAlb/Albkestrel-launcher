# Design System

## 1. Living Flow principles

Albkestrel's visual language is called **Living Flow**. It has three practical rules:

1. **Luminous surfaces** — translucent cards with restrained borders and subtle depth, not flat
   blocks or heavy shadows.
2. **Fluid motion** — spring-driven transitions that preserve spatial context, with a reduced-motion
   escape hatch.
3. **Flexible feedback** — optional haptics and press scaling that confirm interaction without
   becoming noisy.

The design system is implemented in `core/designsystem`.

## 2. Color tokens

### Core accents

| Token | Value | Use |
| --- | --- | --- |
| `LivingCyan` | `#00E5FF` | Primary accent, focus, selected state |
| `LivingCyanLight` | `#0284C7` | Light-theme primary |
| `LivingViolet` | `#8B5CF6` | Secondary accent |
| `LivingVioletLight` | `#7C3AED` | Light-theme secondary |
| `LivingEmerald` | `#10B981` | Emerald theme, positive/wellbeing |
| `LivingAmber` | `#F59E0B` | Tertiary accent, warnings/mock labels |
| `LivingRose` | `#F43F5E` | Error/alert accent |
| `LivingIndigo` | `#6366F1` | Supporting accent |

### Dark theme

| Token | Value |
| --- | --- |
| Background | `#080D1A` |
| Surface | `#0D1424` |
| Surface variant | `#1E293B` |
| On surface | `#F1F5F9` |
| On surface variant | `#94A3B8` |
| Primary container | `#004E5A` |
| Secondary container | `#4C1D95` |

### Light theme

| Token | Value |
| --- | --- |
| Background | `#F8FAFC` |
| Surface | `#FFFFFF` |
| Surface variant | `#E2E8F0` |
| On surface | `#0F172A` |
| On surface variant | `#475569` |
| Primary | `#0284C7` |
| Secondary | `#7C3AED` |

### Emerald theme

A dark green scheme:

| Token | Value |
| --- | --- |
| Primary | `#10B981` |
| Background | `#051711` |
| Surface | `#0A221A` |
| Surface variant | `#13362A` |
| On surface | `#E1F5EC` |

### Theme selection

`AlbkestrelTheme` resolves:

- `SYSTEM` → light/dark from the system.
- `DARK` → dark scheme.
- `LIGHT` → light scheme.
- `EMERALD` → dark emerald scheme.
- Dynamic color on API 31+ overrides the palette when `dynamicColor` is enabled.

Dynamic color is wired through the repository and theme but has no Settings control yet.

## 3. Typography

The brand typeface is **Plus Jakarta Sans**, bundled as `res/font/plus_jakarta_sans.ttf` and mapped
to normal, medium, semibold, and bold weights.

| Style | Size | Weight |
| --- | --- | --- |
| `displayLarge` | 54sp | Bold |
| `displayMedium` | 42sp | Bold |
| `displaySmall` | 32sp | Semibold |
| `headlineMedium` | 24sp | Semibold |
| `headlineSmall` | 20sp | Semibold |
| `titleLarge` | 18sp | Semibold |
| `titleMedium` | 15sp | Medium |
| `bodyLarge` | 15sp | Normal |
| `bodyMedium` | 13sp | Normal |
| `labelLarge` | 13sp | Semibold |
| `labelMedium` | 11sp | Medium |
| `labelSmall` | 10sp | Medium |

Guidelines:

- Use Material 3 typography roles; do not hard-code new font sizes unless a component needs a
  measured exception.
- Keep secondary text readable on translucent surfaces.
- Test at larger system font scales.

## 4. Surfaces

`LuminousSurface` is the shared card primitive:

```kotlin
LuminousSurface(
    shape = RoundedCornerShape(24.dp),
    backgroundColor = Color.Black.copy(alpha = 0.28f),
    borderColor = Color.White.copy(alpha = 0.20f)
) {
    // content
}
```

It applies:

- Clipping to the requested shape.
- A background color.
- A linear-gradient border built from the supplied border color.
- Optional shadow elevation.

Rules:

- Prefer translucent black/white overlays on the wallpaper rather than opaque cards.
- Keep borders subtle; use accent borders only for selected or branded surfaces.
- Do not nest more than two translucent surfaces without checking contrast.
- Avoid large blur/glow effects that are expensive on low-end devices.

## 5. Icon system

### Sizes

| Setting | Size |
| --- | --- |
| Compact | 48dp |
| Balanced | 56dp |
| Expansive | 64dp |

### App icon anatomy

`LivingAppIcon` renders:

1. A rounded "squircle" background with a linear gradient derived from the package name.
2. A specular top highlight.
3. The real `PackageManager` icon when available.
4. A vector symbol fallback when no bitmap exists.
5. An optional notification badge.
6. An optional label.

Press state scales to `0.90f`. Labels use one line with ellipsis.

### Folder icon

`FolderIcon` renders a translucent rounded square with a 2×2 preview of the first four folder apps,
plus the folder title. It uses the same press-scale behavior as app icons.

### Icon limitations

- No icon-pack parsing.
- No per-app icon overrides.
- No themed-icon adaptation.
- The icon cache is bounded by a heap-derived `LruCache` in the Phase 1.2.1 working tree.
- Icon quality at every size is not covered by visual regression tests.

See [ICON_ENGINE.md](ICON_ENGINE.md).

## 6. Motion

`AnimationSpeed` controls spring behavior:

| Mode | Behavior |
| --- | --- |
| Living Flow Spring | Low-bouncy/medium-low entrance, bouncy icon press |
| Snappy | No-bounce, high-stiffness springs |
| Reduced Motion | No-bounce, high-stiffness springs; overlays still fade/scale |

Overlay transitions:

- App drawer: slide from bottom + fade.
- Search: slide from top + fade.
- Settings: slide from bottom + fade.
- Folder and context menu: scale from `0.85f`/`0.92f` + fade.
- Page indicator: animated width spring.

Guidelines:

- Motion should communicate origin and destination.
- Do not animate every property; pick the one that explains the state change.
- Reduced Motion must not rely on a long spring. Prefer short fades or instant state changes.
- Test on a mid-range device; spring physics can still jank if recomposition is excessive.

## 7. Haptics

- Haptics are enabled by default and can be disabled in Settings.
- Pattern 1: short 18ms tick for navigation/selection.
- Pattern 2: 32ms tick for stronger confirmation.
- Other: 12ms tick.
- Android 8.0+ uses `VibrationEffect`; older devices use the deprecated `vibrate(20)` call.
- Android 12+ obtains the default vibrator through `VibratorManager`.
- Haptic failures are ignored.

Haptics must never be the only feedback for an action.

## 8. Layout and density

- Workspace grid: 4 or 5 columns.
- Home page 0 displays at most 8 items in 4-column mode or 10 in 5-column mode.
- App drawer uses the same column setting.
- Folder grid is fixed at 3 columns.
- Dock uses a translucent shelf with optional labels.
- Home uses safe drawing insets via `statusBarsPadding` and `navigationBarsPadding`.

## 9. Component rules

### Search field

- Rounded, translucent container.
- Accent focus border.
- Leading search icon.
- Clear action when non-empty.
- Single line.

### Settings pills

- Selected: accent tint background, accent border, bold accent text.
- Unselected: white low-alpha background, subtle border, light text.
- Keep the entire pill tappable.

### Context menu

- Width: 250dp.
- Maximum height: screen height minus 32dp.
- 20dp corner radius.
- Surface tint with a primary-tinted border.
- 48dp menu-item rhythm.
- Scrollable when actions exceed the height bound.
- Anchored near the pressed icon and clamped to screen margins.

### Empty states

- Use a muted icon, a short title, and a one-line explanation.
- Do not leave a blank screen while loading.
- Distinguish "no apps installed", "no matches for query", and "no apps in category".

## 10. Accessibility

Required:

- Meaningful content descriptions for controls.
- Semantic click and long-click actions for custom gesture surfaces.
- Minimum touch target of 48dp where practical.
- Contrast checks on translucent surfaces.
- Reduced-motion behavior.
- Haptics opt-out.
- Font-scale testing.
- RTL support.

`LivingAppIcon` uses raw `pointerInput` for the touch path but declares both `onClick` and
`onLongClick` through semantics. `LivingAppIconAccessibilityTest` guards that contract. The owner
reported TalkBack click and long-press passing on a Google Pixel 8a; broader traversal, switch
access, and keyboard navigation remain unverified.

## 11. Do and do not

Do:

- Reuse tokens and primitives.
- Prefer a calm composition over decorative noise.
- Keep mock labels visible.
- Test the design in dark, light, and emerald themes.
- Test with a real wallpaper and real app icons.

Do not:

- Add a new color literal in a feature file when a token exists.
- Use opaque cards that hide the wallpaper entirely.
- Animate content that the user is trying to read.
- Use color alone to communicate state.
- Present mock data as live.
- Add a new component library without an ADR.
