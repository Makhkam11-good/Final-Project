package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.entities.Player;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;

public class GameScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final GameManager gameManager;
    private final Player player;
    private final ShapeRenderer shapeRenderer;
    private boolean disposed;

    public GameScreen(GladiatorGame game) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
        this.player = new Player();
        this.shapeRenderer = new ShapeRenderer();
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

        player.update(delta);

        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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
        game.getBatch().end();
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        shapeRenderer.dispose();
    }
}
