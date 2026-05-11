package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.Player;
import com.gladiator.arena.events.EventBus;
import com.gladiator.arena.events.EventListener;
import com.gladiator.arena.events.GameEvent;
import com.gladiator.arena.factories.EnemyFactory;
import com.gladiator.arena.factories.SlimeFactory;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;
import com.gladiator.arena.managers.LevelManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameScreen extends ScreenAdapter {
    private static final int PROTOTYPE_WAVE_ENEMY_COUNT = 4;
    private static final float ARENA_WIDTH = 800f;
    private static final float ARENA_HEIGHT = 480f;
    private static final float DEFAULT_ENEMY_WIDTH = 32f;
    private static final float DEFAULT_ENEMY_HEIGHT = 32f;

    private final GladiatorGame game;
    private final GameManager gameManager;
    private final EventBus eventBus;
    private final LevelManager levelManager;
    private final EventListener waveClearedListener;
    private final EventListener playerDiedListener;
    private final Player player;
    private final ShapeRenderer shapeRenderer;
    private final List<Enemy> enemies = new ArrayList<>();
    private final EnemyFactory slimeFactory = new SlimeFactory();
    private int score;
    private boolean disposed;
    private boolean transitioning;
    private boolean playerDeathPosted;

    public GameScreen(GladiatorGame game) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
        this.eventBus = EventBus.getInstance();
        this.levelManager = new LevelManager(eventBus);
        this.waveClearedListener = this::handleWaveCleared;
        this.playerDiedListener = this::handlePlayerDied;
        this.player = new Player();
        this.shapeRenderer = new ShapeRenderer();

        eventBus.subscribe(GameEvent.Type.WAVE_CLEARED, waveClearedListener);
        eventBus.subscribe(GameEvent.Type.PLAYER_DIED, playerDiedListener);
        levelManager.startWave(1, PROTOTYPE_WAVE_ENEMY_COUNT);
        spawnPrototypeWave();
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

        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : enemies) {
            enemy.render(shapeRenderer);
        }
        player.render(shapeRenderer);
        shapeRenderer.end();

        float hudX = 16f;
        float hudY = 464f;
        float lineHeight = 24f;
        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "Difficulty: " + gameManager.getDifficultyName(), hudX, hudY);
        game.getFont().draw(game.getBatch(), "Press ESC to pause", hudX, hudY - lineHeight);
        game.getFont().draw(game.getBatch(), "Player State: " + player.getCurrentState().getName(), hudX, hudY - (lineHeight * 2f));
        game.getFont().draw(game.getBatch(), "HP: " + (int) player.getHp() + "/" + (int) player.getMaxHp(), hudX, hudY - (lineHeight * 3f));
        game.getFont().draw(game.getBatch(), "Wave: " + levelManager.getCurrentWave(), hudX, hudY - (lineHeight * 4f));
        game.getFont().draw(game.getBatch(), "Enemies Alive: " + levelManager.getEnemiesAlive(), hudX, hudY - (lineHeight * 5f));
        game.getFont().draw(game.getBatch(), "Score: " + score, hudX, hudY - (lineHeight * 6f));
        game.getBatch().end();
    }

    private void spawnPrototypeWave() {
        enemies.clear();
        for (int i = 0; i < PROTOTYPE_WAVE_ENEMY_COUNT; i++) {
            enemies.add(createAtRandomEdge(slimeFactory));
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

    private void handleWaveCleared(GameEvent event) {
        if (transitioning) {
            return;
        }

        transitioning = true;
        game.setScreen(new UpgradeScreen(game));
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

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        eventBus.unsubscribe(GameEvent.Type.WAVE_CLEARED, waveClearedListener);
        eventBus.unsubscribe(GameEvent.Type.PLAYER_DIED, playerDiedListener);
        levelManager.dispose();
        shapeRenderer.dispose();
    }
}
