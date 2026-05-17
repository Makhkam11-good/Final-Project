package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Coin {
    private static final float PICKUP_RADIUS = 14f;
    private static final float BASE_RADIUS = 7f;
    private static final float PULSE_AMOUNT = 1.6f;
    private static final Color SHADOW = new Color(0.08f, 0.045f, 0.01f, 1f);
    private static final Color RIM = new Color(0.78f, 0.43f, 0.08f, 1f);
    private static final Color GOLD = new Color(1f, 0.72f, 0.16f, 1f);
    private static final Color HIGHLIGHT = new Color(1f, 0.93f, 0.48f, 1f);

    private final Rectangle bounds = new Rectangle();
    private final float x;
    private final float y;
    private final int value;
    private float stateTime;
    private boolean collected;

    public Coin(float x, float y, int value) {
        this.x = x;
        this.y = y;
        this.value = Math.max(1, value);
        updateBounds();
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void render(ShapeRenderer shapes) {
        if (collected) {
            return;
        }

        float pulse = (MathUtils.sin(stateTime * 7f) + 1f) * 0.5f;
        float radius = BASE_RADIUS + pulse * PULSE_AMOUNT;

        shapes.setColor(SHADOW);
        shapes.circle(x + 1.5f, y - 1.5f, radius + 2f);
        shapes.setColor(RIM);
        shapes.circle(x, y, radius + 1.5f);
        shapes.setColor(GOLD);
        shapes.circle(x, y, radius);
        shapes.setColor(HIGHLIGHT);
        shapes.circle(x - radius * 0.35f, y + radius * 0.35f, radius * 0.28f);
        if (value > 1) {
            shapes.rectLine(x - radius * 0.42f, y, x + radius * 0.42f, y, 2f);
        }
    }

    public boolean overlaps(Player player) {
        return !collected && player != null && bounds.overlaps(player.getBounds());
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public int getValue() {
        return value;
    }

    private void updateBounds() {
        bounds.set(x - PICKUP_RADIUS, y - PICKUP_RADIUS, PICKUP_RADIUS * 2f, PICKUP_RADIUS * 2f);
    }
}
