# Gladiator Arena

Gladiator Arena is a Java/LibGDX desktop 2D arena game project. The player fights through enemy waves, chooses upgrades between rounds, and finishes the run with a boss battle.

## Features

- Main menu, pause flow, game over screen, victory screen, and upgrade screen.
- Wave-based arena combat with slimes, goblins, and a final boss.
- Difficulty strategies for easy, medium, and hard runs.
- Manual player attack on cooldown with a visible attack effect.
- Player upgrades for damage, speed, attack speed, armor, shield, fire, and poison-style stat changes.
- HUD with HP, wave, score, difficulty, attack cooldown, wave progress, character HP bars, boss HP, floating damage numbers, and boss dash telegraph.
- Safe sprite/animation fallback behavior through the asset manager when an animation is missing.

## Architecture

The project keeps gameplay code in the `core` module and the desktop launcher in the `lwjgl3` module.

Notable patterns and responsibilities:

- `screens`: LibGDX screens for menu, gameplay, pause, upgrades, victory, and game over flow.
- `entities`: player, enemies, boss, hitboxes, health, rendering, and entity state.
- `ai`: enemy and boss behavior. The boss uses explicit states for idle, chase, telegraph, and dash behavior.
- `factories`: enemy creation through factory classes.
- `decorator`: player stat upgrades using decorator-style composition.
- `strategy`: difficulty tuning through strategy classes.
- `events`: lightweight event bus for gameplay events such as enemy death, player death, boss death, and damage feedback.
- `managers`: game state, level progress, assets, and small gameplay presentation managers.

## Controls

- `WASD`: move the player.
- `Space`: manual attack.
- `Esc`: pause the game.

## Gameplay Loop

Start from the menu, choose a difficulty, then survive arena waves. Defeated enemies increase score and advance wave progress. After each cleared wave, choose an upgrade and continue. Wave 10 spawns the boss; watch for the red dash telegraph, dodge the charge, and defeat the boss to win.

## Build

Compile the desktop target:

```bash
./gradlew lwjgl3:classes
```

On Windows:

```bat
gradlew.bat lwjgl3:classes
```

## Run

Launch the desktop game:

```bash
./gradlew lwjgl3:run
```

On Windows:

```bat
gradlew.bat lwjgl3:run
```
