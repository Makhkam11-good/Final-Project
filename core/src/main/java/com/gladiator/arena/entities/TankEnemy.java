package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.gladiator.arena.ai.EnemyAi;
import com.gladiator.arena.ai.GoblinAi;

public class TankEnemy extends Enemy {
    private final EnemyAi ai;

    public TankEnemy(float x, float y) {
        super(
            x,
            y,
            95f,
            16f,
            58f,
            44f,
            56f,
            6f,
            8f,
            32f,
            42f,
            42,
            Color.GRAY
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
