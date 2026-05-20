package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;

public class RangedEnemy extends Enemy {
    private static final float MOVE_RANGE = 54f;

    public RangedEnemy(float x, float y) {
        super(
            x,
            y,
            34f,
            9f,
            78f,
            32f,
            48f,
            4f,
            6f,
            24f,
            36f,
            32,
            Color.SKY
        );
    }

    @Override
    protected void onWaveSpawn() {
    }

    @Override
    protected void updateMovement(float delta, Player player) {
        if (player == null) {
            return;
        }

        float distance = distanceTo(player);
        if (distance > MOVE_RANGE) {
            moveToward(player.getCenterX(), player.getCenterY(), delta, speed);
        }
    }

    @Override
    protected String getSpriteKey() {
        return "goblin";
    }
}
