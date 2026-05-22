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
import com.gladiator.arena.managers.GameStateManager;
import com.gladiator.arena.strategy.EasyDifficulty;
import com.gladiator.arena.strategy.HardDifficulty;
import com.gladiator.arena.strategy.MediumDifficulty;

public class MenuScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final GameManager gameManager;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Rectangle trialPanel = new Rectangle(220f, 258f, 360f, 34f);
    private final Rectangle easyButton = new Rectangle(260f, 176f, 280f, 42f);
    private final Rectangle mediumButton = new Rectangle(260f, 122f, 280f, 42f);
    private final Rectangle hardButton = new Rectangle(260f, 68f, 280f, 42f);
    private boolean disposed;
    private boolean transitioning;

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
        if (disposed || transitioning) {
            return;
        }

        ScreenUtils.clear(0.035f, 0.03f, 0.04f, 1f);
        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        ArenaUi.drawMenuBackdrop(shapeRenderer);

        Vector2 mouse = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ArenaUi.drawTitleSwords(shapeRenderer, 400f, 402f);
        ArenaUi.drawThinPanel(shapeRenderer, trialPanel.x, trialPanel.y, trialPanel.width, trialPanel.height, ArenaUi.INK, ArenaUi.GOLD);
        ArenaUi.drawButton(shapeRenderer, easyButton, ArenaUi.GREEN, easyButton.contains(mouse));
        ArenaUi.drawButton(shapeRenderer, mediumButton, ArenaUi.BLUE, mediumButton.contains(mouse));
        ArenaUi.drawButton(shapeRenderer, hardButton, ArenaUi.RED, hardButton.contains(mouse));
        shapeRenderer.end();

        game.getBatch().begin();
        ArenaUi.drawTitle(game.getBatch(), game.getFont(), 438f);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "CHOOSE YOUR TRIAL", trialPanel.x + trialPanel.width / 2f, 282f, 1f, ArenaUi.PALE_GOLD);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "PRESS 1 - EASY", easyButton.x + easyButton.width / 2f, 202f, 1.15f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "PRESS 2 - MEDIUM", mediumButton.x + mediumButton.width / 2f, 148f, 1.15f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "PRESS 3 - HARD", hardButton.x + hardButton.width / 2f, 94f, 1.15f, ArenaUi.BONE);
        game.getBatch().end();
    }

    private void handleInput() {
        if (transitioning) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            startGameWithEasy();
            return;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
            startGameWithMedium();
            return;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
            startGameWithHard();
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        Vector2 touch = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        if (easyButton.contains(touch)) {
            startGameWithEasy();
        } else if (mediumButton.contains(touch)) {
            startGameWithMedium();
        } else if (hardButton.contains(touch)) {
            startGameWithHard();
        }
    }

    private void startGameWithEasy() {
        transitioning = true;
        gameManager.setDifficulty(new EasyDifficulty());
        gameManager.getGameStateManager().set(GameStateManager.State.GAME);
        game.setScreen(new GameScreen(game));
        dispose();
    }

    private void startGameWithMedium() {
        transitioning = true;
        gameManager.setDifficulty(new MediumDifficulty());
        gameManager.getGameStateManager().set(GameStateManager.State.GAME);
        game.setScreen(new GameScreen(game));
        dispose();
    }

    private void startGameWithHard() {
        transitioning = true;
        gameManager.setDifficulty(new HardDifficulty());
        gameManager.getGameStateManager().set(GameStateManager.State.GAME);
        game.setScreen(new GameScreen(game));
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
