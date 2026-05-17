package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.gladiator.arena.managers.AssetManager;

public class Portal {
    private static final float WIDTH = 58f;
    private static final float HEIGHT = 82f;

    private final Rectangle bounds;
    private final float x;
    private final float y;
    private float stateTime;

    public Portal(float x, float y) {
        this.x = x;
        this.y = y;
        bounds = new Rectangle(x + 8f, y + 8f, WIDTH - 16f, HEIGHT - 16f);
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void render(SpriteBatch batch, AssetManager assets) {
        assets.drawAnimation(batch, "portal.idle", stateTime, x, y, WIDTH, HEIGHT);
    }

    public void renderFallback(ShapeRenderer shapes) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 4.8f);
        float centerX = x + WIDTH / 2f;
        float centerY = y + HEIGHT / 2f;
        shapes.setColor(new Color(0.04f, 0.02f, 0.08f, 0.92f));
        shapes.ellipse(x + 4f, y, WIDTH - 8f, HEIGHT);
        shapes.setColor(new Color(0.22f, 0.55f + pulse * 0.18f, 0.85f, 0.78f));
        shapes.ellipse(x + 10f, y + 8f, WIDTH - 20f, HEIGHT - 16f);
        shapes.setColor(new Color(0.74f, 0.92f, 1f, 0.82f));
        shapes.circle(centerX, centerY, 8f + pulse * 5f);
    }

    public boolean overlaps(Player player) {
        return player != null && bounds.overlaps(player.getBounds());
    }
}
