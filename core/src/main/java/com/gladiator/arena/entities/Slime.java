package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.gladiator.arena.ai.EnemyAi;
import com.gladiator.arena.ai.SlimeAi;

public class Slime extends Enemy {
    private final EnemyAi ai;

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
        ai = new SlimeAi(this);
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
        return "slime";
    }
}
