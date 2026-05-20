package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.entities.Boss;
import com.gladiator.arena.entities.Coin;
import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.MiniBoss;
import com.gladiator.arena.entities.PickupItem;
import com.gladiator.arena.entities.Player;
import com.gladiator.arena.entities.Portal;
import com.gladiator.arena.events.BossPhaseEvent;
import com.gladiator.arena.events.EnemyDamagedEvent;
import com.gladiator.arena.events.EventBus;
import com.gladiator.arena.events.EventListener;
import com.gladiator.arena.events.GameEvent;
import com.gladiator.arena.factories.BossFactory;
import com.gladiator.arena.factories.EnemyFactory;
import com.gladiator.arena.factories.FastEnemyFactory;
import com.gladiator.arena.factories.GoblinFactory;
import com.gladiator.arena.factories.MiniBossFactory;
import com.gladiator.arena.factories.RangedEnemyFactory;
import com.gladiator.arena.factories.SlimeFactory;
import com.gladiator.arena.factories.TankEnemyFactory;
import com.gladiator.arena.managers.AssetManager;
import com.gladiator.arena.managers.DamageNumberManager;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;
import com.gladiator.arena.managers.LevelManager;
import com.gladiator.arena.managers.SoundManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GameScreen extends ScreenAdapter {
    private static final int FINAL_LEVEL = 3;
    private static final int WAVES_PER_ROOM = 3;
    private static final float SCREEN_TRANSITION_DELAY = 0.72f;
    private static final float REVIVE_HP_PERCENT = 0.5f;
    private static final float REVIVE_HEAL_TEXT_OFFSET = 66f;
    private static final float REVIVE_POPUP_ANIMATION_DURATION = 0.18f;
    private static final float HEART_HEAL_AMOUNT = 22f;
    private static final float SHIELD_INVULNERABILITY_DURATION = 2.5f;
    private static final float ARENA_WIDTH = 800f;
    private static final float ARENA_HEIGHT = 480f;
    private static final float DEFAULT_ENEMY_WIDTH = 32f;
    private static final float DEFAULT_ENEMY_HEIGHT = 32f;
    private static final float PROGRESS_BAR_X = 16f;
    private static final float PROGRESS_BAR_Y = 420f;
    private static final float PROGRESS_BAR_WIDTH = 270f;
    private static final float PROGRESS_BAR_HEIGHT = 8f;
    private static final float ATTACK_BAR_X = 608f;
    private static final float ATTACK_BAR_Y = 420f;
    private static final float ATTACK_BAR_WIDTH = 172f;
    private static final float ATTACK_BAR_HEIGHT = 8f;
    private static final float HEALTH_BAR_HEIGHT = 5f;
    private static final float HEALTH_BAR_OFFSET_Y = 6f;
    private static final float PLAYER_HEALTH_BAR_WIDTH = 44f;
    private static final float MIN_ENEMY_HEALTH_BAR_WIDTH = 34f;
    private static final float STRONG_DAMAGE_SHAKE_THRESHOLD = 18f;
    private static final float SCREEN_SHAKE_DURATION = 0.20f;
    private static final float SCREEN_SHAKE_STRENGTH = 5.5f;
    private static final float HIT_EFFECT_DURATION = 0.22f;
    private static final float COMBO_WINDOW = 2.2f;
    private static final int COMBO_BONUS_STEP = 3;
    private static final int COMBO_BONUS_COINS = 2;
    private static final float LEVEL_CLEAR_HEAL_AMOUNT = 24f;
    private static final float BOSS_MESSAGE_DURATION = 2.15f;

    private final GladiatorGame game;
    private final GameManager gameManager;
    private final EventBus eventBus;
    private final LevelManager levelManager;
    private final DamageNumberManager damageNumberManager;
    private final AssetManager assets;
    private final EventListener waveClearedListener;
    private final EventListener playerDiedListener;
    private final EventListener bossDiedListener;
    private final EventListener enemyDamagedListener;
    private final EventListener playerHurtListener;
    private final EventListener bossPhaseListener;
    private final Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private final List<PickupItem> pickups = new ArrayList<>();
    private final List<EnemyFactory> pendingSpawnFactories = new ArrayList<>();
    private final List<HitEffect> hitEffects = new ArrayList<>();
    private final EnemyFactory slimeFactory = new SlimeFactory();
    private final EnemyFactory goblinFactory = new GoblinFactory();
    private final EnemyFactory fastEnemyFactory = new FastEnemyFactory();
    private final EnemyFactory tankEnemyFactory = new TankEnemyFactory();
    private final EnemyFactory rangedEnemyFactory = new RangedEnemyFactory();
    private final EnemyFactory miniBossFactory = new MiniBossFactory();
    private final EnemyFactory bossFactory = new BossFactory();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Matrix4 normalProjection = new Matrix4();
    private final Matrix4 shakenProjection = new Matrix4();
    private Boss activeBoss;
    private Portal portal;
    private int roomNumber;
    private int score;
    private int enemiesRemainingToSpawn;
    private float spawnTimer;
    private float screenTransitionTimer;
    private float lastSafeX;
    private float lastSafeY;
    private float screenShakeTimer;
    private float screenShakeDuration;
    private float screenShakeStrength;
    private float comboTimer;
    private float bossPhaseMessageTimer;
    private float revivePromptTimer;
    private float arenaPulseTimer;
    private int comboCount;
    private int nextComboBonusAt = COMBO_BONUS_STEP;
    private String bossPhaseMessage = "";
    private boolean disposed;
    private boolean transitioning;
    private boolean revivePromptActive;
    private boolean playerDeathPosted;
    private PendingTransition pendingTransition = PendingTransition.NONE;

    private enum PendingTransition {
        NONE,
        UPGRADE,
        GAME_OVER,
        VICTORY,
        NEXT_ROOM
    }

    public GameScreen(GladiatorGame game) {
        this(game, new Player(), 1, 0);
        GameManager.getInstance().resetRunStats();
    }

    public GameScreen(GladiatorGame game, Player player, int waveNumber, int score) {
        this(game, player, waveNumber, score, 0, false);
    }

    public GameScreen(GladiatorGame game, Player player, int waveNumber, int score, int coinCount) {
        this(game, player, waveNumber, score, coinCount, false);
    }

    public GameScreen(GladiatorGame game, Player player, int waveNumber, int score, int coinCount, boolean reviveUsed) {
        this(game, player, waveNumber, score, 1, coinCount, reviveUsed);
    }

    public GameScreen(
        GladiatorGame game,
        Player player,
        int waveNumber,
        int score,
        int roomNumber,
        int coinCount,
        boolean reviveUsed
    ) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
        this.eventBus = EventBus.getInstance();
        this.levelManager = new LevelManager(eventBus);
        this.damageNumberManager = new DamageNumberManager(eventBus);
        this.assets = AssetManager.getInstance();
        this.waveClearedListener = this::handleWaveCleared;
        this.playerDiedListener = this::handlePlayerDied;
        this.bossDiedListener = this::handleBossDied;
        this.enemyDamagedListener = this::handleEnemyDamaged;
        this.playerHurtListener = this::handlePlayerHurt;
        this.bossPhaseListener = this::handleBossPhaseChanged;
        this.player = player == null ? new Player() : player;
        this.score = score;
        this.roomNumber = MathUtils.clamp(roomNumber, 1, FINAL_LEVEL);
        if (coinCount > this.player.getCoins()) {
            this.player.setCoins(coinCount);
        }
        if (reviveUsed) {
            this.player.setReviveUsed(true);
        }
        gameManager.recordLevelReached(this.roomNumber);
        updateLastSafePosition();

        eventBus.subscribe(GameEvent.Type.WAVE_CLEARED, waveClearedListener);
        eventBus.subscribe(GameEvent.Type.PLAYER_DIED, playerDiedListener);
        eventBus.subscribe(GameEvent.Type.BOSS_DIED, bossDiedListener);
        eventBus.subscribe(GameEvent.Type.ENEMY_DAMAGED, enemyDamagedListener);
        eventBus.subscribe(GameEvent.Type.PLAYER_HURT, playerHurtListener);
        eventBus.subscribe(GameEvent.Type.BOSS_PHASE_CHANGED, bossPhaseListener);
        int safeWaveNumber = Math.max(1, Math.min(waveNumber, WAVES_PER_ROOM));
        playerDeathPosted = this.player.getHp() <= 0f;
        int enemiesInWave = prepareWaveSpawns(safeWaveNumber);
        levelManager.startWave(safeWaveNumber, enemiesInWave);
        announceWaveStart(safeWaveNumber);
    }

    @Override
    public void show() {
        gameManager.getGameStateManager().set(GameStateManager.State.GAME);
        SoundManager.getInstance().startGameplayMusic();
    }

    @Override
    public void render(float delta) {
        if (transitioning) {
            updateScreenTransition(delta);
            if (disposed) {
                return;
            }
        }

        if (revivePromptActive) {
            handleRevivePromptInput();
            arenaPulseTimer += delta;
            updateRevivePrompt(delta);
            damageNumberManager.update(delta);
            updateHitEffects(delta);
            updateScreenShake(delta);
            updateBossPhaseMessage(delta);
            renderGame();
            return;
        }

        if (!transitioning && Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            SoundManager.getInstance().toggleMusic(true);
        }

        if (!transitioning && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameManager.getGameStateManager().push(GameStateManager.State.PAUSE);
            game.setScreen(new PauseScreen(game, this));
            return;
        }

        if (!transitioning) {
            updateSpawning(delta);
            updateEnemies(delta);
            if (transitioning) {
                renderGame();
                return;
            }
            player.update(delta, enemies);
            updateCoins(delta);
            updatePickups(delta);
            updatePortal(delta);
            if (transitioning) {
                renderGame();
                return;
            }
            removeDeadEnemies();
            if (transitioning) {
                renderGame();
                return;
            }
            resolvePlayerDeath();
            updateLastSafePosition();
        }
        damageNumberManager.update(delta);
        updateHitEffects(delta);
        updateScreenShake(delta);
        updateCombo(delta);
        updateBossPhaseMessage(delta);
        arenaPulseTimer += delta;

        renderGame();
    }

    private void renderGame() {
        ScreenUtils.clear(getClearRed(), getClearGreen(), getClearBlue(), 1f);
        applyScreenShake();

        game.getBatch().begin();
        assets.drawTiledFloor(game.getBatch(), ARENA_WIDTH, ARENA_HEIGHT);
        game.getBatch().end();

        drawLocationOverlay();

        game.getBatch().begin();
        for (Coin coin : coins) {
            coin.render(game.getBatch(), assets);
        }
        if (portal != null) {
            portal.render(game.getBatch(), assets);
        }
        for (Enemy enemy : enemies) {
            enemy.render(game.getBatch(), assets);
        }
        player.render(game.getBatch(), assets);
        game.getBatch().end();

        drawEntityFallbacks();
        drawBossDashTelegraph();
        drawHudPanel();
        drawCharacterHealthBars();
        drawAttackEffect();
        drawHitEffects();
        drawWaveProgressBar();
        drawAttackCooldownBar();

        game.getBatch().begin();
        damageNumberManager.render(game.getBatch(), game.getFont());
        float hudY = 466f;
        String hud = "HP " + (int) player.getHp() + "/" + (int) player.getMaxHp()
            + "   COINS " + player.getCoins() + "/" + player.getReviveCost()
            + "   " + buildReviveStatus()
            + "   LEVEL " + roomNumber + " " + getLocationName()
            + "   WAVE " + levelManager.getCurrentWave()
            + "   SCORE " + score
            + "   " + gameManager.getDifficultyName().toUpperCase(Locale.ROOT)
            + "   J/CLICK ATK"
            + "   SPACE DASH"
            + "   ESC PAUSE";
        ArenaUi.drawCenteredFit(game.getFont(), game.getBatch(), hud, ARENA_WIDTH / 2f, hudY, 0.74f, ArenaUi.BONE, 756f);
        if (activeBoss != null && !activeBoss.isDead()) {
            float bossHpPercent = MathUtils.clamp(activeBoss.getHp() / activeBoss.getMaxHp(), 0f, 1f);
            ArenaUi.drawCentered(game.getFont(), game.getBatch(), "BOSS HP: " + (int) activeBoss.getHp()
                + "/" + (int) activeBoss.getMaxHp()
                + " (" + (int) (bossHpPercent * 100f) + "%)", 400f, 28f, 0.86f, ArenaUi.PALE_GOLD);
        }
        ArenaUi.drawText(game.getFont(), game.getBatch(), buildWaveProgressText(), PROGRESS_BAR_X, PROGRESS_BAR_Y - 8f, 0.78f, ArenaUi.GOLD);
        ArenaUi.drawText(game.getFont(), game.getBatch(), SoundManager.getInstance().getMusicStatusText(), PROGRESS_BAR_X, PROGRESS_BAR_Y - 24f, 0.72f, ArenaUi.BONE);
        ArenaUi.drawText(game.getFont(), game.getBatch(), buildAttackCooldownText(), ATTACK_BAR_X, ATTACK_BAR_Y - 8f, 0.78f, ArenaUi.GOLD);
        ArenaUi.drawText(game.getFont(), game.getBatch(), buildDashCooldownText(), ATTACK_BAR_X, ATTACK_BAR_Y - 24f, 0.72f, ArenaUi.BONE);
        drawComboText();
        drawBossPhaseMessage();
        if (player.isInvulnerable()) {
            ArenaUi.drawCentered(game.getFont(), game.getBatch(), "REVIVED", player.getCenterX(), player.getY() + 72f, 0.72f, ArenaUi.PALE_GOLD);
        }
        game.getBatch().end();

        drawRevivePrompt();
        drawFadeOverlay();
        resetProjection();
    }

    private void applyScreenShake() {
        normalProjection.set(game.getBatch().getProjectionMatrix());
        if (screenShakeTimer <= 0f || screenShakeDuration <= 0f) {
            return;
        }

        float strength = screenShakeStrength * MathUtils.clamp(screenShakeTimer / screenShakeDuration, 0f, 1f);
        float offsetX = MathUtils.random(-strength, strength);
        float offsetY = MathUtils.random(-strength, strength);
        shakenProjection.set(normalProjection).translate(offsetX, offsetY, 0f);
        game.getBatch().setProjectionMatrix(shakenProjection);
        shapeRenderer.setProjectionMatrix(shakenProjection);
    }

    private void resetProjection() {
        game.getBatch().setProjectionMatrix(normalProjection);
        shapeRenderer.setProjectionMatrix(normalProjection);
    }

    private float getClearRed() {
        if (roomNumber == 2) {
            return 0.15f;
        }
        if (roomNumber >= 3) {
            return 0.035f;
        }
        return 0.08f;
    }

    private float getClearGreen() {
        if (roomNumber == 2) {
            return 0.055f;
        }
        if (roomNumber >= 3) {
            return 0.035f;
        }
        return 0.07f;
    }

    private float getClearBlue() {
        if (roomNumber == 2) {
            return 0.025f;
        }
        if (roomNumber >= 3) {
            return 0.075f;
        }
        return 0.06f;
    }

    private void updateScreenTransition(float delta) {
        screenTransitionTimer -= delta;
        if (screenTransitionTimer > 0f) {
            return;
        }

        PendingTransition next = pendingTransition;
        pendingTransition = PendingTransition.NONE;
        transitioning = false;

        if (next == PendingTransition.UPGRADE) {
            game.setScreen(new UpgradeScreen(
                game,
                player,
                levelManager.getCurrentSummary(),
                score,
                roomNumber,
                player.getCoins(),
                player.isReviveUsed()
            ));
            dispose();
        } else if (next == PendingTransition.GAME_OVER) {
            game.setScreen(new GameOverScreen(game, levelManager.getCurrentWave(), score, roomNumber));
            dispose();
        } else if (next == PendingTransition.VICTORY) {
            game.setScreen(new VictoryScreen(game, score));
            dispose();
        } else if (next == PendingTransition.NEXT_ROOM) {
            game.setScreen(new GameScreen(
                game,
                player,
                1,
                score,
                roomNumber + 1,
                player.getCoins(),
                player.isReviveUsed()
            ));
            dispose();
        }
    }

    private void updateHitEffects(float delta) {
        Iterator<HitEffect> iterator = hitEffects.iterator();
        while (iterator.hasNext()) {
            HitEffect effect = iterator.next();
            effect.update(delta);
            if (effect.isDone()) {
                iterator.remove();
            }
        }
    }

    private void updateScreenShake(float delta) {
        screenShakeTimer -= delta;
        if (screenShakeTimer < 0f) {
            screenShakeTimer = 0f;
            screenShakeStrength = 0f;
            screenShakeDuration = 0f;
        }
    }

    private void updateCombo(float delta) {
        if (comboTimer <= 0f) {
            comboCount = 0;
            nextComboBonusAt = COMBO_BONUS_STEP;
            return;
        }

        comboTimer -= delta;
        if (comboTimer <= 0f) {
            comboTimer = 0f;
            comboCount = 0;
            nextComboBonusAt = COMBO_BONUS_STEP;
        }
    }

    private void updateBossPhaseMessage(float delta) {
        bossPhaseMessageTimer -= delta;
        if (bossPhaseMessageTimer < 0f) {
            bossPhaseMessageTimer = 0f;
        }
    }

    private void updateRevivePrompt(float delta) {
        revivePromptTimer += delta;
        if (revivePromptTimer > REVIVE_POPUP_ANIMATION_DURATION) {
            revivePromptTimer = REVIVE_POPUP_ANIMATION_DURATION;
        }
    }

    private void startScreenShake(float strength, float duration) {
        screenShakeStrength = Math.max(screenShakeStrength, strength);
        screenShakeDuration = Math.max(duration, 0.01f);
        screenShakeTimer = Math.max(screenShakeTimer, duration);
    }

    private void startScreenTransition(PendingTransition next) {
        if (transitioning) {
            return;
        }

        if (next == PendingTransition.VICTORY) {
            SoundManager.getInstance().playVictory();
        } else if (next == PendingTransition.GAME_OVER) {
            SoundManager.getInstance().playGameOver();
        }

        transitioning = true;
        pendingTransition = next;
        screenTransitionTimer = SCREEN_TRANSITION_DELAY;
    }

    private void drawEntityFallbacks() {
        if (coins.isEmpty() && pickups.isEmpty() && portal == null) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Coin coin : coins) {
            coin.renderFallback(shapeRenderer);
        }
        for (PickupItem pickup : pickups) {
            pickup.renderFallback(shapeRenderer);
        }
        if (portal != null) {
            portal.renderFallback(shapeRenderer);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawLocationOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (roomNumber <= 1) {
            drawDungeonOverlay();
        } else if (roomNumber == 2) {
            drawFireArenaOverlay();
        } else {
            drawDarkCastleOverlay();
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawDungeonOverlay() {
        float torchPulse = 0.18f + 0.07f * MathUtils.sin(arenaPulseTimer * 3.4f);
        shapeRenderer.setColor(0.08f, 0.07f, 0.055f, 0.28f);
        shapeRenderer.rect(0f, 0f, ARENA_WIDTH, ARENA_HEIGHT);
        shapeRenderer.setColor(0.80f, 0.48f, 0.16f, torchPulse);
        shapeRenderer.circle(90f, 380f, 54f);
        shapeRenderer.circle(710f, 380f, 54f);
        shapeRenderer.setColor(1f, 0.66f, 0.16f, 0.70f);
        shapeRenderer.triangle(82f, 372f, 90f, 408f, 98f, 372f);
        shapeRenderer.triangle(702f, 372f, 710f, 408f, 718f, 372f);
        shapeRenderer.setColor(0.20f, 0.15f, 0.12f, 0.45f);
        shapeRenderer.rect(34f, 0f, 26f, ARENA_HEIGHT);
        shapeRenderer.rect(740f, 0f, 26f, ARENA_HEIGHT);
        shapeRenderer.rectLine(0f, 76f, ARENA_WIDTH, 76f, 7f);
        shapeRenderer.setColor(0.42f, 0.31f, 0.21f, 0.32f);
        for (int i = 0; i < 6; i++) {
            float x = 150f + i * 92f;
            shapeRenderer.rectLine(x, 70f, x + 22f, 92f, 3f);
            shapeRenderer.rectLine(x + 22f, 92f, x + 44f, 70f, 3f);
        }
    }

    private void drawFireArenaOverlay() {
        shapeRenderer.setColor(0.55f, 0.06f, 0.01f, 0.38f);
        shapeRenderer.rect(0f, 0f, ARENA_WIDTH, ARENA_HEIGHT);
        shapeRenderer.setColor(1f, 0.28f, 0.02f, 0.42f);
        shapeRenderer.rectLine(0f, 72f, ARENA_WIDTH, 132f, 24f);
        shapeRenderer.rectLine(0f, 342f, ARENA_WIDTH, 284f, 20f);
        shapeRenderer.setColor(1f, 0.76f, 0.14f, 0.32f);
        shapeRenderer.rectLine(28f, 90f, 770f, 114f, 6f);
        shapeRenderer.rectLine(36f, 326f, 760f, 302f, 5f);
        for (int i = 0; i < 8; i++) {
            float x = 64f + i * 94f;
            float flame = 5f * MathUtils.sin(arenaPulseTimer * 4f + i);
            shapeRenderer.triangle(x - 12f, 40f, x, 80f + flame, x + 12f, 40f);
            shapeRenderer.triangle(x - 9f, 400f, x, 440f - flame, x + 9f, 400f);
        }
        shapeRenderer.setColor(1f, 0.92f, 0.30f, 0.48f);
        for (int i = 0; i < 12; i++) {
            float x = 44f + i * 64f;
            float y = 118f + 26f * MathUtils.sin(arenaPulseTimer * 1.7f + i * 0.8f);
            shapeRenderer.circle(x, y, 2.5f);
        }
    }

    private void drawDarkCastleOverlay() {
        float fog = 0.18f + 0.05f * MathUtils.sin(arenaPulseTimer * 1.8f);
        shapeRenderer.setColor(0.015f, 0.018f, 0.07f, 0.58f);
        shapeRenderer.rect(0f, 0f, ARENA_WIDTH, ARENA_HEIGHT);
        shapeRenderer.setColor(0.32f, 0.29f, 0.58f, 0.25f);
        shapeRenderer.circle(402f, 390f, 68f);
        shapeRenderer.setColor(0.05f, 0.035f, 0.09f, 0.36f);
        shapeRenderer.circle(432f, 404f, 56f);
        shapeRenderer.setColor(0.18f, 0.06f, 0.28f, 0.38f);
        shapeRenderer.rect(0f, 0f, 96f, ARENA_HEIGHT);
        shapeRenderer.rect(ARENA_WIDTH - 96f, 0f, 96f, ARENA_HEIGHT);
        shapeRenderer.setColor(0.04f, 0.03f, 0.08f, 0.52f);
        for (int i = 0; i < 5; i++) {
            float x = 130f + i * 135f;
            shapeRenderer.rect(x, 0f, 34f, ARENA_HEIGHT);
            shapeRenderer.triangle(x - 10f, ARENA_HEIGHT, x + 17f, ARENA_HEIGHT - 46f, x + 44f, ARENA_HEIGHT);
            shapeRenderer.setColor(0.78f, 0.64f, 0.22f, 0.20f);
            shapeRenderer.rect(x + 10f, 280f, 10f, 24f);
            shapeRenderer.setColor(0.04f, 0.03f, 0.08f, 0.52f);
        }
        shapeRenderer.setColor(0.36f, 0.18f, 0.52f, fog);
        shapeRenderer.rectLine(0f, 112f, ARENA_WIDTH, 144f, 18f);
        shapeRenderer.rectLine(0f, 248f, ARENA_WIDTH, 220f, 14f);
    }

    private void drawFadeOverlay() {
        if (!transitioning) {
            return;
        }

        float alpha = 1f - MathUtils.clamp(screenTransitionTimer / SCREEN_TRANSITION_DELAY, 0f, 1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.72f * alpha);
        shapeRenderer.rect(0f, 0f, ARENA_WIDTH, ARENA_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawRevivePrompt() {
        if (!revivePromptActive) {
            return;
        }

        boolean canRevive = player.canAffordRevive();
        float progress = MathUtils.clamp(revivePromptTimer / REVIVE_POPUP_ANIMATION_DURATION, 0f, 1f);
        float smooth = 1f - (1f - progress) * (1f - progress);
        float panelWidth = 462f * (0.88f + 0.12f * smooth);
        float panelHeight = 190f * (0.88f + 0.12f * smooth);
        float panelX = ARENA_WIDTH / 2f - panelWidth / 2f;
        float panelY = ARENA_HEIGHT / 2f - panelHeight / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.35f + 0.30f * smooth);
        shapeRenderer.rect(0f, 0f, ARENA_WIDTH, ARENA_HEIGHT);
        ArenaUi.drawPanel(shapeRenderer, panelX, panelY, panelWidth, panelHeight, ArenaUi.INK, canRevive ? ArenaUi.GOLD : ArenaUi.RED);
        if (canRevive) {
            ArenaUi.drawThinPanel(shapeRenderer, 248f, 164f, 132f, 36f, ArenaUi.GREEN, ArenaUi.GOLD);
            ArenaUi.drawThinPanel(shapeRenderer, 420f, 164f, 132f, 36f, ArenaUi.RED, ArenaUi.GOLD);
        } else {
            ArenaUi.drawThinPanel(shapeRenderer, 326f, 164f, 148f, 36f, ArenaUi.RED, ArenaUi.GOLD);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.getBatch().begin();
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "YOU DIED", 400f, 310f, 1.46f, ArenaUi.RED);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "Revive for " + player.getReviveCost() + " coins?", 400f, 272f, 0.94f, ArenaUi.PALE_GOLD);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "Current coins: " + player.getCoins(), 400f, 242f, 0.84f, ArenaUi.BONE);
        if (canRevive) {
            ArenaUi.drawCentered(game.getFont(), game.getBatch(), "[R] Revive", 314f, 190f, 0.78f, ArenaUi.BONE);
            ArenaUi.drawCentered(game.getFont(), game.getBatch(), "[ESC] Give Up", 486f, 190f, 0.72f, ArenaUi.BONE);
        } else {
            ArenaUi.drawCentered(game.getFont(), game.getBatch(), "Not enough coins", 400f, 218f, 0.82f, ArenaUi.RED);
            ArenaUi.drawCentered(game.getFont(), game.getBatch(), "[ESC] Give Up", 400f, 190f, 0.72f, ArenaUi.BONE);
        }
        game.getBatch().end();
    }

    private void drawBossDashTelegraph() {
        if (activeBoss == null || activeBoss.isDead() || !activeBoss.isDashTelegraphVisible()) {
            return;
        }

        float startX = activeBoss.getCenterX();
        float startY = activeBoss.getCenterY();
        float endX = startX + activeBoss.getPreparedDashX() * ARENA_WIDTH;
        float endY = startY + activeBoss.getPreparedDashY() * ARENA_WIDTH;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.05f, 0.02f, 0.18f);
        shapeRenderer.rectLine(startX, startY, endX, endY, 32f);
        shapeRenderer.setColor(1f, 0.16f, 0.08f, 0.88f);
        shapeRenderer.rectLine(startX, startY, endX, endY, 5f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawHudPanel() {
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ArenaUi.drawThinPanel(shapeRenderer, 10f, 448f, 780f, 26f, ArenaUi.INK, ArenaUi.GOLD);
        if (activeBoss != null && !activeBoss.isDead()) {
            ArenaUi.drawThinPanel(shapeRenderer, 276f, 8f, 248f, 30f, ArenaUi.INK, ArenaUi.RED);
        }
        shapeRenderer.end();
    }

    private void drawCharacterHealthBars() {
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHealthBar(
            player.getCenterX() - PLAYER_HEALTH_BAR_WIDTH / 2f,
            player.getY() + Player.SPRITE_HEIGHT + HEALTH_BAR_OFFSET_Y,
            PLAYER_HEALTH_BAR_WIDTH,
            player.getHp(),
            player.getMaxHp(),
            0.12f,
            0.9f,
            0.22f
        );

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                continue;
            }

            float width = Math.max(MIN_ENEMY_HEALTH_BAR_WIDTH, enemy.getSpriteWidth());
            drawHealthBar(
                enemy.getCenterX() - width / 2f,
                enemy.getY() + enemy.getSpriteHeight() + HEALTH_BAR_OFFSET_Y,
                width,
                enemy.getHp(),
                enemy.getMaxHp(),
                0.95f,
                0.18f,
                0.12f
            );
        }
        shapeRenderer.end();
    }

    private void drawHealthBar(
        float x,
        float y,
        float width,
        float hp,
        float maxHp,
        float fillRed,
        float fillGreen,
        float fillBlue
    ) {
        float safeWidth = Math.max(1f, width);
        float clampedX = MathUtils.clamp(x, 2f, ARENA_WIDTH - safeWidth - 2f);
        float clampedY = MathUtils.clamp(y, 2f, ARENA_HEIGHT - HEALTH_BAR_HEIGHT - 2f);
        float percent = maxHp <= 0f ? 0f : MathUtils.clamp(hp / maxHp, 0f, 1f);

        shapeRenderer.setColor(0f, 0f, 0f, 0.95f);
        shapeRenderer.rect(clampedX - 1f, clampedY - 1f, safeWidth + 2f, HEALTH_BAR_HEIGHT + 2f);
        shapeRenderer.setColor(0.28f, 0.04f, 0.04f, 1f);
        shapeRenderer.rect(clampedX, clampedY, safeWidth, HEALTH_BAR_HEIGHT);
        shapeRenderer.setColor(fillRed, fillGreen, fillBlue, 1f);
        shapeRenderer.rect(clampedX, clampedY, safeWidth * percent, HEALTH_BAR_HEIGHT);
    }

    private void drawAttackEffect() {
        if (!player.isAttackEffectActive()) {
            return;
        }

        float progress = player.getAttackEffectProgress();
        float width = 4f + progress * 8f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.75f, 0.12f, 0.22f * progress);
        shapeRenderer.circle(player.getAttackStartX(), player.getAttackStartY(), 22f + (1f - progress) * 10f);
        shapeRenderer.setColor(1f, 0.92f, 0.32f, 0.9f * progress);
        shapeRenderer.rectLine(
            player.getAttackStartX(),
            player.getAttackStartY(),
            player.getAttackEndX(),
            player.getAttackEndY(),
            width
        );
        shapeRenderer.setColor(1f, 0.98f, 0.62f, 0.85f * progress);
        shapeRenderer.circle(player.getAttackEndX(), player.getAttackEndY(), 5f + progress * 5f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawHitEffects() {
        if (hitEffects.isEmpty()) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (HitEffect effect : hitEffects) {
            float progress = effect.getProgress();
            float alpha = effect.getAlpha();
            float radius = 5f + progress * 12f;

            shapeRenderer.setColor(1f, 0.22f, 0.12f, 0.32f * alpha);
            shapeRenderer.circle(effect.x, effect.y, radius);
            shapeRenderer.setColor(1f, 0.86f, 0.28f, 0.80f * alpha);
            shapeRenderer.rectLine(effect.x - radius, effect.y, effect.x + radius, effect.y, 3f);
            shapeRenderer.rectLine(effect.x, effect.y - radius, effect.x, effect.y + radius, 3f);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawWaveProgressBar() {
        float progress = getWaveProgress();
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ArenaUi.drawProgressBar(
            shapeRenderer,
            PROGRESS_BAR_X,
            PROGRESS_BAR_Y,
            PROGRESS_BAR_WIDTH,
            PROGRESS_BAR_HEIGHT,
            progress,
            ArenaUi.GOLD
        );
        shapeRenderer.end();
    }

    private float getWaveProgress() {
        if (portal != null) {
            return 1f;
        }
        if (activeBoss != null && activeBoss.getMaxHp() > 0f) {
            return MathUtils.clamp(1f - activeBoss.getHp() / activeBoss.getMaxHp(), 0f, 1f);
        }

        return MathUtils.clamp(levelManager.getWaveProgress(), 0f, 1f);
    }

    private boolean isFinalRoom() {
        return roomNumber >= FINAL_LEVEL;
    }

    private String getLocationName() {
        if (roomNumber == 2) {
            return "Fire Arena";
        }
        if (roomNumber >= 3) {
            return "Dark Castle";
        }
        return "Dungeon";
    }

    private String buildWaveProgressText() {
        if (portal != null) {
            return "Portal: enter Level " + Math.min(roomNumber + 1, FINAL_LEVEL);
        }
        if (activeBoss != null && activeBoss.getMaxHp() > 0f) {
            return "Wave Progress: Boss " + (int) (getWaveProgress() * 100f) + "%";
        }

        int totalEnemies = levelManager.getEnemiesTotalThisWave();
        return "Wave Progress: " + levelManager.getEnemiesKilledThisWave() + "/" + totalEnemies;
    }

    private int prepareWaveSpawns(int waveNumber) {
        enemies.clear();
        coins.clear();
        pickups.clear();
        pendingSpawnFactories.clear();
        activeBoss = null;
        portal = null;

        if (waveNumber == 1) {
            addPendingEnemies(slimeFactory, 3 + roomNumber);
            if (roomNumber >= 2) {
                addPendingEnemies(fastEnemyFactory, 1);
            }
        } else if (waveNumber == 2) {
            spawnMiniBoss();
            addPendingEnemies(goblinFactory, 2);
            addPendingEnemies(fastEnemyFactory, 1 + roomNumber);
            if (roomNumber >= 2) {
                addPendingEnemies(tankEnemyFactory, 1);
            }
            if (roomNumber >= 3) {
                addPendingEnemies(rangedEnemyFactory, 1);
            }
        } else {
            activeBoss = createBossAtEdgeCenter();
            enemies.add(activeBoss);
            enemiesRemainingToSpawn = 0;
            spawnTimer = 0f;
            return enemies.size();
        }

        addLevelBonusEnemies();
        Collections.shuffle(pendingSpawnFactories);
        enemiesRemainingToSpawn = pendingSpawnFactories.size();
        spawnTimer = 0f;
        return enemiesRemainingToSpawn + enemies.size();
    }

    private void announceWaveStart(int waveNumber) {
        if (waveNumber == 1) {
            bossPhaseMessage = "Level " + roomNumber + ": " + getLocationName();
        } else if (waveNumber == 2) {
            bossPhaseMessage = "Wave 2: Mini Boss!";
            startScreenShake(SCREEN_SHAKE_STRENGTH * 0.45f, SCREEN_SHAKE_DURATION);
        } else {
            bossPhaseMessage = isFinalRoom() ? "FINAL BOSS!" : "Wave 3: Main Boss!";
            SoundManager.getInstance().playBossPhase();
            startScreenShake(SCREEN_SHAKE_STRENGTH * 0.70f, SCREEN_SHAKE_DURATION);
        }
        bossPhaseMessageTimer = BOSS_MESSAGE_DURATION;
    }

    private void addPendingEnemies(EnemyFactory factory, int count) {
        for (int i = 0; i < count; i++) {
            pendingSpawnFactories.add(factory);
        }
    }

    private void addLevelBonusEnemies() {
        if (roomNumber >= 2) {
            addPendingEnemies(fastEnemyFactory, 1);
        }
        if (roomNumber >= 3) {
            addPendingEnemies(tankEnemyFactory, 1);
            addPendingEnemies(rangedEnemyFactory, 1);
        }
    }

    private void spawnMiniBoss() {
        enemies.add(createMiniBossAtEdgeCenter());
        bossPhaseMessage = "MINI BOSS APPEARED!";
        bossPhaseMessageTimer = BOSS_MESSAGE_DURATION;
        SoundManager.getInstance().playBossPhase();
        startScreenShake(SCREEN_SHAKE_STRENGTH, SCREEN_SHAKE_DURATION);
    }

    private void updateSpawning(float delta) {
        if (enemiesRemainingToSpawn <= 0 || transitioning) {
            return;
        }

        spawnTimer -= delta;
        if (spawnTimer > 0f) {
            return;
        }

        while (enemiesRemainingToSpawn > 0 && spawnTimer <= 0f) {
            EnemyFactory factory = pendingSpawnFactories.remove(pendingSpawnFactories.size() - 1);
            enemies.add(createAtRandomEdge(factory));
            enemiesRemainingToSpawn--;
            spawnTimer += gameManager.getDifficulty().getSpawnInterval();
        }
    }

    private void updateCoins(float delta) {
        Iterator<Coin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            Coin coin = iterator.next();
            coin.update(delta);
            if (player.getHp() > 0f && coin.overlaps(player)) {
                player.addCoins(coin.getValue());
                gameManager.recordCoinsCollected(coin.getValue());
                damageNumberManager.addText(
                    "+" + coin.getValue() + " COIN",
                    player.getCenterX(),
                    player.getY() + REVIVE_HEAL_TEXT_OFFSET,
                    ArenaUi.GOLD
                );
                iterator.remove();
            }
        }
    }

    private void updatePickups(float delta) {
        Iterator<PickupItem> iterator = pickups.iterator();
        while (iterator.hasNext()) {
            PickupItem pickup = iterator.next();
            pickup.update(delta);
            if (player.getHp() <= 0f || !pickup.overlaps(player)) {
                continue;
            }

            if (pickup.getType() == PickupItem.Type.HEART) {
                float healed = player.heal(HEART_HEAL_AMOUNT);
                if (healed > 0f) {
                    damageNumberManager.addHealText(
                        player.getCenterX(),
                        player.getY() + REVIVE_HEAL_TEXT_OFFSET,
                        MathUtils.round(healed)
                    );
                } else {
                    damageNumberManager.addText("HP FULL", player.getCenterX(), player.getY() + REVIVE_HEAL_TEXT_OFFSET, ArenaUi.BONE);
                }
            } else {
                player.grantInvulnerability(SHIELD_INVULNERABILITY_DURATION);
                damageNumberManager.addText("SHIELD", player.getCenterX(), player.getY() + REVIVE_HEAL_TEXT_OFFSET, ArenaUi.BLUE);
            }
            iterator.remove();
        }
    }

    private void updatePortal(float delta) {
        if (portal == null || player.getHp() <= 0f) {
            return;
        }

        portal.update(delta);
        if (!transitioning && portal.overlaps(player)) {
            startScreenTransition(PendingTransition.NEXT_ROOM);
        }
    }

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            enemy.update(delta, player);
            if (player.getHp() <= 0f) {
                resolvePlayerDeath();
                return;
            }
        }
    }

    private void removeDeadEnemies() {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (!enemy.isDead()) {
                continue;
            }
            if (!enemy.isReadyToRemove()) {
                continue;
            }

            score += enemy.getScoreReward();
            gameManager.recordEnemyKill();
            recordComboKill();
            dropLoot(enemy);
            iterator.remove();
            if (enemy == activeBoss) {
                activeBoss = null;
            }
            if (enemy instanceof Boss) {
                eventBus.post(new GameEvent(GameEvent.Type.BOSS_DIED, enemy));
                if (transitioning) {
                    return;
                }
                continue;
            }

            eventBus.post(GameEvent.Type.ENEMY_DIED);
            if (transitioning) {
                return;
            }
        }
    }

    private void recordComboKill() {
        comboCount = comboTimer > 0f ? comboCount + 1 : 1;
        comboTimer = COMBO_WINDOW;
        gameManager.recordBestCombo(comboCount);
        if (comboCount >= nextComboBonusAt) {
            giveComboBonus();
            nextComboBonusAt += COMBO_BONUS_STEP;
        }
    }

    private void giveComboBonus() {
        player.addCoins(COMBO_BONUS_COINS);
        gameManager.recordCoinsCollected(COMBO_BONUS_COINS);
        damageNumberManager.addText(
            "COMBO BONUS +" + COMBO_BONUS_COINS + " COINS",
            player.getCenterX(),
            player.getY() + REVIVE_HEAL_TEXT_OFFSET,
            ArenaUi.PALE_GOLD
        );
        startScreenShake(SCREEN_SHAKE_STRENGTH * 0.35f, SCREEN_SHAKE_DURATION * 0.75f);
    }

    private void dropLoot(Enemy enemy) {
        if (enemy == null) {
            return;
        }

        int value = 2;
        if (enemy instanceof Boss) {
            value = 10;
        } else if (enemy instanceof MiniBoss) {
            value = 5;
        } else if (enemy.getScoreReward() >= 25) {
            value = 3;
        }
        coins.add(new Coin(enemy.getCenterX(), enemy.getCenterY(), value));

        if (enemy instanceof Boss || enemy instanceof MiniBoss || MathUtils.random() < 0.18f) {
            pickups.add(new PickupItem(PickupItem.Type.HEART, enemy.getCenterX() + 18f, enemy.getCenterY()));
        } else if (MathUtils.random() < 0.08f) {
            pickups.add(new PickupItem(PickupItem.Type.SHIELD, enemy.getCenterX() + 18f, enemy.getCenterY()));
        }
    }

    private void drawAttackCooldownBar() {
        float progress = player.getAttackReadyProgress();
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ArenaUi.drawProgressBar(
            shapeRenderer,
            ATTACK_BAR_X,
            ATTACK_BAR_Y,
            ATTACK_BAR_WIDTH,
            ATTACK_BAR_HEIGHT,
            progress,
            progress >= 1f ? ArenaUi.GREEN : ArenaUi.GOLD
        );
        shapeRenderer.end();
    }

    private String buildAttackCooldownText() {
        float progress = player.getAttackReadyProgress();
        if (progress >= 1f) {
            return "Attack: READY";
        }

        return "Attack: " + (int) (progress * 100f) + "%";
    }

    private String buildDashCooldownText() {
        float progress = player.getDashReadyProgress();
        if (player.isDashing()) {
            return "Dash: MOVING";
        }
        if (progress >= 1f) {
            return "Dash: READY";
        }

        return "Dash: " + (int) (progress * 100f) + "%";
    }

    private void drawComboText() {
        if (comboCount < 2 || comboTimer <= 0f) {
            return;
        }

        float y = bossPhaseMessageTimer > 0f ? 352f : 386f;
        float scale = comboCount >= 5 ? 1.18f : 1.02f;
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "x" + comboCount + " COMBO", ARENA_WIDTH / 2f, y, scale, ArenaUi.PALE_GOLD);
    }

    private void drawBossPhaseMessage() {
        if (bossPhaseMessageTimer <= 0f || bossPhaseMessage.isEmpty()) {
            return;
        }

        ArenaUi.drawCentered(game.getFont(), game.getBatch(), bossPhaseMessage, ARENA_WIDTH / 2f, 386f, 1.08f, ArenaUi.RED);
    }

    private String buildReviveStatus() {
        if (player.getCoins() >= player.getReviveCost()) {
            return "REVIVE READY";
        }
        return "REVIVE LOCKED";
    }

    private Enemy createAtRandomEdge(EnemyFactory factory) {
        int edge = MathUtils.random(3);
        Enemy enemy;
        if (edge == 0) {
            enemy = factory.create(MathUtils.random(0f, ARENA_WIDTH - DEFAULT_ENEMY_WIDTH), ARENA_HEIGHT - DEFAULT_ENEMY_HEIGHT);
        } else if (edge == 1) {
            enemy = factory.create(MathUtils.random(0f, ARENA_WIDTH - DEFAULT_ENEMY_WIDTH), 0f);
        } else if (edge == 2) {
            enemy = factory.create(0f, MathUtils.random(0f, ARENA_HEIGHT - DEFAULT_ENEMY_HEIGHT));
        } else {
            enemy = factory.create(ARENA_WIDTH - DEFAULT_ENEMY_WIDTH, MathUtils.random(0f, ARENA_HEIGHT - DEFAULT_ENEMY_HEIGHT));
        }
        applyLevelScaling(enemy);
        return enemy;
    }

    private Boss createBossAtEdgeCenter() {
        Enemy enemy = bossFactory.create(ARENA_WIDTH - Boss.SPRITE_WIDTH, (ARENA_HEIGHT - Boss.SPRITE_HEIGHT) / 2f);
        applyLevelScaling(enemy);
        if (enemy instanceof Boss) {
            return (Boss) enemy;
        }

        throw new IllegalStateException("BossFactory must create a Boss instance.");
    }

    private Enemy createMiniBossAtEdgeCenter() {
        Enemy enemy = miniBossFactory.create(ARENA_WIDTH - 72f, (ARENA_HEIGHT - 72f) / 2f);
        applyLevelScaling(enemy);
        return enemy;
    }

    private void applyLevelScaling(Enemy enemy) {
        if (enemy == null || roomNumber <= 1) {
            return;
        }

        float levelSteps = roomNumber - 1f;
        enemy.setHp(enemy.getMaxHp() * (1f + 0.25f * levelSteps));
        enemy.applyDifficulty(1f + 0.08f * levelSteps, 1f + 0.20f * levelSteps);
    }

    private void handleWaveCleared(GameEvent event) {
        if (transitioning) {
            return;
        }

        LevelManager.WaveSummary summary = null;
        if (event.getPayload() instanceof LevelManager.WaveSummary) {
            summary = (LevelManager.WaveSummary) event.getPayload();
        }

        if (summary != null && summary.getWaveNumber() >= WAVES_PER_ROOM) {
            if (isFinalRoom()) {
                startScreenTransition(PendingTransition.VICTORY);
                return;
            }
            createPortal();
            return;
        }

        startScreenTransition(PendingTransition.UPGRADE);
    }

    private void handlePlayerDied(GameEvent event) {
        if (transitioning) {
            return;
        }

        startScreenTransition(PendingTransition.GAME_OVER);
    }

    private void resolvePlayerDeath() {
        if (transitioning || revivePromptActive || playerDeathPosted || player.getHp() > 0f) {
            return;
        }

        revivePromptActive = true;
        revivePromptTimer = 0f;
        playerDeathPosted = true;
    }

    private void handleRevivePromptInput() {
        if (player.canAffordRevive() && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            tryRevivePlayer();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            revivePromptActive = false;
            startScreenTransition(PendingTransition.GAME_OVER);
        }
    }

    private boolean tryRevivePlayer() {
        float previousHp = player.getHp();
        if (!player.tryReviveAt(lastSafeX, lastSafeY, REVIVE_HP_PERCENT)) {
            return false;
        }

        int healed = MathUtils.round(player.getHp() - previousHp);
        damageNumberManager.addHealText(player.getCenterX(), player.getY() + REVIVE_HEAL_TEXT_OFFSET, healed);
        revivePromptActive = false;
        playerDeathPosted = false;
        updateLastSafePosition();
        return true;
    }

    private void updateLastSafePosition() {
        if (player.getHp() <= 0f) {
            return;
        }

        lastSafeX = player.getX();
        lastSafeY = player.getY();
    }

    private void createPortal() {
        activeBoss = null;
        enemiesRemainingToSpawn = 0;
        pendingSpawnFactories.clear();
        portal = new Portal(ARENA_WIDTH - 112f, (ARENA_HEIGHT - 82f) / 2f);
        grantLevelClearBonus();
        bossPhaseMessage = "Level Cleared! Entering Level " + Math.min(roomNumber + 1, FINAL_LEVEL) + "...";
        bossPhaseMessageTimer = BOSS_MESSAGE_DURATION;
    }

    private void grantLevelClearBonus() {
        int bonusCoins = 4 + roomNumber * 2;
        player.addCoins(bonusCoins);
        gameManager.recordCoinsCollected(bonusCoins);
        damageNumberManager.addText(
            "LEVEL BONUS +" + bonusCoins + " COINS",
            ARENA_WIDTH / 2f,
            238f,
            ArenaUi.PALE_GOLD
        );

        float healed = player.heal(LEVEL_CLEAR_HEAL_AMOUNT);
        if (healed > 0f) {
            damageNumberManager.addHealText(player.getCenterX(), player.getY() + REVIVE_HEAL_TEXT_OFFSET, MathUtils.round(healed));
        }
    }

    private void handleBossDied(GameEvent event) {
        if (transitioning) {
            return;
        }

        if (isFinalRoom()) {
            startScreenTransition(PendingTransition.VICTORY);
            return;
        }

        createPortal();
    }

    private void handleEnemyDamaged(GameEvent event) {
        if (!(event.getPayload() instanceof EnemyDamagedEvent)) {
            return;
        }

        EnemyDamagedEvent damaged = (EnemyDamagedEvent) event.getPayload();
        hitEffects.add(new HitEffect(damaged.getX(), damaged.getY() - 10f));
        if (damaged.isBoss() && (damaged.isCritical() || damaged.getAmount() >= STRONG_DAMAGE_SHAKE_THRESHOLD)) {
            startScreenShake(SCREEN_SHAKE_STRENGTH * 0.75f, SCREEN_SHAKE_DURATION);
        }
    }

    private void handlePlayerHurt(GameEvent event) {
        float amount = 0f;
        if (event.getPayload() instanceof Number) {
            amount = ((Number) event.getPayload()).floatValue();
        }
        if (amount >= STRONG_DAMAGE_SHAKE_THRESHOLD) {
            startScreenShake(SCREEN_SHAKE_STRENGTH, SCREEN_SHAKE_DURATION);
        }
    }

    private void handleBossPhaseChanged(GameEvent event) {
        if (!(event.getPayload() instanceof BossPhaseEvent)) {
            return;
        }

        BossPhaseEvent phaseEvent = (BossPhaseEvent) event.getPayload();
        bossPhaseMessage = phaseEvent.getMessage();
        bossPhaseMessageTimer = BOSS_MESSAGE_DURATION;
        startScreenShake(SCREEN_SHAKE_STRENGTH, SCREEN_SHAKE_DURATION + 0.08f);
    }

    @Override
    public void hide() {
        SoundManager.getInstance().pauseGameplayMusic();
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        eventBus.unsubscribe(GameEvent.Type.WAVE_CLEARED, waveClearedListener);
        eventBus.unsubscribe(GameEvent.Type.PLAYER_DIED, playerDiedListener);
        eventBus.unsubscribe(GameEvent.Type.BOSS_DIED, bossDiedListener);
        eventBus.unsubscribe(GameEvent.Type.ENEMY_DAMAGED, enemyDamagedListener);
        eventBus.unsubscribe(GameEvent.Type.PLAYER_HURT, playerHurtListener);
        eventBus.unsubscribe(GameEvent.Type.BOSS_PHASE_CHANGED, bossPhaseListener);
        levelManager.dispose();
        damageNumberManager.dispose();
        shapeRenderer.dispose();
    }

    private static final class HitEffect {
        private final float x;
        private final float y;
        private float timer = HIT_EFFECT_DURATION;

        private HitEffect(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private void update(float delta) {
            timer -= delta;
        }

        private boolean isDone() {
            return timer <= 0f;
        }

        private float getAlpha() {
            return MathUtils.clamp(timer / HIT_EFFECT_DURATION, 0f, 1f);
        }

        private float getProgress() {
            return 1f - getAlpha();
        }
    }
}
