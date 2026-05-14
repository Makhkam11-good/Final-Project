package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.managers.GameManager;

public class PauseScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final GameScreen returnScreen;
    private final Rectangle panel = new Rectangle(238f, 138f, 324f, 214f);
    private final Rectangle resumeButton = new Rectangle(280f, 232f, 240f, 46f);
    private final Rectangle menuButton = new Rectangle(280f, 174f, 240f, 46f);
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private boolean disposed;
    private boolean transitioning;

    public PauseScreen(GladiatorGame game, GameScreen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
    }

    @Override
    public void render(float delta) {
        handleInput();
        if (disposed || transitioning) {
            return;
        }

        ScreenUtils.clear(0.035f, 0.03f, 0.04f, 1f);

        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        ArenaUi.drawArenaBackdrop(shapeRenderer);

        Vector2 mouse = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ArenaUi.drawPanel(shapeRenderer, panel, ArenaUi.INK, ArenaUi.GOLD);
        ArenaUi.drawButton(shapeRenderer, resumeButton, ArenaUi.GREEN, resumeButton.contains(mouse));
        ArenaUi.drawButton(shapeRenderer, menuButton, ArenaUi.RED, menuButton.contains(mouse));
        shapeRenderer.end();

        game.getBatch().begin();
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "PAUSED", 400f, 324f, 1.8f, ArenaUi.PALE_GOLD);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "RESUME", 400f, 262f, 1.1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "MAIN MENU", 400f, 204f, 1.1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "R - RESUME     M - MENU", 400f, 156f, 0.8f, ArenaUi.GOLD);
        game.getBatch().end();
    }

    private void handleInput() {
        if (transitioning) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resumeGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            resumeGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            returnToMenu();
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        Vector2 touch = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        if (resumeButton.contains(touch.x, touch.y)) {
            resumeGame();
        } else if (menuButton.contains(touch.x, touch.y)) {
            returnToMenu();
        }
    }

    private void resumeGame() {
        transitioning = true;
        GameManager.getInstance().getGameStateManager().pop();
        game.setScreen(returnScreen);
        dispose();
    }

    private void returnToMenu() {
        transitioning = true;
        returnScreen.dispose();
        game.setScreen(new MenuScreen(game));
        dispose();
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
