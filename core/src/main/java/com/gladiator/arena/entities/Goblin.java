package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;

public class Goblin extends Enemy {
    private boolean aggressive;

    public Goblin(float x, float y) {
        super(
            x,
            y,
            40f,
            12f,
            100f,
            32f,
            48f,
            4f,
            6f,
            24f,
            36f,
            25,
            Color.ORANGE
        );
    }

    @Override
    protected void onWaveSpawn() {
        aggressive = true;
    }

    @Override
    protected void updateMovement(float delta, Player player) {
        if (!aggressive) {
            return;
        }

        moveToward(player.getX() + Player.SPRITE_WIDTH / 2f, player.getY() + Player.SPRITE_HEIGHT / 2f, delta, speed);
    }
}
