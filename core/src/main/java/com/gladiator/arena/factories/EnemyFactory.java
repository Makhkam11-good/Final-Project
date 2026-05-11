package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.strategy.DifficultyStrategy;

public abstract class EnemyFactory {
    public final Enemy create(float x, float y) {
        Enemy enemy = createEnemy(x, y);
        applyDifficulty(enemy);
        enemy.waveSpawn();
        return enemy;
    }

    protected abstract Enemy createEnemy(float x, float y);

    protected void applyDifficulty(Enemy enemy) {
        DifficultyStrategy difficulty = GameManager.getInstance().getDifficulty();
        enemy.applyDifficulty(difficulty.getEnemySpeedMult(), difficulty.getEnemyDamageMult());
    }
}
