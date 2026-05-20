package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.gladiator.arena.ai.EnemyAi;
import com.gladiator.arena.ai.GoblinAi;

public class MiniBoss extends Enemy {
    private final EnemyAi ai;

    public MiniBoss(float x, float y) {
        super(
            x,
            y,
            170f,
            24f,
            72f,
            58f,
            64f,
            7f,
            8f,
            44f,
            52f,
            160,
            Color.RED
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
