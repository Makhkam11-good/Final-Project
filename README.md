# Gladiator Arena

![Java](https://img.shields.io/badge/Java-17-orange)
![LibGDX](https://img.shields.io/badge/LibGDX-1.14.0-red)
![Gradle](https://img.shields.io/badge/Gradle-9.3.1-blue)
![Platform](https://img.shields.io/badge/platform-desktop%20LWJGL3-2f6f9f)

**Gladiator Arena** is a desktop 2D arena game built with Java and LibGDX. The player controls a gladiator knight, clears waves of enemies, collects coins and pickups, chooses upgrades between fights, and pushes through three arenas toward the final boss.

The project is split into a gameplay module, `core`, and a desktop launcher module, `lwjgl3`. The current implementation includes the main menu, pause flow, wave combat, bosses, upgrades, HUD, music, sound effects, animations, and simple enemy AI.

## Table of Contents

- [Screenshots](#screenshots)
- [Features](#features)
- [Gameplay](#gameplay)
- [Controls](#controls)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Development Run](#development-run)
- [Build](#build)
- [Testing](#testing)
- [Architecture](#architecture)
- [Core Gameplay Systems](#core-gameplay-systems)
- [Assets](#assets)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)
- [License](#license)

## Screenshots

Screenshots can be placed in `docs/screenshots/` and linked here. The folder already exists in the repository.

Suggested filenames:

| File | Suggested screen |
| --- | --- |
| `docs/screenshots/menu-screen.png` | Difficulty selection in `MenuScreen`. |
| `docs/screenshots/gameplay-wave.png` | Main arena HUD and wave combat in `GameScreen`. |
| `docs/screenshots/upgrade-screen.png` | Upgrade choice screen after clearing a wave. |
| `docs/screenshots/boss-fight.png` | Boss fight with boss HP and dash telegraph. |
| `docs/screenshots/dark-castle.png` | Level 3 `Dark Castle` arena. |
| `docs/screenshots/victory-screen.png` | Final `VictoryScreen`. |
| `docs/screenshots/pause-screen.png` | Pause menu with resume/music/restart/exit actions. |

## Features

- ⚔️ Arena combat: fight enemy waves in a fixed `800x480` arena.
- 🌊 Wave loop: each room has 3 waves, with a mini boss on wave 2 and a main boss on wave 3.
- 🏰 Progression: 3 implemented rooms: `Dungeon`, `Fire Arena`, and `Dark Castle`.
- 🧠 Enemy AI: enemies chase the player, some wander until the player enters sight range, and the boss uses a state machine.
- 🧩 Upgrade system: after clearing regular waves, the player chooses one of three random upgrades.
- 🪙 Coins and revive: enemies drop coins; 10 coins unlock revive after death.
- ❤️ Pickups: defeated enemies can drop `HEART` and `SHIELD` pickups.
- 🎵 Audio: background music plus attack, hit, enemy death, boss phase, victory, and game over sounds.
- 🖼️ Pixel-art assets: game atlas, tiled floor, UI font/skin, and character animations.
- 🧱 Desktop build: LWJGL3 launcher, runnable JAR, and platform-specific JAR tasks.

The project does not currently contain disk saves, a dedicated settings screen, multiplayer, or an external physics engine.

## Gameplay

The player is a gladiator knight placed in the arena. The goal is to survive, clear all waves, and defeat the final boss on the third level.

### Gameplay Loop

1. The player chooses a difficulty in `MenuScreen`.
2. `GameScreen` starts the first wave of the current room.
3. Enemies spawn from arena edges and deal contact damage.
4. The player moves, attacks the nearest enemy in range, and uses dash to reposition or dodge.
5. Enemy kills grant score, combo progress, coins, and possible pickups.
6. After a wave is cleared, the player gets a short loot collection window.
7. After wave 1 and wave 2, `UpgradeScreen` opens and the player chooses one upgrade.
8. Wave 3 is a boss fight. After defeating the boss, a portal opens to the next room.
9. After clearing the third room, the game transitions to `VictoryScreen`.
10. If player HP reaches 0, a revive prompt appears when revive is affordable; otherwise the game moves to `GameOverScreen`.

### Levels and Waves

| Room | Name | Waves | Notes |
| --- | --- | --- | --- |
| 1 | `Dungeon` | 3 | Base arena with early slime/goblin groups, mini boss, and boss. |
| 2 | `Fire Arena` | 3 | More enemies, extra fast/tank enemies, and stronger visual styling. |
| 3 | `Dark Castle` | 3 | Final room with extra ranged/tank enemies and the final boss. |

`GameScreen` defines `FINAL_LEVEL = 3` and `WAVES_PER_ROOM = 3`, so a full run contains 9 combat waves.

### Enemies

| Entity | Class | Behavior |
| --- | --- | --- |
| Slime | `Slime` | Uses `SlimeAi`: wanders, then chases the player inside sight range. |
| Goblin | `Goblin` | Uses `GoblinAi`: aggressively moves toward the player. |
| Fast enemy | `FastEnemy` | Faster slime variant with lower HP. |
| Tank enemy | `TankEnemy` | Slower, tougher goblin variant. |
| Ranged enemy | `RangedEnemy` | Keeps `MOVE_RANGE` and approaches only while the player is far away. |
| Mini boss | `MiniBoss` | Strong enemy used on the second wave of a room. |
| Boss | `Boss` | Large enemy with phases, telegraphing, and dash attacks. |

### Victory and Defeat

- Victory: defeat the boss on level 3 (`Dark Castle`) to open `VictoryScreen`.
- Level transition: defeat the boss on level 1 or 2 and enter the `Portal`.
- Defeat: player HP reaches 0 and the player does not revive.
- Revive: if the player has at least `10` coins, pressing `R` or clicking the revive button restores the player at the last safe position with partial HP and brief invulnerability.

## Controls

| Action | Keys / mouse |
| --- | --- |
| Movement | `W`, `A`, `S`, `D` |
| Attack | `J` or left mouse click |
| Dash | `Space` |
| Pause | `Esc` |
| Toggle music during gameplay | `M` |
| Select difficulty | `1`, `2`, `3`, or click |
| Choose upgrade | `1`, `2`, `3`, or click |
| Revive after death | `R` |
| Give up after death | `Esc` or click |
| Pause: resume | `R` or `Esc` |
| Pause: music | `M` |
| Pause: restart | `T` |
| Pause: exit to menu | `Q` |
| Victory/Game Over: restart | `R` |
| Victory/Game Over: menu | `M` or `Esc` |

## Technology Stack

| Area | Technology |
| --- | --- |
| Language | Java 17 |
| Game framework | LibGDX `1.14.0` |
| Desktop backend | LWJGL3 |
| Build system | Gradle wrapper `9.3.1` |
| Audio | LibGDX `Music` and `Sound` |
| Rendering | `SpriteBatch`, `ShapeRenderer`, `TextureAtlas`, `BitmapFont` |
| Viewport | `FitViewport` with an `800x480` world |
| Packaging | Gradle `application`, `jar`, Construo |

There is no `package.json`, `requirements.txt`, or Maven `pom.xml`; dependencies are managed through Gradle files.

## Project Structure

```text
.
├── assets/                         # Runtime assets for LibGDX
│   ├── atlas/game.atlas             # Texture atlas with gameplay regions
│   ├── atlas/game.png
│   ├── music/gameplay.wav
│   ├── sounds/*.wav
│   ├── sprites/
│   └── ui/
├── core/                            # Main gameplay logic
│   └── src/main/java/com/gladiator/arena/
│       ├── ai/                      # Enemy AI and boss state machine
│       ├── decorator/               # Player stat upgrades
│       ├── entities/                # Player, Enemy, Boss, Coin, PickupItem, Portal
│       ├── events/                  # EventBus and game events
│       ├── factories/               # Enemy factories
│       ├── managers/                # Assets, audio, state, levels, score stats
│       ├── screens/                 # Menu/Game/Pause/Upgrade/Victory/GameOver
│       ├── strategy/                # DifficultyStrategy
│       └── GladiatorGame.java       # LibGDX Game entry point
├── docs/
│   └── screenshots/                 # Place gameplay screenshots here
├── lwjgl3/                          # Desktop launcher and packaging
│   ├── build.gradle
│   └── src/main/java/com/gladiator/arena/lwjgl3/
│       ├── Lwjgl3Launcher.java
│       └── StartupHelper.java
├── build.gradle                     # Shared Gradle configuration
├── gradle.properties                # Versions and Gradle settings
├── settings.gradle                  # core/lwjgl3 modules
└── gradlew / gradlew.bat            # Gradle wrapper
```

## Installation

### Requirements

- JDK 17.
- Internet access on the first Gradle wrapper run, so Gradle `9.3.1` and dependencies can be downloaded.
- Windows, Linux, or macOS with LWJGL3 support.

### Clone

```bash
git clone <repo-url>
cd Final-Project
```

### Check Java

```bash
java -version
```

Java 17 or a compatible JDK is expected. The project also applies `org.gradle.toolchains.foojay-resolver-convention`, so Gradle may automatically resolve a JDK toolchain.

## Development Run

Linux/macOS:

```bash
./gradlew lwjgl3:run
```

Windows:

```bat
gradlew.bat lwjgl3:run
```

If Gradle fails on Windows because of low memory or a small page file, you can reduce the heap for the current run:

```powershell
$env:GRADLE_OPTS='-Xms64M -Xmx384M -Dfile.encoding=UTF-8'
.\gradlew.bat lwjgl3:run --no-daemon
```

## Build

Compile the desktop classes:

```bash
./gradlew lwjgl3:classes
```

Windows:

```bat
gradlew.bat lwjgl3:classes
```

Build a runnable JAR:

```bash
./gradlew lwjgl3:jar
```

The built JAR is expected at:

```text
lwjgl3/build/libs/GladiatorArena-1.0.0.jar
```

Platform-specific JAR tasks from `lwjgl3/build.gradle`:

```bash
./gradlew lwjgl3:jarWin
./gradlew lwjgl3:jarLinux
./gradlew lwjgl3:jarMac
```

The project also provides a gdx-setup-compatible task:

```bash
./gradlew lwjgl3:dist
```

## Testing

No test sources (`src/test/...`) or dedicated test dependencies were found in the repository. Java/Gradle test tasks may exist as standard plugin tasks, but the project currently has no actual automated tests.

For a basic verification, compile the desktop target:

```bash
./gradlew lwjgl3:classes
```

Then run a manual smoke test:

```bash
./gradlew lwjgl3:run
```

## Architecture

### Entry Point

- `GladiatorGame` creates `SpriteBatch`, `BitmapFont`, `TextureAtlas`, `OrthographicCamera`, and `FitViewport`, loads game assets, and opens `MenuScreen`.
- `Lwjgl3Launcher` creates the desktop application with the `Gladiator Arena` title, VSync, and `800x480` windowed mode.

### Screens

| Screen | Responsibility |
| --- | --- |
| `MenuScreen` | Difficulty selection and new game start. |
| `GameScreen` | Main gameplay loop, wave spawning, combat, loot, portal, HUD, and transitions. |
| `UpgradeScreen` | Lets the player choose one of three random upgrades after wave 1/2. |
| `PauseScreen` | Resume, music toggle, restart, and exit to menu. |
| `GameOverScreen` | Defeat screen, run statistics, retry/menu actions. |
| `VictoryScreen` | Victory screen, final score, play again/menu actions. |

### Managers

| Class | Responsibility |
| --- | --- |
| `AssetManager` | Loads `atlas/game.atlas`, registers animations, and draws the tiled floor. |
| `SoundManager` | Loads music and sounds, controls `Music: ON/OFF`. |
| `GameManager` | Stores selected difficulty and runtime run statistics. |
| `GameStateManager` | Maintains a state stack: `MENU`, `GAME`, `PAUSE`, `UPGRADE`, `GAME_OVER`, `VICTORY`. |
| `LevelManager` | Tracks current wave, alive enemies, wave progress, and posts `WAVE_CLEARED`. |
| `DamageNumberManager` | Displays floating damage/heal/combo text through events. |

### Patterns

- Strategy: `DifficultyStrategy`, `EasyDifficulty`, `MediumDifficulty`, `HardDifficulty`.
- Decorator: `PlayerStats` and upgrade decorators for player stats.
- Factory: `EnemyFactory` and concrete factories such as `SlimeFactory`, `GoblinFactory`, `BossFactory`, and others.
- State: `PlayerState` for idle/run/attack/dead and `BossState` for boss behavior.
- Event bus: `EventBus` connects gameplay systems without hard coupling.

## Core Gameplay Systems

### Player Stats and Upgrades

Base values are defined in `BasePlayerStats`:

| Stat | Value |
| --- | --- |
| Max HP | `100` |
| Damage | `10` |
| Speed | `150` |
| Attack cooldown | `1.0s` |
| Incoming damage multiplier | `1.0` |

Upgrades:

| Upgrade | Class | Effect |
| --- | --- | --- |
| Fire Weapon | `FireWeaponDecorator` | `+15 damage` |
| Poison Edge | `PoisonDecorator` | `+10 damage` |
| Shield | `ShieldDecorator` | `+30 max HP`; current HP increases along with max HP |
| Armor | `ArmorDecorator` | incoming damage is multiplied by `0.8` |
| Speed Boots | `SpeedBootsDecorator` | speed is multiplied by `1.25` |
| Attack Speed | `AttackSpeedDecorator` | attack cooldown is multiplied by `0.8` |

### Difficulty

| Difficulty | Enemy speed | Enemy damage | Boss HP | Spawn interval |
| --- | --- | --- | --- | --- |
| `EasyDifficulty` | `0.8x` | `0.7x` | `300` | `2.0s` |
| `MediumDifficulty` | `1.0x` | `0.9x` | `450` | `2.0s` |
| `HardDifficulty` | `1.1x` | `1.1x` | `600` | `2.0s` |

### Boss Behavior

`Boss` uses explicit states:

- `IdleBossState` - short pause.
- `ChaseBossState` - follows the player.
- `TelegraphBossState` - prepares dash direction and displays the telegraph.
- `DashBossState` - fast charge that damages the player on hitbox overlap.

Boss phases:

| Phase | Condition | Effect |
| --- | --- | --- |
| Phase 1 | HP above 60% | Base timing and speed. |
| Phase 2 | HP <= 60% | Faster attacks, speed/damage multiplier `1.25`. |
| Phase 3 | HP <= 30% | Rage mode, speed multiplier `1.55`, damage multiplier `1.60`. |

### Loot, Combo, and Revive

- Each defeated enemy grants score.
- Enemies drop coins; mini boss and boss drops are worth more than regular enemies.
- Every 3 kills inside the `2.2s` combo window grants `+2 coins`.
- `HEART` heals `22 HP`.
- `SHIELD` grants `2.5s` of invulnerability.
- Revive costs `10 coins` and returns the player to the last safe position with partial HP.

### UI/HUD

`GameScreen` HUD shows:

- Player HP and max HP.
- Coins and revive cost.
- Revive status: `REVIVE READY` or `REVIVE LOCKED`.
- Level, location, wave, score, and difficulty.
- Control hints: `J/CLICK ATK`, `SPACE DASH`, `ESC PAUSE`.
- Wave progress bar.
- Attack cooldown bar.
- Dash status.
- Boss HP during boss fights.
- Floating damage, heal, combo, and boss phase messages.

## Assets

Runtime assets are stored in `assets/`. In `lwjgl3/build.gradle`, this folder is registered as resources:

```groovy
sourceSets.main.resources.srcDirs += [ rootProject.file('assets').path ]
```

Key files:

| Path | Purpose |
| --- | --- |
| `assets/atlas/game.atlas` | Texture atlas for player, enemies, boss, coin, and floor. |
| `assets/atlas/game.png` | Atlas image. |
| `assets/sprites/arena_floor.png` | Separate floor tile/source asset. |
| `assets/music/gameplay.wav` | Gameplay background music. |
| `assets/sounds/*.wav` | Attack, hit, death, boss phase, victory, and game over sounds. |
| `assets/ui/font.fnt` | Bitmap font for UI. |
| `assets/ui/uiskin.*` | UI skin/atlas/font resources. |
| `assets/assets.txt` | Asset list generated by the `generateAssetList` Gradle task. |

`AssetManager` registers these animation keys:

- `player.idle`, `player.run`, `player.attack`, `player.dead`
- `slime.run`, `slime.attack`, `slime.dead`
- `goblin.run`, `goblin.attack`, `goblin.dead`
- `boss.run`, `boss.attack`, `boss.dead`

`SoundManager` wraps audio loading in `try/catch`, so missing or unsupported audio should not stop the game from running.

## Configuration

Main settings are stored in `gradle.properties`:

| Key | Value |
| --- | --- |
| `gdxVersion` | `1.14.0` |
| `projectVersion` | `1.0.0` |
| `enableGraalNative` | `false` |
| `graalHelperVersion` | `2.0.1` |
| `org.gradle.daemon` | `false` |
| `org.gradle.jvmargs` | `-Xms512M -Xmx1G -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8` |

Modules are declared in `settings.gradle`:

```groovy
include 'lwjgl3', 'core'
```

The game world size is defined in `GladiatorGame`:

```java
public static final float WORLD_WIDTH = 800f;
public static final float WORLD_HEIGHT = 480f;
```

## Troubleshooting

| Problem | Cause | Solution |
| --- | --- | --- |
| Gradle tries to download `gradle-9.3.1-bin.zip` and fails without network | First wrapper run requires internet | Connect to the internet and rerun `gradlew.bat lwjgl3:run`, or pre-download the wrapper distribution. |
| `Permission denied: getsockopt` while downloading Gradle | Network access is blocked by the execution environment | Run the command in a normal terminal with network access. |
| `There is insufficient memory for the Java Runtime Environment` / small page file | Gradle JVM starts with `-Xms512M -Xmx1G` | Temporarily reduce heap through `GRADLE_OPTS` or increase the Windows page file. |
| Game runs but has no sound | Audio device/format is unavailable or files did not load | Check `assets/music/gameplay.wav` and `assets/sounds/*.wav`; the game should keep running without audio. |
| Some window icons do not show | `Lwjgl3Launcher` references `gladiator128.png`, `gladiator64.png`, `gladiator32.png`, `gladiator16.png` | Matching icon files now exist in `lwjgl3/src/main/resources/`; if icons still fail, refresh Gradle resources/build output. |
| `assets/` contains `hs_err_pid*.log` | Java crash logs were generated inside the assets folder and included in `assets.txt` | Remove them from `assets/` and regenerate the asset list if they are no longer needed for diagnostics. |

## Roadmap

No active `TODO` or `FIXME` roadmap items were found in the source code. The repository contains `GladiatorArena_GDD_v2.md`, but this README describes only what is visible in the current implementation.

## License

No root project license file (`LICENSE`) was found.

The `assets/sprites/` folder has its own license file, `assets/sprites/LICENSE.txt`:

```text
Original pixel art created for this student project.
License: CC0-1.0 / public domain dedication for project use.
Generated locally on 2026-05-12; no external sprite sources used.
```
