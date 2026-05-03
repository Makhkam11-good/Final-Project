package com.gladiator.arena.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;

public class GameOverScreen extends ScreenAdapter {
    private final GladiatorGame game;

    public GameOverScreen(GladiatorGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        GameManager.getInstance().getGameStateManager().set(GameStateManager.State.GAME_OVER);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.3f, 0.05f, 0.05f, 1f);
        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "GAME OVER SCREEN", 315f, 260f);
        game.getBatch().end();
    }
}
