package com.gladiator.arena.managers;

public final class AssetManager {
    private static final AssetManager INSTANCE = new AssetManager();

    private final com.badlogic.gdx.assets.AssetManager manager;

    private AssetManager() {
        manager = new com.badlogic.gdx.assets.AssetManager();
    }

    public static AssetManager getInstance() {
        return INSTANCE;
    }

    public com.badlogic.gdx.assets.AssetManager getManager() {
        return manager;
    }
}
