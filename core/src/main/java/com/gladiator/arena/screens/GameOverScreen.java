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

public class GameOverScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final int waveReached;
    private final int score;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Rectangle panel = new Rectangle(206f, 132f, 388f, 232f);
    private final Rectangle retryButton = new Rectangle(250f, 210f, 140f, 46f);
    private final Rectangle menuButton = new Rectangle(410f, 210f, 140f, 46f);
    private boolean disposed;
    private boolean transitioning;

    public GameOverScreen(GladiatorGame game) {
        this(game, 1, 0);
    }

    public GameOverScreen(GladiatorGame game, int waveReached, int score) {
        this.game = game;
        this.waveReached = waveReached;
        this.score = score;
    }

    @Override
    public void show() {
        GameManager.getInstance().getGameStateManager().set(GameStateManager.State.GAME_OVER);
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
        ArenaUi.drawPanel(shapeRenderer, panel, ArenaUi.INK, ArenaUi.RED);
        ArenaUi.drawButton(shapeRenderer, retryButton, ArenaUi.GREEN, retryButton.contains(mouse));
        ArenaUi.drawButton(shapeRenderer, menuButton, ArenaUi.BLUE, menuButton.contains(mouse));
        shapeRenderer.end();

        game.getBatch().begin();
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "DEFEATED", 400f, 330f, 2f, ArenaUi.PALE_GOLD);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "WAVE " + waveReached + "     SCORE " + score, 400f, 286f, 1f, ArenaUi.GOLD);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "RETRY", retryButton.x + retryButton.width / 2f, 240f, 1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "MENU", menuButton.x + menuButton.width / 2f, 240f, 1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "R - RETRY     M - MENU", 400f, 170f, 0.82f, ArenaUi.GOLD);
        game.getBatch().end();
    }

    private void handleInput() {
        if (transitioning) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            retry();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            openMenu();
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        Vector2 touch = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        if (retryButton.contains(touch)) {
            retry();
        } else if (menuButton.contains(touch)) {
            openMenu();
        }
    }

    private void retry() {
        transitioning = true;
        GameManager.getInstance().getGameStateManager().set(GameStateManager.State.GAME);
        game.setScreen(new GameScreen(game));
        dispose();
    }

    private void openMenu() {
        transitioning = true;
        GameManager.getInstance().getGameStateManager().set(GameStateManager.State.MENU);
        game.setScreen(new MenuScreen(game));
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
