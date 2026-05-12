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
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;
import com.gladiator.arena.managers.LevelManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GameScreen extends ScreenAdapter {
    private static final int FINAL_WAVE = 10;
    private static final float ARENA_WIDTH = 800f;
    private static final float ARENA_HEIGHT = 480f;
    private static final float DEFAULT_ENEMY_WIDTH = 32f;
    private static final float DEFAULT_ENEMY_HEIGHT = 32f;
    private static final float PROGRESS_BAR_X = 16f;
    private static final float PROGRESS_BAR_Y = 426f;
    private static final float PROGRESS_BAR_WIDTH = 270f;
    private static final float PROGRESS_BAR_HEIGHT = 12f;

    private final GladiatorGame game;
    private final GameManager gameManager;
    private final EventBus eventBus;
    private final LevelManager levelManager;
    private final AssetManager assets;
    private final EventListener waveClearedListener;
    private final EventListener playerDiedListener;
    private final EventListener bossDiedListener;
    private final Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<EnemyFactory> pendingSpawnFactories = new ArrayList<>();
    private final EnemyFactory slimeFactory = new SlimeFactory();
    private final EnemyFactory goblinFactory = new GoblinFactory();
    private final EnemyFactory bossFactory = new BossFactory();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Boss activeBoss;
    private int score;
    private int enemiesRemainingToSpawn;
    private float spawnTimer;
    private boolean disposed;
    private boolean transitioning;
    private boolean playerDeathPosted;

    public GameScreen(GladiatorGame game) {
        this(game, new Player(), 1, 0);
    }

    public GameScreen(GladiatorGame game, Player player, int waveNumber, int score) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
        this.eventBus = EventBus.getInstance();
        this.levelManager = new LevelManager(eventBus);
        this.assets = AssetManager.getInstance();
        this.waveClearedListener = this::handleWaveCleared;
        this.playerDiedListener = this::handlePlayerDied;
        this.bossDiedListener = this::handleBossDied;
        this.player = player == null ? new Player() : player;
        this.score = score;

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

        updateSpawning(delta);
        updateEnemies(delta);
        if (transitioning) {
            return;
        }

        player.update(delta, enemies);
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

        ScreenUtils.clear(0.08f, 0.07f, 0.06f, 1f);
        game.getBatch().begin();
        assets.drawTiledFloor(game.getBatch(), ARENA_WIDTH, ARENA_HEIGHT);
        for (Enemy enemy : enemies) {
            enemy.render(game.getBatch(), assets);
        }
        player.render(game.getBatch(), assets);
        game.getBatch().end();

        drawAttackEffect();
        drawWaveProgressBar();

        game.getBatch().begin();
        float hudX = 16f;
        float hudY = 464f;
        String hud = "HP: " + (int) player.getHp() + "/" + (int) player.getMaxHp()
            + " | Wave: " + levelManager.getCurrentWave()
            + " | Score: " + score
            + " | Difficulty: " + gameManager.getDifficultyName()
            + " | ESC Pause";
        game.getFont().draw(game.getBatch(), hud, hudX, hudY);
        if (activeBoss != null && !activeBoss.isDead()) {
            float bossHpPercent = MathUtils.clamp(activeBoss.getHp() / activeBoss.getMaxHp(), 0f, 1f);
            game.getFont().draw(game.getBatch(), "Boss HP: " + (int) activeBoss.getHp()
                + "/" + (int) activeBoss.getMaxHp()
                + " (" + (int) (bossHpPercent * 100f) + "%)", 300f, 28f);
        }
        game.getFont().draw(game.getBatch(), buildWaveProgressText(), PROGRESS_BAR_X, PROGRESS_BAR_Y - 8f);
        game.getBatch().end();
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
        shapeRenderer.setColor(0.02f, 0.02f, 0.02f, 0.95f);
        shapeRenderer.rect(PROGRESS_BAR_X - 2f, PROGRESS_BAR_Y - 2f, PROGRESS_BAR_WIDTH + 4f, PROGRESS_BAR_HEIGHT + 4f);
        shapeRenderer.setColor(0.19f, 0.16f, 0.12f, 1f);
        shapeRenderer.rect(PROGRESS_BAR_X, PROGRESS_BAR_Y, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        shapeRenderer.setColor(0.9f, 0.55f, 0.18f, 1f);
        shapeRenderer.rect(PROGRESS_BAR_X, PROGRESS_BAR_Y, PROGRESS_BAR_WIDTH * progress, PROGRESS_BAR_HEIGHT);
        shapeRenderer.setColor(1f, 0.86f, 0.38f, 1f);
        shapeRenderer.rect(PROGRESS_BAR_X, PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT - 3f, PROGRESS_BAR_WIDTH * progress, 3f);
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

            score += enemy.getScoreReward();
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
        if (transitioning) {
            return;
        }

        transitioning = true;
        LevelManager.WaveSummary summary = null;
        if (event.getPayload() instanceof LevelManager.WaveSummary) {
            summary = (LevelManager.WaveSummary) event.getPayload();
        }

        if (summary != null && summary.getWaveNumber() >= FINAL_WAVE) {
            game.setScreen(new VictoryScreen(game));
            dispose();
            return;
        }

        game.setScreen(new UpgradeScreen(game, player, summary, score));
        dispose();
    }

    private void handlePlayerDied(GameEvent event) {
        if (transitioning) {
            return;
        }

        transitioning = true;
        game.setScreen(new GameOverScreen(game));
        dispose();
    }

    private void handleBossDied(GameEvent event) {
        if (transitioning) {
            return;
        }

        transitioning = true;
        game.setScreen(new VictoryScreen(game));
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
        shapeRenderer.dispose();
    }
}
