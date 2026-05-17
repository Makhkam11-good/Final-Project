package com.gladiator.arena.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public final class AssetManager {
    private static final String GAME_ATLAS = "atlas/game.atlas";
    private static final AssetManager INSTANCE = new AssetManager();

    private final com.badlogic.gdx.assets.AssetManager manager;
    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>();
    private final Color previousBatchColor = new Color();
    private TextureAtlas gameAtlas;
    private TextureRegion floorTile;

    private AssetManager() {
        manager = new com.badlogic.gdx.assets.AssetManager();
    }

    public static AssetManager getInstance() {
        return INSTANCE;
    }

    public com.badlogic.gdx.assets.AssetManager getManager() {
        return manager;
    }

    public void loadGameAssets() {
        if (gameAtlas != null) {
            return;
        }

        manager.load(GAME_ATLAS, TextureAtlas.class);
        manager.finishLoadingAsset(GAME_ATLAS);
        gameAtlas = manager.get(GAME_ATLAS, TextureAtlas.class);
        floorTile = gameAtlas.findRegion("arena_floor");

        registerLoop("player.idle", "knight_idle", 0.35f);
        registerLoop("player.run", "knight_run", 0.12f);
        registerLoop("player.attack", "knight_attack", 0.06f);
        registerNormal("player.dead", "knight_death", 0.16f);

        registerLoop("slime.run", "slime_walk", 0.18f);
        registerLoop("slime.attack", "slime_attack", 0.10f);
        registerNormal("slime.dead", "slime_death", 0.12f);

        registerLoop("goblin.run", "goblin_walk", 0.14f);
        registerLoop("goblin.attack", "goblin_attack", 0.09f);
        registerNormal("goblin.dead", "goblin_death", 0.12f);

        registerLoop("boss.run", "boss_walk", 0.16f);
        registerLoop("boss.attack", "boss_attack", 0.10f);
        registerNormal("boss.dead", "boss_death", 0.14f);
    }

    public void drawTiledFloor(SpriteBatch batch, float width, float height) {
        loadGameAssets();
        if (floorTile == null) {
            return;
        }

        int tileWidth = floorTile.getRegionWidth();
        int tileHeight = floorTile.getRegionHeight();
        for (float x = 0f; x < width; x += tileWidth) {
            for (float y = 0f; y < height; y += tileHeight) {
                batch.draw(floorTile, x, y, tileWidth, tileHeight);
            }
        }
    }

    public void drawAnimation(SpriteBatch batch, String key, float stateTime, float x, float y, float width, float height) {
        drawAnimation(batch, key, stateTime, x, y, width, height, null);
    }

    public void drawAnimation(
        SpriteBatch batch,
        String key,
        float stateTime,
        float x,
        float y,
        float width,
        float height,
        Color tint
    ) {
        loadGameAssets();
        Animation<TextureRegion> animation = animations.get(key);
        if (animation == null) {
            return;
        }

        if (tint != null) {
            previousBatchColor.set(batch.getColor());
            batch.setColor(tint);
        }
        batch.draw(animation.getKeyFrame(stateTime), x, y, width, height);
        if (tint != null) {
            batch.setColor(previousBatchColor);
        }
    }

    public void dispose() {
        animations.clear();
        gameAtlas = null;
        floorTile = null;
        manager.dispose();
    }

    private void registerLoop(String key, String regionName, float frameDuration) {
        register(key, regionName, frameDuration, Animation.PlayMode.LOOP);
    }

    private void registerNormal(String key, String regionName, float frameDuration) {
        register(key, regionName, frameDuration, Animation.PlayMode.NORMAL);
    }

    private void register(String key, String regionName, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureAtlas.AtlasRegion> regions = gameAtlas.findRegions(regionName);
        if (regions == null || regions.size == 0) {
            TextureAtlas.AtlasRegion region = gameAtlas.findRegion(regionName);
            if (region == null) {
                return;
            }
            regions = new Array<>();
            regions.add(region);
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, regions, playMode);
        animations.put(key, animation);
    }
}
