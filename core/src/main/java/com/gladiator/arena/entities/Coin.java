package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.gladiator.arena.managers.AssetManager;

public class Coin {
    private static final float SIZE = 14f;
    private static final float PICKUP_RADIUS = 14f;
    private static final float BOB_SPEED = 5.5f;
    private static final float BOB_HEIGHT = 2.5f;
    private static final Color SHADOW = new Color(0.08f, 0.045f, 0.01f, 1f);
    private static final Color RIM = new Color(0.78f, 0.43f, 0.08f, 1f);
    private static final Color GOLD = new Color(1f, 0.72f, 0.16f, 1f);
    private static final Color HIGHLIGHT = new Color(1f, 0.93f, 0.48f, 1f);

    private final Rectangle bounds = new Rectangle();
    private final float x;
    private final float y;
    private final int value;
    private float stateTime;

    public Coin(float centerX, float centerY) {
        this(centerX, centerY, 1);
    }

    public Coin(float centerX, float centerY, int value) {
        x = centerX - SIZE / 2f;
        y = centerY - SIZE / 2f;
        this.value = Math.max(1, value);
        bounds.set(centerX - PICKUP_RADIUS, centerY - PICKUP_RADIUS, PICKUP_RADIUS * 2f, PICKUP_RADIUS * 2f);
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void render(SpriteBatch batch, AssetManager assets) {
        assets.drawAnimation(batch, "coin.idle", stateTime, x, getDrawY(), SIZE, SIZE);
    }

    public void renderFallback(ShapeRenderer shapes) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 7f);
        float radius = SIZE * 0.44f + pulse * 1.6f;
        float centerX = x + SIZE / 2f;
        float centerY = getDrawY() + SIZE / 2f;

        shapes.setColor(SHADOW);
        shapes.circle(centerX + 1.5f, centerY - 1.5f, radius + 2f);
        shapes.setColor(RIM);
        shapes.circle(centerX, centerY, radius + 1.5f);
        shapes.setColor(GOLD);
        shapes.circle(centerX, centerY, radius);
        shapes.setColor(HIGHLIGHT);
        shapes.circle(centerX - radius * 0.35f, centerY + radius * 0.35f, radius * 0.28f);
        if (value > 1) {
            shapes.rectLine(centerX - radius * 0.42f, centerY, centerX + radius * 0.42f, centerY, 2f);
        }
    }

    public boolean overlaps(Player player) {
        return player != null && bounds.overlaps(player.getBounds());
    }

    public int getValue() {
        return value;
    }

    private float getDrawY() {
        return y + (float) Math.sin(stateTime * BOB_SPEED) * BOB_HEIGHT;
    }
}
