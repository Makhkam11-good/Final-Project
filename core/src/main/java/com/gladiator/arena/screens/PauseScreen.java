package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.managers.GameManager;

public class PauseScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final GameScreen returnScreen;
    private final Rectangle resumeButton = new Rectangle(300f, 205f, 200f, 70f);
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private boolean disposed;

    public PauseScreen(GladiatorGame game, GameScreen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
    }

    @Override
    public void render(float delta) {
        handleInput();
        ScreenUtils.clear(0.18f, 0.1f, 0.22f, 1f);

        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.SKY);
        shapeRenderer.rect(resumeButton.x, resumeButton.y, resumeButton.width, resumeButton.height);
        shapeRenderer.end();

        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "PAUSED", 370f, 340f);
        game.getFont().draw(game.getBatch(), "RESUME", 362f, 248f);
        game.getFont().draw(game.getBatch(), "Click RESUME button", 330f, 170f);
        game.getBatch().end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resumeGame();
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        Vector2 touch = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        if (resumeButton.contains(touch.x, touch.y)) {
            resumeGame();
        }
    }

    private void resumeGame() {
        GameManager.getInstance().getGameStateManager().pop();
        game.setScreen(returnScreen);
    }

    @Override
    public void hide() {
        dispose();
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
