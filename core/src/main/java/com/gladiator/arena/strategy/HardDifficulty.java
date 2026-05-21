package com.gladiator.arena.strategy;

public class HardDifficulty implements DifficultyStrategy {
    @Override
    public float getEnemySpeedMult() {
        return 1.1f;
    }

    @Override
    public float getEnemyDamageMult() {
        return 1.1f;
    }

    @Override
    public float getBossHp() {
        return 600f;
    }

    @Override
    public float getSpawnInterval() {
        return 2.0f;
    }
}
