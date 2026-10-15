# DESIGN.md: Gym Baswara - Apple Sporty Design System

> Source of Truth for Visual Identity, HIG Rules, and Anti-Slop Guidelines.

## 1. Identity & Personality
- **Product**: Gym Baswara (Personal Gym & Workout Tracker for Android).
- **Aesthetic**: Apple Fitness+ meets Athletic Performance (Human Interface Guidelines).
- **Keywords**: Sporty, Precision, Focused, High-Legibility, Tactile.
- **Audience**: Active gym-goers tracking weights, reps, volume, and routines at a glance during workouts.

## 2. Design Read & Dials (antislop Part 3)
- **Design Read**: Gym workout tracker for active gym goers, in an Apple Fitness+ / HIG Sporty style, dial ENERGY 2 / RHYTHM 2 / MOTION 1.
- **ENERGY: 2** (Balanced: Confident athletic contrast, Volt Green accent reserved for focal action and metrics).
- **RHYTHM: 2** (Consistent hierarchy: Dominant Workout Hero -> Metric Summary with Segmented Tabs -> Direct Routine/PR Action Cards).
- **MOTION: 1** (Subtle & purposeful: Responsive touch indications, smooth state transitions, strictly zero perpetual bouncing loops).

## 3. Color Tokens (Apple Fitness Volt)

### Core Rule (R-29)
Maximum 2-3 core neutral layers + 1 primary accent.

### Dark Theme (Default)
| Token | Hex | Role | Contrast Ratio |
|---|---|---|---|
| `background` | `#000000` | Pure OLED Pitch Black canvas | Base |
| `surface` | `#1C1C1E` | Apple System Gray 6 (Elevated Cards) | 1.3:1 vs background |
| `surfaceVariant` | `#2C2C2E` | Apple System Gray 5 (Segmented container, Insets) | 2.0:1 vs background |
| `primary` (Accent) | `#D0FD3E` | Apple Fitness Volt Green (Hero CTA, Key Metrics) | 14.5:1 on `#000000` (AAA) |
| `onPrimary` | `#000000` | Deep Black text on Volt accent | 14.5:1 (AAA) |
| `onBackground` | `#FFFFFF` | Crisp White text (Headlines, Titles) | 21:1 (AAA) |
| `onSurface` | `#FAFAFA` | Off-white text on cards | 13.8:1 on `#1C1C1E` (AAA) |
| `onSurfaceVariant`| `#8E8E93` | Apple System Gray secondary label text | 4.8:1 on `#1C1C1E` (AA) |
| `tertiary` (Active) | `#FF375F` | Apple Activity Crimson (Active Workout pulse/banner) | 4.6:1 on `#000000` (AA) |
| `outline` | `#38383A` | Subtle divider border | 3.1:1 non-text boundary |

### Light Theme
| Token | Hex | Role | Contrast Ratio |
|---|---|---|---|
| `background` | `#F2F2F7` | Apple System Grouped Background Light | Base |
| `surface` | `#FFFFFF` | Pure White Cards | 1.15:1 vs background |
| `surfaceVariant` | `#E5E5EA` | Apple System Gray 5 Light | 1.3:1 vs surface |
| `primary` | `#000000` | Jet Black bold accents and primary CTA | 21:1 on `#FFFFFF` (AAA) |
| `onPrimary` | `#FFFFFF` | White text on black button | 21:1 (AAA) |
| `onBackground` | `#000000` | Deep Black headlines | 19.5:1 on `#F2F2F7` (AAA) |
| `onSurface` | `#1C1C1E` | Deep Charcoal card titles | 14.8:1 on `#FFFFFF` (AAA) |
| `onSurfaceVariant`| `#636366` | Apple Muted Gray secondary text | 4.7:1 on `#FFFFFF` (AA) |
| `outline` | `#D1D1D6` | Subtle divider border | 3.2:1 non-text boundary |

## 4. Typography Scale (HIG Athletic Standard)
- `headlineLarge`: 30sp, FontWeight.ExtraBold, letterSpacing -0.5sp (App bar / Screen Greeting)
- `headlineMedium`: 24sp, FontWeight.Bold, letterSpacing -0.3sp (Section titles)
- `titleLarge`: 20sp, FontWeight.Bold, letterSpacing -0.2sp (Card titles)
- `titleMedium`: 16sp, FontWeight.SemiBold, letterSpacing 0sp (Subheadings)
- `bodyLarge`: 16sp, FontWeight.Normal, lineHeight 22sp (Body content)
- `bodyMedium`: 14sp, FontWeight.Normal, lineHeight 20sp (Description copy)
- `labelLarge`: 14sp, FontWeight.SemiBold, letterSpacing 0.2sp (Button labels)
- `labelMedium`: 12sp, FontWeight.Medium, letterSpacing 0.4sp (Metric units, pill tabs)
- Metric Numbers: 28sp - 32sp, FontWeight.ExtraBold (Clean tabular numerals for session, volume, duration)

## 5. Shape & Corner Radii (Apple Squircle Tokens)
- `Card Radius`: 22dp (RoundedCornerShape - generous Apple squircle curve)
- `Segmented Container`: 100dp (Smooth pill shape)
- `Active Tab Indicator`: 100dp
- `Action Icon Container`: 14dp
- `Minimum Tap Target`: 48dp on all interactive elements (buttons, tabs, list cards).

## 6. Copywriting & Content (antislop-copywriting)
- **FORBIDDEN**: em dash character (`—`) in any Indonesian or English copy (R-02).
- **Tone**: Energetic, direct, athletic, and human.
- **No generic AI buzzwords**: No "revolusioner", "seamless", "solusi terbaik", "next generation".
- **Real States**: Real labels with real calculations (Volume Formula popup, real dynamic session numbers).

## 7. Performance & Jetpack Compose Craftsmanship
- `remember` on all lambda handlers and interaction sources.
- No heavy date/time formatting inside recomposition loops.
- `key` provided in LazyColumn items for list optimization.
- Semantic accessibility labels on all icon buttons (`contentDescription`).
