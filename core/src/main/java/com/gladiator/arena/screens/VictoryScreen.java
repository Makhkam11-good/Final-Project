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

public class VictoryScreen extends ScreenAdapter {
    private final GladiatorGame game;
    private final int score;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Rectangle panel = new Rectangle(190f, 126f, 420f, 244f);
    private final Rectangle playAgainButton = new Rectangle(232f, 206f, 164f, 46f);
    private final Rectangle menuButton = new Rectangle(416f, 206f, 152f, 46f);
    private boolean disposed;
    private boolean transitioning;

    public VictoryScreen(GladiatorGame game) {
        this(game, 0);
    }

    public VictoryScreen(GladiatorGame game, int score) {
        this.game = game;
        this.score = score;
    }

    @Override
    public void show() {
        GameManager.getInstance().getGameStateManager().set(GameStateManager.State.VICTORY);
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
        ArenaUi.drawCrossedSwords(shapeRenderer, 400f, 328f);
        ArenaUi.drawPanel(shapeRenderer, panel, ArenaUi.INK, ArenaUi.GOLD);
        ArenaUi.drawButton(shapeRenderer, playAgainButton, ArenaUi.GREEN, playAgainButton.contains(mouse));
        ArenaUi.drawButton(shapeRenderer, menuButton, ArenaUi.BLUE, menuButton.contains(mouse));
        shapeRenderer.end();

        game.getBatch().begin();
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "VICTORY", 400f, 336f, 2.15f, ArenaUi.PALE_GOLD);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "THE ARENA IS YOURS", 400f, 294f, 1f, ArenaUi.GOLD);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "SCORE " + score, 400f, 270f, 0.9f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "PLAY AGAIN", playAgainButton.x + playAgainButton.width / 2f, 236f, 0.95f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "MENU", menuButton.x + menuButton.width / 2f, 236f, 1f, ArenaUi.BONE);
        ArenaUi.drawCentered(game.getFont(), game.getBatch(), "R - PLAY AGAIN     M - MENU", 400f, 166f, 0.82f, ArenaUi.GOLD);
        game.getBatch().end();
    }

    private void handleInput() {
        if (transitioning) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            playAgain();
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
        if (playAgainButton.contains(touch)) {
            playAgain();
        } else if (menuButton.contains(touch)) {
            openMenu();
        }
    }

    private void playAgain() {
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
