package com.gladiator.arena;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gladiator.arena.managers.AssetManager;
import com.gladiator.arena.managers.SoundManager;
import com.gladiator.arena.screens.MenuScreen;

public class GladiatorGame extends Game {
    public static final float WORLD_WIDTH = 800f;
    public static final float WORLD_HEIGHT = 480f;

    private SpriteBatch batch;
    private BitmapFont font;
    private TextureAtlas uiAtlas;
    private OrthographicCamera camera;
    private Viewport viewport;
    private final Vector2 screenToWorld = new Vector2();

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        uiAtlas = new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas"));
        font = createUiFont();
        AssetManager.getInstance().loadGameAssets();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public Vector2 screenToWorld(float screenX, float screenY) {
        screenToWorld.set(screenX, screenY);
        viewport.unproject(screenToWorld);
        return screenToWorld;
    }

    private BitmapFont createUiFont() {
        TextureRegion fontRegion = uiAtlas.findRegion("font");
        BitmapFont uiFont = fontRegion == null
            ? new BitmapFont()
            : new BitmapFont(Gdx.files.internal("ui/font.fnt"), fontRegion, false);
        uiFont.setUseIntegerPositions(true);
        return uiFont;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
        if (batch != null) {
            batch.setProjectionMatrix(camera.combined);
        }
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.dispose();
        }
        batch.dispose();
        font.dispose();
        uiAtlas.dispose();
        AssetManager.getInstance().dispose();
        SoundManager.getInstance().dispose();
    }
}
