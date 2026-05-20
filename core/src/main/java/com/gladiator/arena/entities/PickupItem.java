package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class PickupItem {
    public enum Type {
        HEART,
        SHIELD
    }

    private static final float SIZE = 18f;
    private static final float PICKUP_RADIUS = 16f;
    private static final float BOB_SPEED = 4.8f;
    private static final float BOB_HEIGHT = 2.5f;

    private final Rectangle bounds = new Rectangle();
    private final Type type;
    private final float x;
    private final float y;
    private float stateTime;

    public PickupItem(Type type, float centerX, float centerY) {
        this.type = type;
        x = centerX - SIZE / 2f;
        y = centerY - SIZE / 2f;
        bounds.set(centerX - PICKUP_RADIUS, centerY - PICKUP_RADIUS, PICKUP_RADIUS * 2f, PICKUP_RADIUS * 2f);
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void renderFallback(ShapeRenderer shapes) {
        float centerX = x + SIZE / 2f;
        float centerY = getDrawY() + SIZE / 2f;
        if (type == Type.HEART) {
            drawHeart(shapes, centerX, centerY);
        } else {
            drawShield(shapes, centerX, centerY);
        }
    }

    public boolean overlaps(Player player) {
        return player != null && bounds.overlaps(player.getBounds());
    }

    public Type getType() {
        return type;
    }

    private float getDrawY() {
        return y + (float) Math.sin(stateTime * BOB_SPEED) * BOB_HEIGHT;
    }

    private void drawHeart(ShapeRenderer shapes, float centerX, float centerY) {
        shapes.setColor(0.20f, 0.02f, 0.03f, 1f);
        shapes.circle(centerX - 5f, centerY + 2f, 7f);
        shapes.circle(centerX + 5f, centerY + 2f, 7f);
        shapes.triangle(centerX - 12f, centerY, centerX + 12f, centerY, centerX, centerY - 13f);
        shapes.setColor(Color.SCARLET);
        shapes.circle(centerX - 4f, centerY + 3f, 5f);
        shapes.circle(centerX + 4f, centerY + 3f, 5f);
        shapes.triangle(centerX - 9f, centerY + 1f, centerX + 9f, centerY + 1f, centerX, centerY - 10f);
    }

    private void drawShield(ShapeRenderer shapes, float centerX, float centerY) {
        shapes.setColor(0.02f, 0.08f, 0.15f, 1f);
        shapes.rect(centerX - 10f, centerY - 4f, 20f, 17f);
        shapes.triangle(centerX - 10f, centerY - 4f, centerX + 10f, centerY - 4f, centerX, centerY - 15f);
        shapes.setColor(0.26f, 0.62f, 1f, 1f);
        shapes.rect(centerX - 7f, centerY - 2f, 14f, 13f);
        shapes.triangle(centerX - 7f, centerY - 2f, centerX + 7f, centerY - 2f, centerX, centerY - 11f);
    }
}
