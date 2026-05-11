package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Enemy;

public abstract class EnemyFactory {
    public final Enemy create(float x, float y) {
        Enemy enemy = createEnemy(x, y);
        enemy.waveSpawn();
        return enemy;
    }

    protected abstract Enemy createEnemy(float x, float y);
}
