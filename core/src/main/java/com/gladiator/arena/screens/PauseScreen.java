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
import com.gladiator.arena.managers.SoundManager;

public class PauseScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final GameScreen returnScreen;
    private final Rectangle panel = new Rectangle(230f, 84f, 340f, 316f);
    private final Rectangle resumeButton = new Rectangle(280f, 300f, 240f, 42f);
    private final Rectangle musicButton = new Rectangle(280f, 246f, 240f, 42f);
    private final Rectangle restartButton = new Rectangle(280f, 192f, 240f, 42f);
    private final Rectangle exitButton = new Rectangle(280f, 138f, 240f, 42f);
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
        ArenaUi.drawButton(shapeRenderer, musicButton, ArenaUi.BLUE, musicButton.contains(mouse));
        ArenaUi.drawButton(shapeRenderer, restartButton, ArenaUi.GOLD, restartButton.contains(mouse));
        ArenaUi.drawButton(shapeRenderer, exitButton, ArenaUi.RED, exitButton.contains(mouse));
        shapeRenderer.end();

        game.getBatch().begin();
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "PAUSED", 400f, 374f, 1.8f, ArenaUi.PALE_GOLD);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "RESUME", 400f, 326f, 1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), SoundManager.getInstance().getMusicStatusText(), 400f, 272f, 1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "RESTART", 400f, 218f, 1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "EXIT", 400f, 164f, 1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "R RESUME     M MUSIC     T RESTART     Q EXIT", 400f, 108f, 0.70f, ArenaUi.GOLD);
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
            SoundManager.getInstance().toggleMusic(false);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            restartGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            exitToMenu();
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        Vector2 touch = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        if (resumeButton.contains(touch.x, touch.y)) {
            resumeGame();
        } else if (musicButton.contains(touch.x, touch.y)) {
            SoundManager.getInstance().toggleMusic(false);
        } else if (restartButton.contains(touch.x, touch.y)) {
            restartGame();
        } else if (exitButton.contains(touch.x, touch.y)) {
            exitToMenu();
        }
    }

    private void resumeGame() {
        transitioning = true;
        GameManager.getInstance().getGameStateManager().pop();
        game.setScreen(returnScreen);
        dispose();
    }

    private void restartGame() {
        transitioning = true;
        returnScreen.dispose();
        GameManager.getInstance().getGameStateManager().set(com.gladiator.arena.managers.GameStateManager.State.GAME);
        game.setScreen(new GameScreen(game));
        dispose();
    }

    private void exitToMenu() {
        transitioning = true;
        SoundManager.getInstance().pauseGameplayMusic();
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
