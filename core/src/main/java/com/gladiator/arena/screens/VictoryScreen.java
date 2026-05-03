package com.gladiator.arena.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;

public class VictoryScreen extends ScreenAdapter {
    private final GladiatorGame game;

    public VictoryScreen(GladiatorGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        GameManager.getInstance().getGameStateManager().set(GameStateManager.State.VICTORY);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.35f, 0.28f, 0.05f, 1f);
        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "VICTORY SCREEN", 330f, 260f);
        game.getBatch().end();
    }
}
