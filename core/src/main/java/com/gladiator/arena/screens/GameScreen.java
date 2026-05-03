package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;

public class GameScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final GameManager gameManager;

    public GameScreen(GladiatorGame game) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
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

        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);
        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "GAME SCREEN", 340f, 360f);
        game.getFont().draw(game.getBatch(), "Difficulty: " + gameManager.getDifficultyName(), 320f, 320f);
        game.getFont().draw(game.getBatch(), "Press ESC to pause", 320f, 280f);
        game.getBatch().end();
    }
}
