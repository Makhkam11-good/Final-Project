package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.gladiator.arena.ai.EnemyAi;
import com.gladiator.arena.ai.SlimeAi;

public class FastEnemy extends Enemy {
    private final EnemyAi ai;

    public FastEnemy(float x, float y) {
        super(
            x,
            y,
            16f,
            4f,
            145f,
            32f,
            32f,
            4f,
            6f,
            24f,
            20f,
            14,
            Color.CYAN
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
