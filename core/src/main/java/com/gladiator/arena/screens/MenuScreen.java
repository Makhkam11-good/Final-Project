package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;
import com.gladiator.arena.strategy.EasyDifficulty;
import com.gladiator.arena.strategy.HardDifficulty;
import com.gladiator.arena.strategy.MediumDifficulty;

public class MenuScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final GameManager gameManager;

    public MenuScreen(GladiatorGame game) {
        this.game = game;
        this.gameManager = GameManager.getInstance();
    }

    @Override
    public void show() {
        gameManager.getGameStateManager().set(GameStateManager.State.MENU);
    }

    @Override
    public void render(float delta) {
        handleInput();
        ScreenUtils.clear(0.08f, 0.12f, 0.22f, 1f);

        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "GLADIATOR ARENA", 300f, 360f);
        game.getFont().draw(game.getBatch(), "Press 1 - Easy", 320f, 300f);
        game.getFont().draw(game.getBatch(), "Press 2 - Medium", 320f, 270f);
        game.getFont().draw(game.getBatch(), "Press 3 - Hard", 320f, 240f);
        game.getBatch().end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            startGameWithEasy();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
            startGameWithMedium();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
            startGameWithHard();
        }
    }

    private void startGameWithEasy() {
        gameManager.setDifficulty(new EasyDifficulty());
        gameManager.getGameStateManager().set(GameStateManager.State.GAME);
        game.setScreen(new GameScreen(game));
    }

    private void startGameWithMedium() {
        gameManager.setDifficulty(new MediumDifficulty());
        gameManager.getGameStateManager().set(GameStateManager.State.GAME);
        game.setScreen(new GameScreen(game));
    }

    private void startGameWithHard() {
        gameManager.setDifficulty(new HardDifficulty());
        gameManager.getGameStateManager().set(GameStateManager.State.GAME);
        game.setScreen(new GameScreen(game));
    }
}
