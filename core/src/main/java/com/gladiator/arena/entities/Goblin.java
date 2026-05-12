package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.gladiator.arena.ai.EnemyAi;
import com.gladiator.arena.ai.GoblinAi;

public class Goblin extends Enemy {
    private final EnemyAi ai;

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
        ai = new GoblinAi(this);
    }

    @Override
    protected void onWaveSpawn() {
        ai.onSpawn();
    }

    @Override
    protected void updateMovement(float delta, Player player) {
        ai.update(delta, player);
    }

    @Override
    protected String getSpriteKey() {
        return "goblin";
    }
}
