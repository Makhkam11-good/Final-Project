package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.gladiator.arena.managers.AssetManager;

public class Portal {
    private static final float DEFAULT_WIDTH = 58f;
    private static final float DEFAULT_HEIGHT = 82f;

    private final Rectangle bounds = new Rectangle();
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private float stateTime;

    public Portal(float x, float y) {
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Portal(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        bounds.set(x + 8f, y + 8f, Math.max(1f, width - 16f), Math.max(1f, height - 16f));
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void render(SpriteBatch batch, AssetManager assets) {
        assets.drawAnimation(batch, "portal.idle", stateTime, x, y, width, height);
    }

    public void renderFallback(ShapeRenderer shapes) {
        float pulse = 0.5f + 0.5f * MathUtils.sin(stateTime * 4.8f);
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;
        float glowWidth = width + 20f + pulse * 14f;
        float glowHeight = height + 24f + pulse * 18f;

        shapes.setColor(new Color(0.18f, 0.05f, 0.72f, 0.18f));
        shapes.ellipse(centerX - glowWidth / 2f, centerY - glowHeight / 2f, glowWidth, glowHeight);
        shapes.setColor(new Color(0.04f, 0.02f, 0.08f, 0.92f));
        shapes.ellipse(x + 4f, y, width - 8f, height);
        shapes.setColor(new Color(0.22f, 0.55f + pulse * 0.18f, 0.85f, 0.78f));
        shapes.ellipse(x + 10f, y + 8f, width - 20f, height - 16f);
        shapes.setColor(new Color(0.74f, 0.92f, 1f, 0.82f));
        shapes.circle(centerX, centerY, 8f + pulse * 5f);
    }

    public boolean overlaps(Player player) {
        return player != null && bounds.overlaps(player.getBounds());
    }
}
