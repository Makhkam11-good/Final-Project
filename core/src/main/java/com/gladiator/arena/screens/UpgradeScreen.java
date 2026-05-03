package com.gladiator.arena.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;

public class UpgradeScreen extends ScreenAdapter {
    private final GladiatorGame game;

    public UpgradeScreen(GladiatorGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        GameManager.getInstance().getGameStateManager().set(GameStateManager.State.UPGRADE);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.07f, 0.2f, 0.1f, 1f);
        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "UPGRADE SCREEN", 325f, 260f);
        game.getBatch().end();
    }
}
