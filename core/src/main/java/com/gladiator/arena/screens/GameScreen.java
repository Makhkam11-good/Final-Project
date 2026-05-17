package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.entities.Boss;
import com.gladiator.arena.entities.Coin;
import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.Player;
import com.gladiator.arena.events.EventBus;
import com.gladiator.arena.events.EventListener;
import com.gladiator.arena.events.GameEvent;
import com.gladiator.arena.factories.BossFactory;
import com.gladiator.arena.factories.EnemyFactory;
import com.gladiator.arena.factories.GoblinFactory;
import com.gladiator.arena.factories.SlimeFactory;
import com.gladiator.arena.managers.AssetManager;
import com.gladiator.arena.managers.DamageNumberManager;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;
import com.gladiator.arena.managers.LevelManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GameScreen extends ScreenAdapter {
    private static final int FINAL_WAVE = 10;
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
    private static final float WAVE_COMPLETE_DELAY = 1.0f;
    private static final float WAVE_COMPLETE_FADE_ALPHA = 0.6f;

    private final GladiatorGame game;
    private final GameManager gameManager;
    private final EventBus eventBus;
    private final LevelManager levelManager;
    private final DamageNumberManager damageNumberManager;
    private final AssetManager assets;
    private final EventListener waveClearedListener;
    private final EventListener playerDiedListener;
    private final EventListener bossDiedListener;
    private final Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private final List<EnemyFactory> pendingSpawnFactories = new ArrayList<>();
    private final EnemyFactory slimeFactory = new SlimeFactory();
    private final EnemyFactory goblinFactory = new GoblinFactory();
    private final EnemyFactory bossFactory = new BossFactory();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Boss activeBoss;
    private LevelManager.WaveSummary pendingWaveSummary;
    private int score;
    private int coinCount;
    private int enemiesRemainingToSpawn;
    private float spawnTimer;
    private float waveCompleteTimer;
    private float transitionAlpha;
    private boolean disposed;
    private boolean transitioning;
    private boolean playerDeathPosted;
    private boolean waveCompletePending;

    public GameScreen(GladiatorGame game) {
        this(game, new Player(), 1, 0);
    }

    public GameScreen(GladiatorGame game, Player player, int waveNumber, int score) {
        this(game, player, waveNumber, score, 0);
    }

    public GameScreen(GladiatorGame game, Player player, int waveNumber, int score, int coinCount) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
        this.eventBus = EventBus.getInstance();
        this.levelManager = new LevelManager(eventBus);
        this.damageNumberManager = new DamageNumberManager(eventBus);
        this.assets = AssetManager.getInstance();
        this.waveClearedListener = this::handleWaveCleared;
        this.playerDiedListener = this::handlePlayerDied;
        this.bossDiedListener = this::handleBossDied;
        this.player = player == null ? new Player() : player;
        this.score = score;
        this.coinCount = Math.max(0, coinCount);

        eventBus.subscribe(GameEvent.Type.WAVE_CLEARED, waveClearedListener);
        eventBus.subscribe(GameEvent.Type.PLAYER_DIED, playerDiedListener);
        eventBus.subscribe(GameEvent.Type.BOSS_DIED, bossDiedListener);
        int safeWaveNumber = Math.max(1, Math.min(waveNumber, FINAL_WAVE));
        int enemiesInWave = prepareWaveSpawns(safeWaveNumber);
        levelManager.startWave(safeWaveNumber, enemiesInWave);
    }

    @Override
    public void show() {
        gameManager.getGameStateManager().set(GameStateManager.State.GAME);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameManager.getGameStateManager().push(GameStateManager.State.PAUSE);
            game.setScreen(new PauseScreen(game, this));
            return;
        }

        if (waveCompletePending) {
            updateWaveCompleteTransition(delta);
            if (transitioning) {
                return;
            }
            damageNumberManager.update(delta);
            player.update(delta, enemies);
            updateCoins(delta);
            if (transitioning) {
                return;
            }
        } else {
            updateSpawning(delta);
            updateEnemies(delta);
            damageNumberManager.update(delta);
            if (transitioning) {
                return;
            }

            player.update(delta, enemies);
            updateCoins(delta);
            removeDeadEnemies();
            if (transitioning) {
                return;
            }

            if (player.getHp() <= 0f && !playerDeathPosted) {
                playerDeathPosted = true;
                eventBus.post(GameEvent.Type.PLAYER_DIED);
                if (transitioning) {
                    return;
                }
            }
        }

        ScreenUtils.clear(0.08f, 0.07f, 0.06f, 1f);
        game.getBatch().begin();
        assets.drawTiledFloor(game.getBatch(), ARENA_WIDTH, ARENA_HEIGHT);
        for (Enemy enemy : enemies) {
            enemy.render(game.getBatch(), assets);
        }
        player.render(game.getBatch(), assets);
        game.getBatch().end();

        drawBossDashTelegraph();
        drawCoins();
        drawHudPanel();
        drawCharacterHealthBars();
        drawAttackEffect();
        drawWaveProgressBar();
        drawAttackCooldownBar();

        game.getBatch().begin();
        damageNumberManager.render(game.getBatch(), game.getFont());
        float hudX = 20f;
        float hudY = 466f;
        String hud = "HP " + (int) player.getHp() + "/" + (int) player.getMaxHp()
            + "   WAVE " + levelManager.getCurrentWave()
            + "   SCORE " + score
            + "   COINS: " + coinCount
            + "   " + gameManager.getDifficultyName().toUpperCase(Locale.ROOT)
            + "   SPACE ATK"
            + "   ESC PAUSE";
        ArenaUi.drawText(game.getFont(), game.getBatch(), hud, hudX, hudY, 0.74f, ArenaUi.BONE);
        if (activeBoss != null && !activeBoss.isDead()) {
            float bossHpPercent = MathUtils.clamp(activeBoss.getHp() / activeBoss.getMaxHp(), 0f, 1f);
            ArenaUi.drawCentered(game.getFont(), game.getBatch(), "BOSS HP: " + (int) activeBoss.getHp()
                + "/" + (int) activeBoss.getMaxHp()
                + " (" + (int) (bossHpPercent * 100f) + "%)", 400f, 28f, 0.86f, ArenaUi.PALE_GOLD);
        }
        ArenaUi.drawText(game.getFont(), game.getBatch(), buildWaveProgressText(), PROGRESS_BAR_X, PROGRESS_BAR_Y - 8f, 0.78f, ArenaUi.GOLD);
        ArenaUi.drawText(game.getFont(), game.getBatch(), buildAttackCooldownText(), ATTACK_BAR_X, ATTACK_BAR_Y - 8f, 0.78f, ArenaUi.GOLD);
        game.getBatch().end();

        drawWaveCompleteTransition();
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
        if (activeBoss != null && activeBoss.getMaxHp() > 0f) {
            return MathUtils.clamp(1f - activeBoss.getHp() / activeBoss.getMaxHp(), 0f, 1f);
        }

        return MathUtils.clamp(levelManager.getWaveProgress(), 0f, 1f);
    }

    private String buildWaveProgressText() {
        if (activeBoss != null && activeBoss.getMaxHp() > 0f) {
            return "Wave Progress: Boss " + (int) (getWaveProgress() * 100f) + "%";
        }

        int totalEnemies = levelManager.getEnemiesTotalThisWave();
        return "Wave Progress: " + levelManager.getEnemiesKilledThisWave() + "/" + totalEnemies;
    }

    private int prepareWaveSpawns(int waveNumber) {
        enemies.clear();
        pendingSpawnFactories.clear();
        activeBoss = null;

        if (waveNumber == 1) {
            addPendingEnemies(slimeFactory, 4);
        } else if (waveNumber == 2) {
            addPendingEnemies(slimeFactory, 6);
        } else if (waveNumber == 3) {
            addPendingEnemies(slimeFactory, 4);
            addPendingEnemies(goblinFactory, 2);
        } else if (waveNumber == 4) {
            addPendingEnemies(goblinFactory, 5);
            addPendingEnemies(slimeFactory, 3);
        } else if (waveNumber == 5) {
            addPendingEnemies(goblinFactory, 8);
        } else if (waveNumber == 6) {
            addPendingEnemies(goblinFactory, 6);
            addPendingEnemies(slimeFactory, 4);
        } else if (waveNumber == 7) {
            addPendingEnemies(goblinFactory, 10);
        } else if (waveNumber == 8) {
            addPendingEnemies(goblinFactory, 8);
            addPendingEnemies(slimeFactory, 5);
        } else if (waveNumber == 9) {
            addPendingEnemies(goblinFactory, 12);
        } else {
            activeBoss = createBossAtEdgeCenter();
            enemies.add(activeBoss);
            enemiesRemainingToSpawn = 0;
            spawnTimer = 0f;
            return enemies.size();
        }

        Collections.shuffle(pendingSpawnFactories);
        enemiesRemainingToSpawn = pendingSpawnFactories.size();
        spawnTimer = 0f;
        return enemiesRemainingToSpawn;
    }

    private void addPendingEnemies(EnemyFactory factory, int count) {
        for (int i = 0; i < count; i++) {
            pendingSpawnFactories.add(factory);
        }
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

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            enemy.update(delta, player);
            if (player.getHp() <= 0f && !playerDeathPosted) {
                playerDeathPosted = true;
                eventBus.post(GameEvent.Type.PLAYER_DIED);
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
            dropCoins(enemy);
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

    private void updateCoins(float delta) {
        Iterator<Coin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            Coin coin = iterator.next();
            coin.update(delta);
            if (player.getHp() > 0f && coin.overlaps(player)) {
                coin.collect();
                coinCount += coin.getValue();
            }
            if (coin.isCollected()) {
                iterator.remove();
            }
        }
    }

    private void drawCoins() {
        if (coins.isEmpty()) {
            return;
        }

        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Coin coin : coins) {
            coin.render(shapeRenderer);
        }
        shapeRenderer.end();
    }

    private void dropCoins(Enemy enemy) {
        coins.add(new Coin(enemy.getCenterX(), enemy.getCenterY(), getCoinDropValue(enemy)));
    }

    private int getCoinDropValue(Enemy enemy) {
        if (enemy instanceof Boss) {
            return 10;
        }
        if (enemy.getScoreReward() >= 25) {
            return 2;
        }
        return 1;
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

    private void updateWaveCompleteTransition(float delta) {
        waveCompleteTimer += delta;
        float progress = MathUtils.clamp(waveCompleteTimer / WAVE_COMPLETE_DELAY, 0f, 1f);
        transitionAlpha = WAVE_COMPLETE_FADE_ALPHA * progress;

        if (waveCompleteTimer >= WAVE_COMPLETE_DELAY) {
            openUpgradeScreen();
        }
    }

    private void drawWaveCompleteTransition() {
        if (!waveCompletePending) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, transitionAlpha);
        shapeRenderer.rect(0f, 0f, ARENA_WIDTH, ARENA_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.getBatch().begin();
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "WAVE CLEARED", ARENA_WIDTH / 2f, 268f, 1.45f, ArenaUi.PALE_GOLD);
        game.getBatch().end();
    }

    private void openUpgradeScreen() {
        transitioning = true;
        game.setScreen(new UpgradeScreen(game, player, pendingWaveSummary, score, coinCount));
        dispose();
    }

    private Enemy createAtRandomEdge(EnemyFactory factory) {
        int edge = MathUtils.random(3);
        if (edge == 0) {
            return factory.create(MathUtils.random(0f, ARENA_WIDTH - DEFAULT_ENEMY_WIDTH), ARENA_HEIGHT - DEFAULT_ENEMY_HEIGHT);
        }
        if (edge == 1) {
            return factory.create(MathUtils.random(0f, ARENA_WIDTH - DEFAULT_ENEMY_WIDTH), 0f);
        }
        if (edge == 2) {
            return factory.create(0f, MathUtils.random(0f, ARENA_HEIGHT - DEFAULT_ENEMY_HEIGHT));
        }
        return factory.create(ARENA_WIDTH - DEFAULT_ENEMY_WIDTH, MathUtils.random(0f, ARENA_HEIGHT - DEFAULT_ENEMY_HEIGHT));
    }

    private Boss createBossAtEdgeCenter() {
        Enemy enemy = bossFactory.create(ARENA_WIDTH - Boss.SPRITE_WIDTH, (ARENA_HEIGHT - Boss.SPRITE_HEIGHT) / 2f);
        if (enemy instanceof Boss) {
            return (Boss) enemy;
        }

        throw new IllegalStateException("BossFactory must create a Boss instance.");
    }

    private void handleWaveCleared(GameEvent event) {
        if (transitioning || waveCompletePending) {
            return;
        }

        LevelManager.WaveSummary summary = null;
        if (event.getPayload() instanceof LevelManager.WaveSummary) {
            summary = (LevelManager.WaveSummary) event.getPayload();
        }

        if (summary != null && summary.getWaveNumber() >= FINAL_WAVE) {
            transitioning = true;
            game.setScreen(new VictoryScreen(game, score));
            dispose();
            return;
        }

        pendingWaveSummary = summary;
        waveCompletePending = true;
        waveCompleteTimer = 0f;
        transitionAlpha = 0f;
    }

    private void handlePlayerDied(GameEvent event) {
        if (transitioning) {
            return;
        }

        transitioning = true;
        game.setScreen(new GameOverScreen(game, levelManager.getCurrentWave(), score));
        dispose();
    }

    private void handleBossDied(GameEvent event) {
        if (transitioning) {
            return;
        }

        transitioning = true;
        game.setScreen(new VictoryScreen(game, score));
        dispose();
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
        levelManager.dispose();
        damageNumberManager.dispose();
        shapeRenderer.dispose();
    }
}
