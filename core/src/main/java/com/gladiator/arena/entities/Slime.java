package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class Slime extends Enemy {
    private static final float SIGHT_RANGE = 200f;
    private static final float WANDER_CHANGE_INTERVAL = 1.2f;

    private float wanderX;
    private float wanderY;
    private float wanderTimer;

    public Slime(float x, float y) {
        super(
            x,
            y,
            20f,
            5f,
            60f,
            32f,
            32f,
            4f,
            6f,
            24f,
            20f,
            10,
            Color.GREEN
        );
    }

    @Override
    protected void onWaveSpawn() {
        pickWanderDirection();
    }

    @Override
    protected void updateMovement(float delta, Player player) {
        if (distanceTo(player) <= SIGHT_RANGE) {
            moveToward(player.getX() + Player.SPRITE_WIDTH / 2f, player.getY() + Player.SPRITE_HEIGHT / 2f, delta, speed);
            return;
        }

        wanderTimer -= delta;
        if (wanderTimer <= 0f) {
            pickWanderDirection();
        }

        x += wanderX * speed * 0.55f * delta;
        y += wanderY * speed * 0.55f * delta;

        if (x <= 0f || x >= ARENA_WIDTH - spriteWidth || y <= 0f || y >= ARENA_HEIGHT - spriteHeight) {
            pickWanderDirection();
        }
    }

    private void pickWanderDirection() {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        wanderX = MathUtils.cos(angle);
        wanderY = MathUtils.sin(angle);
        wanderTimer = WANDER_CHANGE_INTERVAL;
    }
}
