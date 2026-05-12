GLADIATOR ARENA — Game Design Document  |  SDP Class 2nd Year

SDP Class | 2nd Year Student Project

**  GLADIATOR ARENA  **

**Game Design Document**

Comprehensive Development Plan & Technical Specification  |  v2.0 Final

| **Platform** | Desktop (libGDX — Java) |
| --- | --- |
| **Genre** | 2D Top-Down Arena Survival |
| **Framework** | libGDX + Java + Gradle |
| **Design Patterns** | 7 GoF Patterns |
| **Sprites Format** | PNG (TextureAtlas — .atlas + .png) |
| **Development Timeline** | ~28 Days (9 Phases, 5 Milestones) |

**Tools**

| **GitHub** | **ClickUp** | **libGDX** | **draw.io** |
| --- | --- | --- | --- |
| Version Control | Task Management | Game Framework | Diagrams |

# Table of Contents

# 1. Game Concept

This section fulfills the Planning phase (SDLC Phase 1). The concept is defined in one paragraph answering all required questions: genre, core mechanic, win/lose conditions, and platform.

## 1.1 One-Paragraph Game Summary

| **Gladiator Arena **is a 2D top-down arena survival game where a lone Knight stands at the center of an 800×480 arena, facing relentless waves of enemies (Slimes and Goblins) that swarm from all edges. The Knight automatically attacks enemies within range while the player controls movement via W/A/S/D. The **win condition** is defeating the Final Boss (Demon King) on Wave 10; the **lose condition** is the Knight's HP dropping to zero. Between waves 1–9 the player picks one upgrade card (Decorator pattern) to permanently improve their build. The game runs on Desktop via libGDX and Java. |
| --- |

## 1.2 Core Answers — Planning Checklist

| **Question** | **Answer** |
| --- | --- |
| **Genre?** | 2D Top-Down Arena Survival (inspired by Brotato) |
| **Core Mechanic?** | WASD movement + auto-attack; survive 10 waves of increasing difficulty |
| **Win Condition?** | Defeat the Demon King (Final Boss) on Wave 10 — VictoryScreen appears |
| **Lose Condition?** | Knight HP reaches 0 at any point — GameOverScreen appears |
| **Platform?** | Desktop — libGDX default (Windows / macOS / Linux via runnable JAR) |

# 2. Team Roles & Responsibilities

Each member has one clear role. Responsibilities do not overlap to avoid merge conflicts and unclear ownership.

| **Role** | **Main Responsibility** | **Key Deliverables** | **SDLC Phase** |
| --- | --- | --- | --- |
| **Project Lead** | Tracks deadlines, owns ClickUp board, runs meetings, writes README | ClickUp board, README, submission package | All |
| **Lead Programmer** | Core architecture: GameStateManager, EventBus, GameManager. All PR code reviews | Architecture skeleton, review sign-offs | 3–8 |
| **Programmer A** | Player system (WASD, States, auto-attack), Decorator upgrade chain, UpgradeScreen | Player.java, all Decorators, UpgradeScreen | 3, 7 |
| **Programmer B** | Enemy system, EnemyFactory hierarchy, Boss State machine, DifficultyStrategy, wave logic | Enemy.java, factories, Boss.java, strategies | 5, 6, 8 |
| **Designer / Artist** | PNG sprite sourcing & integration, TextureAtlas packing, UI screens, draw.io diagrams | All PNGs, atlas, 3 diagrams, UI textures | 2, 9 |

# 3. Project Milestones (SDLC Schedule)

| **MS** | **Name** | **What You Must Show** | **Phases Done** | **Week** |
| --- | --- | --- | --- | --- |
| **M1** | **Design Done** | This GDD + Game Flow Diagram + Class Diagram + Level Sketch — reviewed by instructor | 1–2 | 2 |
| **M2** | **Prototype** | Knight moves WASD, enemies spawn & die, EventBus fires ENEMY_DIED — placeholder rectangles OK | 3–5 | 4 |
| **M3** | **Alpha** | All 10 waves working, Decorator upgrades apply, Boss spawns Wave 10, difficulty selection works | 6–8 | 6 |
| **M4** | **Beta** | Real PNG sprites & animations, PauseScreen working, all screens functional, bugs logged in ClickUp | 9 start | 8 |
| **M5** | **Final / Ship** | Polished JAR + GitHub Release v1.0.0 + GDD PDF + diagrams + README | 9 done | 10 |

# 4. Tool Setup (Day 1 Checklist)

## 4.1 GitHub — Version Control

| **#** | **Action** | **Why** |
| --- | --- | --- |
| **1** | Create repo gladiator-arena — set to Public | Instructor must access for review |
| **2** | Add all team members as Collaborators | Everyone can push branches |
| **3** | Protect main: require 1 PR review before merge | Prevents broken builds on main |
| **4** | Create initial README.md with project name and team names | Required in submission package |
| **5** | Tag milestones: git tag v0.1-m1 / v0.2-proto / v0.3-alpha / v1.0.0 | Tags mark each milestone delivery |

## 4.2 GitHub Workflow Rules (Mandatory)

- Never commit directly to main — always use a feature branch

- Branch naming: feature/player-movement  |  fix/collision-bug  |  feature/boss-state-machine

- Commit messages must be descriptive: 'Add Goblin AggressiveAI chase logic' — not 'update'

- Open a Pull Request when a feature is done — at least one teammate reviews before merge

- Merge to main ONLY when the build compiles and runs without crashes

- Upload GitHub Release at each milestone with the correct tag

## 4.3 ClickUp Board Columns

| **To Do** | **In Progress** | **In Review** | **Done** |
| --- | --- | --- | --- |
| All planned tasks | Assigned to 1 person | PR open, being reviewed | Merged & verified |

Every bug must be a ClickUp card with: Title, Steps to Reproduce, Expected, Actual, Severity (High/Med/Low), Assigned To.

## 4.4 libGDX Setup

- Download generator from gdx-liftoff.com — select Desktop module only, Java, Gradle

- Import into IntelliJ IDEA — wait for Gradle sync (0 red errors)

- Run DesktopLauncher.java — must show empty black window 800×480

- Create all package stubs from Section 10 before writing any logic

- Install gdx-tools (TexturePacker) for packing sprites into TextureAtlas

# 5. Sprites, Assets & Visual Spec

All sprites use PNG format — the only correct choice for 2D pixel art in libGDX because it supports full transparency (alpha channel) and is lossless (pixels stay sharp). All PNGs are packed into a single TextureAtlas (.atlas + .png) using gdx-tools TexturePacker.

## 5.1 Why PNG — Format Decision

| **Format** | **Transparency** | **Quality** | **Verdict** |
| --- | --- | --- | --- |
| **PNG** | YES — full alpha channel | Lossless — pixels stay sharp | **USE THIS** |
| **JPG** | NO — white fringe on sprites | Lossy — blurry at small sizes | **NEVER USE** |
| **GIF** | Partial (1-bit only) | libGDX does not support | **NEVER USE** |

## 5.2 Character Sprite Specifications

All characters are pixel-art style. Sprite size = the full PNG frame including empty space. Hitbox = the Rectangle used for collision — always smaller than the sprite to keep collisions feeling fair.

| **Character** | **Sprite Size** | **Hitbox** | **Walk Frames** | **Attack Frames** | **Death Frames** |
| --- | --- | --- | --- | --- | --- |
| **Knight (Player)** | **48×48 px** | 32×40 px | 4 frames | 4 frames | 6 frames |
| **Slime** | **32×32 px** | 24×20 px | 4 frames | — (no attack) | 4 frames |
| **Goblin** | **32×48 px** | 24×36 px | 6 frames | 4 frames | 4 frames |
| **Demon King (Boss)** | **96×96 px** | 72×80 px | 6 frames | 6 frames | 8 frames |

Important: Hitbox is always centred on the sprite. Knight hitbox = 32×40 centred in 48×48 frame (8px margin each side horizontally, 4px top, 4px bottom).

## 5.3 Placeholder Colours (Prototype Phase — before PNG sprites)

| **Character** | **Colour** | **Size Rendered** | **libGDX call** |
| --- | --- | --- | --- |
| **Knight** | White | 48×48 | shapeRenderer.setColor(Color.WHITE) |
| **Slime** | Lime Green | 32×32 | shapeRenderer.setColor(Color.GREEN) |
| **Goblin** | Orange | 32×48 | shapeRenderer.setColor(Color.ORANGE) |
| **Demon King** | Dark Red | 96×96 | shapeRenderer.setColor(Color.RED) |

## 5.4 TextureAtlas Workflow

- Pack all PNG frames using gdx-tools TexturePacker → outputs game.atlas + game.png

- Load once in AssetManager: manager.load("atlas/game.atlas", TextureAtlas.class)

- Access any sprite: atlas.findRegion("knight_walk_0")

- Never create new Texture() inside render() or update() — load everything once at startup

- Call manager.finishLoading() in a LoadingScreen before entering GameScreen

## 5.5 Asset Folder Structure

| **Path** | **Contents** |
| --- | --- |
| **assets/sprites/** | knight.png, slime.png, goblin.png, boss.png — individual spritesheets |
| **assets/atlas/** | game.atlas + game.png — packed TextureAtlas (generated by TexturePacker) |
| **assets/backgrounds/** | arena_floor.png — tiling stone texture, minimum 128×128 px |
| **assets/ui/** | hp_bar_bg.png, hp_bar_fill.png, upgrade_card_bg.png |
| **assets/fonts/** | pixel_font.fnt + pixel_font.png — BitmapFont always needs BOTH files |

## 5.6 Free Sprite Sources (CC0 / CC-BY)

| **Source** | **What to Search** | **License** |
| --- | --- | --- |
| **itch.io** | "0x72 dungeon tileset II", "brotato character pack" | CC0 — no credit needed |
| **kenney.nl** | "Tiny Dungeon", "Roguelike RPG pack" | CC0 — no credit needed |
| **OpenGameArt.org** | Search by tag: pixel-art, top-down, character | Check each asset — CC0 or CC-BY |

Rule: CC0 = use freely. CC-BY = must credit the author in README. Check the license page before downloading any asset.

# 6. Art Style

- Style: pixel art — consistent retro aesthetic across ALL sprites and UI elements

- Character sprite sizes: Knight 48×48, Slime 32×32, Goblin 32×48, Boss 96×96 (see Section 5.2)

- Background: tiled stone/dirt arena floor texture — arena_floor.png (128×128 minimum, tiled to fill 800×480)

- UI HP bars: rendered with ShapeRenderer (no texture needed in early build); polish later with hp_bar PNG

- Color palette: muted earth tones for arena floor, bright red for HP bars, gold for score, white for wave counter

- Animations: use libGDX Animation<TextureRegion> — split spritesheet with TextureRegion.split(texture, frameW, frameH)

- Font: BitmapFont from .fnt + .png pair — consistent size across all screens; free pixel fonts at dafont.com (filter: Bitmap)

- All text in English — consistent BitmapFont size 16pt for HUD, 24pt for screen titles

# 7. Game Entities & Mechanics

## 7.1 Player (Knight)

| **Parameter** | **Value / Description** |
| --- | --- |
| **Sprite** | knight.png — 48×48 px frame, hitbox 32×40 px (centred) |
| **Controls** | W A S D — 8-directional movement across the arena |
| **Base HP** | 100 units — does NOT reset between waves |
| **Move Speed** | 150 px/sec base — modified by SpeedBootsDecorator |
| **Attack Type** | Auto-attack: melee strike at 80 px radius, once per 1.0 sec cooldown |
| **Base Damage** | 10 units per hit — modified by FireWeaponDecorator / PoisonDecorator |
| **States** | IdleState / RunState / AttackState / DeadState (State pattern) |
| **Bounds Clamp** | MathUtils.clamp(x, 0, 800-48) and clamp(y, 0, 480-48) — cannot leave arena |

## 7.2 Controls Reference

| **Key / Button** | **Action** |
| --- | --- |
| **W / A / S / D** | Move Knight in 4 cardinal directions |
| **W+A  W+D  S+A  S+D** | Diagonal movement (8 directions total — normalise velocity vector!) |
| **ESC (during game)** | Open PauseScreen — game loop pauses, enemies freeze |
| **CLICK (UpgradeScreen)** | Select upgrade card from 3 choices |
| **CLICK (MenuScreen)** | Select difficulty: Easy / Medium / Hard |
| **CLICK (Pause / End screen)** | Resume / Try Again / Play Again / Main Menu |

## 7.3 Enemies

### 7.3.1 Slime — Wave 1+

| **Parameter** | **Value** |
| --- | --- |
| **Sprite** | slime.png — 32×32 px frame, hitbox 24×20 px (bottom-centred) |
| **HP** | 20 units |
| **Contact Damage** | 5 units/sec while overlapping Knight |
| **Move Speed** | 60 px/sec (slow, easy to dodge) |
| **Score Reward** | 10 points on kill |
| **AI Behaviour** | Wanders randomly until Knight enters 200 px sight range → switches to direct chase |
| **Factory** | SlimeFactory extends EnemyFactory |

### 7.3.2 Goblin — Wave 3+

| **Parameter** | **Value** |
| --- | --- |
| **Sprite** | goblin.png — 32×48 px frame, hitbox 24×36 px (centred) |
| **HP** | 40 units |
| **Contact Damage** | 12 units/sec while overlapping Knight |
| **Move Speed** | 100 px/sec — noticeably faster than Slime |
| **Score Reward** | 25 points on kill |
| **AI Behaviour** | Immediate aggressive chase from spawn — no wander phase |
| **Factory** | GoblinFactory extends EnemyFactory |

### 7.3.3 Demon King — Wave 10 Only (Final Boss)

| **Parameter** | **Value** |
| --- | --- |
| **Sprite** | boss.png — 96×96 px frame, hitbox 72×80 px (centred) — 2× bigger than normal enemies |
| **HP (Easy)** | 300 units |
| **HP (Medium)** | 500 units |
| **HP (Hard)** | 1000 units |
| **Contact Damage** | 20 units/sec on contact |
| **Dash Damage** | 40 units per dash hit (one-time trigger, not per-second) |
| **State Machine** | Idle (1.5s, stationary) → Chase (3.0s, 80 px/s) → Dash (0.6s, 400 px/s) → Idle loop |
| **HP Bar** | 400×16 px bar at screen bottom-centre — visible ONLY on Wave 10 |
| **Factory** | BossFactory extends EnemyFactory — HP = difficulty.getBossHp() |

# 8. Levels & Wave Progression

The arena is a single static 800×480 screen. There is no scrolling. 'Levels' are implemented as 10 waves of increasing enemy count and composition.

## 8.1 Wave Composition Table

Score per wave = (Slimes killed × 10) + (Goblins killed × 25). Values below are maximums if all enemies killed.

| **Wave** | **Enemy Composition** | **After Wave** | **Difficulty** | **Max Score** |
| --- | --- | --- | --- | --- |
| **1** | 4 Slimes | Upgrade Screen (3 random cards) | Very Easy | 40 |
| **2** | 6 Slimes | Upgrade Screen | Easy | 60 |
| **3** | 4 Slimes + 2 Goblins | Upgrade Screen | Easy+ | 90 |
| **4** | 5 Goblins + 3 Slimes | Upgrade Screen | Medium | 155 |
| **5** | 8 Goblins | Upgrade Screen | Medium | 200 |
| **6** | 6 Goblins + 4 Slimes | Upgrade Screen | Medium+ | 230 |
| **7** | 10 Goblins | Upgrade Screen | Hard | 250 |
| **8** | 8 Goblins + 5 Slimes | Upgrade Screen | Hard | 250 |
| **9** | 12 Goblins (max speed) | Upgrade Screen | Very Hard | 300 |
| **10** | **FINAL BOSS — Demon King** | **Victory OR Game Over** | **BOSS** | **1000+** |

Wave 6 fix: 6×25 + 4×10 = 230 (corrected from 190 — previously Wave 6 showed fewer points than Wave 5 despite more enemies).

## 8.2 Wave Timer — 'Wave Incoming' Countdown

A 3-second countdown with text 'Wave N incoming…' appears before each wave spawns. This gives the player time to reposition after the UpgradeScreen. Implemented as a waveCountdown float in LevelManager.

## 8.3 Arena Layout (Level Sketch for draw.io)

- 800×480 px arena — stone floor tiling texture as background

- Knight spawn: screen centre (400, 240)

- Enemy spawn edges: top (y=470), bottom (y=10), left (x=10), right (x=790) — randomly along each edge

- HUD overlay: HP bar top-left | wave counter top-centre | score top-right

- Boss HP bar: 400×16 px bar at bottom-centre (ONLY visible on Wave 10)

- No platforms, no exits, no scrolling — entire arena on one screen

# 9. Win / Lose Conditions & Screens

| **Screen** | **VictoryScreen (Win)** | **GameOverScreen (Lose)** |
| --- | --- | --- |
| **Trigger** | EventBus fires BOSS_DIED | EventBus fires PLAYER_DIED |
| **Background** | Gold / yellow — celebratory | Dark red — ominous |
| **Main Text** | Large 'VICTORY!' in white | Large 'GAME OVER' in white |
| **Stats Shown** | Final Score │ Enemies Killed │ Upgrades Collected │ Time Survived | Final Score │ Wave Reached │ Cause of Death |
| **Buttons** | 'Play Again' (Wave 1) │ 'Main Menu' | 'Try Again' (Wave 1) │ 'Main Menu' |

# 10. Design Patterns (7 GoF)

All seven patterns must be present in the final build. Each is mapped to an exact concrete use case — not a hypothetical one.

| **Pattern** | **Where Applied** | **Key Classes** | **SOLID Principle** |
| --- | --- | --- | --- |
| **Singleton** | Global access to critical managers | AssetManager, GameManager — private static instance + getInstance() | S — one manager per concern |
| **State** | Screen transitions, player behaviour, boss cycle | GameStateManager, PlayerState (Idle/Run/Attack/Dead), BossState (Idle/Chase/Dash) | O — add state without touching existing code |
| **Factory Method** | Enemy creation — each type has its own factory | EnemyFactory (abstract), SlimeFactory, GoblinFactory, BossFactory | O — new enemy = new factory only |
| **Observer** | Decoupled game event system | EventBus (Singleton), GameEvent (enum Type), EventListener (@FunctionalInterface) | D — components depend on EventBus abstraction |
| **Strategy** | Swap difficulty behaviour at runtime | DifficultyStrategy (interface), EasyDifficulty, MediumDifficulty, HardDifficulty | O — new difficulty = new class, GameManager unchanged |
| **Decorator** | Stackable player upgrades | PlayerStats (interface), BasePlayerStats, PlayerDecorator (abstract), 6 concrete decorators | O — new upgrade = new class only |
| **Template Method** | Enemy wave spawn behaviour | Enemy.onWaveSpawn() — abstract. Slime overrides with wander init; Goblin overrides with immediate chase | S — spawn behaviour isolated from movement |

Note: Template Method replaces the Command pattern from v1. Command (MoveCommand / CommandHistory) was over-engineering for a real-time arena game where undo/redo is never needed. Template Method is simpler, genuinely used, and easier to demonstrate.

## 10.1 SOLID Principles Mapping

| **S — Single Responsibility** | PlayerInputHandler handles ONLY input. Player handles ONLY state. Enemy handles ONLY movement & damage. Each class does exactly one job. |
| --- | --- |

| **O — Open/Closed** | New enemy? → new Factory subclass. New upgrade? → new Decorator subclass. New difficulty? → new Strategy class. Zero changes to existing working code in every case. |
| --- | --- |

| **L — Liskov Substitution** | Slime, Goblin, Boss all extend Enemy. Any method accepting Enemy works correctly with all subclasses. Boss.update() is a valid override of Enemy.update(). |
| --- | --- |

| **I — Interface Segregation** | Renderable and Updatable are separate interfaces. Background tile implements Renderable but NOT Updatable. No class is forced to implement methods it does not use. |
| --- | --- |

| **D — Dependency Inversion** | GameScreen depends on DifficultyStrategy interface — not EasyDifficulty directly. EventBus depends on EventListener interface. Easy to swap implementations for testing. |
| --- | --- |

# 11. Package Structure & Class Diagram

Draw all classes and interfaces below in draw.io as the Class Diagram (required at M1). Show inheritance arrows, implementation arrows, and key field names.

| **Package** | **Classes / Interfaces** |
| --- | --- |
| **com.gladiator** | GladiatorGame.java — entry point, extends Game |
| **.screens** | MenuScreen, GameScreen, PauseScreen, UpgradeScreen, GameOverScreen, VictoryScreen — all implement Screen |
| **.managers** | AssetManager (Singleton), GameStateManager (State), GameManager (Singleton + Strategy host) |
| **.events** | EventBus (Singleton + Observer), GameEvent (enum Type), EventListener (@FunctionalInterface) |
| **.entities** | Player, Enemy (abstract), Boss extends Enemy — position, HP, Rectangle bounds, PNG sprite |
| **.entities.states** | PlayerState (interface), IdleState, RunState, AttackState, DeadState |
| **.entities.boss** | BossState (interface), IdleBossState, ChaseBossState, DashBossState |
| **.factories** | EnemyFactory (abstract), SlimeFactory, GoblinFactory, BossFactory |
| **.decorator** | PlayerStats (interface), BasePlayerStats, PlayerDecorator (abstract), FireWeaponDecorator, PoisonDecorator, ShieldDecorator, ArmorDecorator, SpeedBootsDecorator, AttackSpeedDecorator |
| **.strategy** | DifficultyStrategy (interface), EasyDifficulty, MediumDifficulty, HardDifficulty |
| **assets/ (no .ai package)** | sprites/ , atlas/ , backgrounds/ , ui/ , fonts/ — all loaded via AssetManager |

Removed: .ai package (CowardlyAI, ArcherAI, PatrolAI, AggressiveAI). Enemy AI behaviour is now implemented directly inside Slime.update() and Goblin.update() — simpler, no over-engineering.

# 12. Upgrade System (Decorator Pattern)

After each of Waves 1–9, the UpgradeScreen shows 3 randomly selected Decorator cards with a brief 'Wave N cleared! Killed: X enemies' summary. The chosen upgrade wraps current stats permanently.

| **Decorator Class** | **Category** | **First Stack Effect** | **Second Stack Effect** |
| --- | --- | --- | --- |
| **FireWeaponDecorator** | Attack | +15 damage | +30 total damage |
| **PoisonDecorator** | Attack | +10 damage (no DoT — removed) | +20 total damage |
| **ShieldDecorator** | Defence | +30 max HP | +60 HP total |
| **ArmorDecorator** | Defence | -20% incoming damage | -36% total (multiplicative) |
| **SpeedBootsDecorator** | Movement | +25% move speed | +56% total speed (compounding) |
| **AttackSpeedDecorator** | Movement | -20% attack cooldown | -36% cooldown — near double rate |

PoisonDecorator change: DoT (damage-over-time) effect removed — it required a separate tick system that adds complexity for a student project. Replaced with flat +10 / +20 damage bonus instead.

**Decorator chain example: BasePlayerStats → FireWeaponDecorator → ShieldDecorator → SpeedBootsDecorator**

Each decorator calls wrapped.getX() and adds its bonus — compounding across multiple upgrades.

# 13. Difficulty System (Strategy Pattern)

Chosen on MenuScreen. Creates a DifficultyStrategy object stored in GameManager. All calculations reference this object — changing difficulty never requires changing game logic.

| **Parameter** | **Easy** | **Medium** | **Hard** |
| --- | --- | --- | --- |
| Enemy Speed Multiplier | ×0.8 | ×1.0 | ×1.3 |
| Enemy Damage Multiplier | ×0.7 | ×1.0 | ×1.5 |
| Boss HP | 300 | 500 | 1000 |
| Enemy Spawn Interval | 2.0 sec | 1.5 sec | 1.0 sec |

# 14. Screens & Game Flow Diagram

Draw this as the Game Flow Diagram in draw.io (required at M1). Every box = one Screen class. Every arrow = one transition.

| **Screen** | **Content** | **Transitions To** |
| --- | --- | --- |
| **MenuScreen** | Title, Easy / Medium / Hard buttons | → GameScreen (on difficulty click) |
| **GameScreen** | Arena, Knight, enemies, HUD (HP │ Wave │ Score) | → PauseScreen (ESC) │ → UpgradeScreen (wave cleared) │ → GameOverScreen (player dies) │ → VictoryScreen (boss dies) |
| **PauseScreen** | 'PAUSED' text, Resume button — game loop frozen | → GameScreen (Resume clicked) |
| **UpgradeScreen** | 'Wave N cleared! Killed X' + 3 card choices | → GameScreen (card selected, next wave starts after 3s countdown) |
| **GameOverScreen** | Score, wave reached, cause of death, two buttons | → GameScreen (Try Again) │ → MenuScreen (Main Menu) |
| **VictoryScreen** | Gold background, 'VICTORY!', final stats, two buttons | → GameScreen (Play Again) │ → MenuScreen (Main Menu) |

**Full flow: MenuScreen → GameScreen → [PauseScreen ↔ GameScreen] → [UpgradeScreen → GameScreen] ×9 → GameScreen (Wave 10 Boss) → VictoryScreen or GameOverScreen → MenuScreen**

# 15. Phasewise Development Plan

Each phase must be 100% working before moving to the next. Corresponds to the SDLC Build phase from the Student Guideline.

## Phase 1 — Project Setup  (Day 1–2) | M1 prerequisite

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | Download libGDX generator (gdx-liftoff.com) — Desktop only, Java, Gradle | Project files generated |
| **2** | Import into IntelliJ IDEA — wait for Gradle sync to complete | 0 red errors in IDE |
| **3** | Run DesktopLauncher.java — empty black window 800×480 opens | Window visible on screen |
| **4** | Create all package stubs from Section 11 — empty public class X {} only, no logic | All packages present |
| **5** | AssetManager.java Singleton: private static instance; public static getInstance() | Singleton compiles |
| **6** | GitHub repo live, all members added, main branch protected, first commit pushed | Repo accessible by instructor |
| **7** | ClickUp board created — all Phase 2–9 tasks added to To Do column | Board populated, team assigned |

## Phase 2 — Screens & State Machine  (Day 3–4) | M1

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | GameStateManager.java: enum State {MENU,GAME,PAUSE,UPGRADE,GAME_OVER,VICTORY} + push/pop/set() | State transitions compile |
| **2** | Create 6 stub Screen classes — each renders a distinct background colour | All 6 screens show their colour |
| **3** | MenuScreen: BitmapFont title + keys 1/2/3 → setDifficulty → transition to GameScreen | Menu navigation works |
| **4** | ESC in GameScreen → PauseScreen. Resume button → back to GameScreen | Pause/Resume cycle works |
| **5** | Git tag v0.1-design. Submit GDD PDF + all 3 diagrams for M1 review | M1 submitted on time |

## Phase 3 — Player System  (Day 5–7) | M2 start

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | Player.java: float x, y, velocityX/Y, hp, maxHp, attackTimer. Render as WHITE 48×48 rectangle placeholder | White box visible in arena |
| **2** | update(delta): read WASD via Gdx.input.isKeyPressed(). Normalise diagonal vector. x += vX*delta | 8-direction movement works |
| **3** | Clamp: x = MathUtils.clamp(x, 0, 800-48),  y = MathUtils.clamp(y, 0, 480-48) | Cannot leave screen |
| **4** | PlayerState interface + IdleState / RunState / AttackState / DeadState — wire to velocity | States switch correctly |
| **5** | Auto-attack: attackTimer -= delta; if(<=0){ attackTimer=1.0f; performAttack(); } | Attack fires every 1 second |
| **6** | Rectangle bounds = new Rectangle(x+8, y+4, 32, 40); update every frame for collision | Hitbox tracks player position |

## Phase 4 — EventBus & LevelManager  (Day 8–9) | M2

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | EventBus.java (Singleton): Map<Type, List<EventListener>> — subscribe(), unsubscribe(), post() | EventBus compiles as Singleton |
| **2** | GameEvent.java: enum Type {ENEMY_DIED, WAVE_CLEARED, PLAYER_HURT, PLAYER_DIED, BOSS_DIED} | All event types defined |
| **3** | LevelManager.java: subscribes ENEMY_DIED → decrement enemiesAlive → post WAVE_CLEARED at 0 | WAVE_CLEARED fires correctly |
| **4** | GameScreen: subscribe WAVE_CLEARED → show UpgradeScreen; subscribe PLAYER_DIED → show GameOverScreen | Screen transitions work |
| **5** | Test with temp key Q → fire ENEMY_DIED manually until WAVE_CLEARED fires. Git tag v0.2-prototype | M2 tag pushed |

## Phase 5 — Factory & Enemies  (Day 10–13)

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | Enemy.java (abstract): x, y, hp, damage, speed, Rectangle bounds — update(), render(), takeDamage(int), onWaveSpawn() | Abstract class compiles |
| **2** | Slime.java extends Enemy: onWaveSpawn() → init random wander. update() → wander until player in 200px → chase | Slime wanders then chases |
| **3** | Goblin.java extends Enemy: onWaveSpawn() → set aggressive=true. update() → always chase player directly | Goblin chases immediately |
| **4** | On bounds.overlaps(player.bounds): player.takeDamage(damage*delta). EventBus.post(PLAYER_HURT) | Knight loses HP on contact |
| **5** | EnemyFactory (abstract) + SlimeFactory + GoblinFactory — create(x,y) returns configured enemy | Both factories create correctly |
| **6** | GameScreen: spawn enemies at edges. On hp<=0 → EventBus.post(ENEMY_DIED) → Iterator.remove() | Enemies spawn, die, are removed |
| **7** | Player auto-attack: overlap check at 80px radius → enemy.takeDamage(player.getDamage()) | Player kills enemies |

## Phase 6 — Difficulty Strategy  (Day 14)

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | DifficultyStrategy interface: getEnemySpeedMult(), getEnemyDamageMult(), getBossHp(), getSpawnInterval() | Interface defined |
| **2** | EasyDifficulty / MediumDifficulty / HardDifficulty — values from Section 13 | 3 strategy classes compile |
| **3** | MenuScreen button: GameManager.getInstance().setDifficulty(new EasyDifficulty()) | Difficulty persists to GameScreen |
| **4** | EnemyFactory.create(): enemy.speed *= difficulty.getEnemySpeedMult() | Hard enemies visibly faster |
| **5** | spawnTimer in GameScreen: reset to difficulty.getSpawnInterval() each spawn | Hard mode swarms more frequently |

## Phase 7 — Decorator Upgrades  (Day 15–17) | M3 start

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | PlayerStats interface: getMaxHp(), getDamage(), getSpeed(), getAttackCooldown() | Interface compiles |
| **2** | BasePlayerStats implements PlayerStats: returns (100, 10, 150f, 1.0f) | Base values correct |
| **3** | abstract PlayerDecorator implements PlayerStats: protected PlayerStats wrapped — pass-through all methods | Decorator base compiles |
| **4** | All 6 concrete decorators from Section 12 — each overrides only its relevant getX() method | All 6 decorators add correct bonuses |
| **5** | Player.java: PlayerStats stats = new BasePlayerStats(). applyUpgrade(d) → stats = new Decorator(stats) | Stats chain builds correctly |
| **6** | UpgradeScreen: randomly pick 3 of 6 decorators, draw cards, on click → player.applyUpgrade() → GameScreen | Upgrades apply and stack |
| **7** | UpgradeScreen header shows: 'Wave N cleared! Enemies killed: X' | Kill count displays correctly |

## Phase 8 — Boss  (Day 18–20) | M3 complete

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | BossState interface: enter(), update(float delta, float px, float py), exit() | Interface defined |
| **2** | IdleBossState (1.5s stationary), ChaseBossState (80px/s, 3.0s), DashBossState (400px/s, 0.6s) | 3 boss states implemented |
| **3** | Boss.java extends Enemy: BossState currentState, changeState(BossState). Boss.update() delegates to currentState | Boss speed changes by state |
| **4** | DashBossState: set dashHitRegistered=false on enter(). On overlap: if(!dashHitRegistered){ player.takeDamage(40); dashHitRegistered=true; } | Dash does 40 dmg once per dash |
| **5** | BossFactory extends EnemyFactory: Boss hp = difficulty.getBossHp(). Sprite = 96×96 placeholder dark red | Boss HP scales with difficulty |
| **6** | GameScreen Wave 10: stop spawn timer, create Boss via BossFactory at screen edge centre | Boss appears on Wave 10 |
| **7** | Boss HP bar: shapeRenderer.rect(200, 10, boss.hp/boss.maxHp*400, 16) at screen bottom | HP bar shrinks as Boss takes damage |
| **8** | On boss.hp<=0: EventBus.post(BOSS_DIED) → VictoryScreen. Git tag v0.3-alpha for M3 | Victory triggers on boss death |

## Phase 9 — Sprites, Polish & Deploy  (Day 21–28) | M4 + M5

| **#** | **Task** | **Exit Criteria** |
| --- | --- | --- |
| **1** | Source free sprites (see Section 5.6) — CC0 or CC-BY. Check licence before download | PNG files in assets/sprites/ |
| **2** | Pack sprites into TextureAtlas using gdx-tools TexturePacker → assets/atlas/game.atlas + game.png | Atlas generated, no errors |
| **3** | Load atlas in AssetManager. Replace all placeholder rectangles with TextureRegion from atlas | Sprites render in game |
| **4** | Add walk/attack/death animations: Animation<TextureRegion> using TextureRegion.split() | Animations play correctly |
| **5** | Add arena_floor.png tiling background — render first in batch.begin() before entities | Background visible behind all entities |
| **6** | Finalize HUD: BitmapFont renders HP: X/Y │ Wave: N │ Score: S at screen top | HUD readable and accurate |
| **7** | Test: 3 difficulties × 10 waves × 6 upgrade combos. Log ALL bugs in ClickUp before fixing | Zero High-severity bugs open |
| **8** | Verify dispose(): every Texture, SpriteBatch, ShapeRenderer, Sound, Music calls dispose() | No OutOfMemoryError on 30min run |
| **9** | Remove ALL Gdx.app.log debug lines and remove ShapeRenderer hitbox overlays from release build | Clean release — no debug visible |
| **10** | ./gradlew desktop:dist → test JAR on classmate's laptop (no IntelliJ, no project folder) | JAR runs on clean machine |
| **11** | DesktopLauncher: config.title="Gladiator Arena", set icon. Upload JAR to GitHub Release v1.0.0 | M5 submitted by Week 10 |

# 16. Rendering Order (GameScreen)

libGDX rule: SpriteBatch and ShapeRenderer cannot be active at the same time. Always end one before beginning the other.

| **Step** | **Tool** | **What to Draw** |
| --- | --- | --- |
| **1** | **batch.begin()** | arena_floor.png tiling background |
| **2** | **batch** | All enemy sprites (iterate enemies list) |
| **3** | **batch** | Player / Knight sprite |
| **4** | **batch.end()** | — flush batch to GPU — |
| **5** | **shapeRenderer** | Knight HP bar (top-left) + Boss HP bar (bottom-centre, Wave 10 only) |
| **6** | **batch.begin()** | HUD text: BitmapFont — HP value │ Wave number │ Score |
| **7** | **batch.end()** | — end of frame — |

# 17. Testing Plan

Testing is not playing the game once and saying it works. Every feature must be checked deliberately and all bugs logged in ClickUp with full steps.

| **What to Test** | **How to Test** | **Pass Criteria** |
| --- | --- | --- |
| **Core Loop** | Play from MenuScreen through all 10 waves to VictoryScreen | Full run completes without crash |
| **Controls** | Test each WASD key individually + all 4 diagonals | All 8 directions work correctly |
| **Pause / Resume** | Press ESC mid-wave, press Resume, confirm enemies continue from where they stopped | Game state preserved through pause |
| **Win Condition** | Kill the Boss on Wave 10 | VictoryScreen appears with correct stats |
| **Lose Condition** | Stand in enemies until HP = 0 | GameOverScreen with wave reached |
| **All 3 Difficulties** | New game on Easy / Medium / Hard — observe enemy speed and Boss HP | Visible differences per difficulty |
| **All 6 Upgrades** | Force-select each upgrade, print stats to Gdx.app.log, check values | Each upgrade changes correct stat |
| **Upgrade Stacking** | Select same upgrade twice — verify compound effect | Stats stack as designed |
| **Boss State Machine** | Reach Wave 10, watch Boss for 15+ seconds | Idle→Chase→Dash→Idle cycle visible |
| **Edge Cases** | Walk into all 4 walls; spam WASD at corners; spam click UpgradeScreen; spam ESC | No clip, no double-select, no crash |
| **Sprite Hitboxes** | Enable hitbox debug render (ShapeRenderer outline over bounds), verify offset matches sprite | Hitbox centred on visible sprite |
| **Clean Machine Test** | Run JAR on classmate's laptop — no IDE, no project, no Java SDK visible | Game launches and runs normally |

## 17.1 Bug Report Template (ClickUp)

| **Field** | **Example** |
| --- | --- |
| **Title** | Knight clips through right boundary wall |
| **Steps** | 1. Start GameScreen  2. Walk Knight to right edge  3. Hold D key for 3 seconds |
| **Expected** | Knight stopped by MathUtils.clamp boundary |
| **Actual** | Knight partially exits screen, half the sprite disappears |
| **Severity** | High (gameplay-breaking) / Medium (visual glitch) / Low (cosmetic only) |
| **Assigned To** | Name of the teammate who will fix this |

# 18. Deployment & Submission Package

## 18.1 Build Checklist

- Remove ALL debug: delete every Gdx.app.log() call and disable ShapeRenderer hitbox overlays

- DesktopLauncher: config.title = "Gladiator Arena"; set icon via config.addIcon()

- Build: ./gradlew desktop:dist → output in desktop/build/libs/gladiator-arena.jar

- Test JAR on a machine that has NEVER had this project open (classmate's laptop)

- Upload JAR to GitHub Releases with tag v1.0.0

- Share GitHub Release link with instructor

## 18.2 Submission Package

| **Item** | **What to Include** | **Where** |
| --- | --- | --- |
| **Runnable Build** | gladiator-arena.jar (or ZIP with JAR + assets/ if needed) | GitHub Release v1.0.0 |
| **Source Code** | Full GitHub repository — must be publicly accessible | GitHub repo URL |
| **GDD** | This document exported as PDF | GitHub Release + email to instructor |
| **Diagrams** | Game Flow, Class Diagram, Level Sketch — PDF or PNG | GitHub Release + /docs folder in repo |
| **README.txt** | Controls, how to run, known issues, team names, all 7 patterns with class names | Repo root + inside ZIP |

# 19. Quick Reference — Do's and Don'ts

| **DO THIS** | **AVOID THIS** |
| --- | --- |
| Commit every day with clear messages: 'Add Slime wander-to-chase AI' | One giant commit at the end of the week |
| Keep scope: implement only what is in this GDD until M5 | Adding new enemies or screens after Beta (M4) |
| One class = one job: PlayerInputHandler only handles input | Putting all game logic inside GameScreen.java |
| Use PNG for all sprites — check licence (CC0 or CC-BY) | Using JPG (no transparency) or GIF (not supported) |
| Pack sprites into TextureAtlas — one draw call, better performance | Loading each sprite as a separate Texture in a loop |
| Test JAR on clean machine before every milestone submission | Submitting a build only tested inside IntelliJ |
| Update ClickUp board every session — move cards, log bugs | Ignoring ClickUp after Week 2 |
| Open a PR for every feature — teammate reviews before merge | Everyone pushing directly to main branch |
| Log every bug in ClickUp with full Steps to Reproduce | 'It sometimes crashes' — no steps, no owner, no severity |
| Call dispose() on every Texture, SpriteBatch, ShapeRenderer, Sound | Ignoring dispose() — causes memory leaks on longer sessions |
| Ask a classmate who never played your game to test it — watch silently | Only testing your own game where you know every path |

# 20. Final Submission Checklist

Every checkbox must be ticked before submitting. Items are ordered by milestone.

| **✓** | **What to Verify** | **Pattern / Rule** | **MS** |
| --- | --- | --- | --- |
| **☐** | All 3 diagrams drawn in draw.io: Game Flow, Class Diagram, Level Sketch | Guideline | M1 |
| **☐** | Game launches — MenuScreen shows title and Easy/Medium/Hard buttons | State | M1 |
| **☐** | AssetManager.getInstance() returns same instance from anywhere | Singleton | M1 |
| **☐** | Knight moves in all 8 WASD directions, cannot leave 800×480 screen | State | M2 |
| **☐** | Knight hitbox = 32×40 px — centred in 48×48 sprite (verify with debug render) | Sprites | M2 |
| **☐** | Knight has 4 working States: Idle / Run / Attack / Dead | State | M2 |
| **☐** | Killing an enemy fires ENEMY_DIED through EventBus | Observer | M2 |
| **☐** | LevelManager counts kills and fires WAVE_CLEARED when count reaches 0 | Observer | M2 |
| **☐** | GameManager.getInstance() Singleton returns same instance from any class | Singleton | M2 |
| **☐** | Slime wanders randomly then chases when player enters 200 px range | Template Method | M3 |
| **☐** | Goblin chases player immediately from spawn — no wander phase | Template Method | M3 |
| **☐** | Slime and Goblin created through separate Factory classes | Factory Method | M3 |
| **☐** | Easy/Medium/Hard visibly changes enemy speed and Boss HP | Strategy | M3 |
| **☐** | UpgradeScreen shows 'Wave N cleared! Killed X' + 3 random cards | Decorator | M3 |
| **☐** | All 6 upgrades apply and stack — verified via Gdx.app.log stat output | Decorator | M3 |
| **☐** | Wave 10 spawns Demon King Boss (96×96) via BossFactory | Factory Method | M4 |
| **☐** | Boss cycles: Idle (slow) → Chase (medium) → Dash (fast) → repeat | State | M4 |
| **☐** | Dash does exactly 40 damage once per dash (not per second) | State | M4 |
| **☐** | Boss HP bar at screen bottom decreases correctly | Observer | M4 |
| **☐** | Defeating Boss shows VictoryScreen with score + stats | State | M4 |
| **☐** | Knight death shows GameOverScreen with wave reached | State | M4 |
| **☐** | PauseScreen opens on ESC, Resume returns game to exact same state | State | M4 |
| **☐** | All PNG sprites render correctly — hitboxes match visual bounds | Sprites | M4 |
| **☐** | Wave 6 max score = 230 (6×25 + 4×10) — not 190 | Wave Table | M4 |
| **☐** | No Gdx.app.log or debug overlays in release build | Guideline | M5 |
| **☐** | JAR tested on clean machine that never had the project open | Guideline | M5 |
| **☐** | GitHub Release v1.0.0 uploaded with JAR + README | Guideline | M5 |
| **☐** | README lists controls, how to run, all 7 patterns with class names | Guideline | M5 |
| **☐** | dispose() called for every Texture, SpriteBatch, ShapeRenderer, Sound | libGDX | M5 |

**  GLADIATOR ARENA — Good luck. Build something you****'****re proud of!  **

SDP Class | 2nd Year | libGDX + GitHub + ClickUp | 7 GoF Patterns | v2.0 Final

libGDX + Java + 7 GoF Patterns  |  v2.0 — Final