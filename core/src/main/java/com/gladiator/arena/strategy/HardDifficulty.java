package com.gladiator.arena.strategy;

public class HardDifficulty implements DifficultyStrategy {
    @Override
    public float getEnemySpeedMult() {
        return 1.3f;
    }

    @Override
    public float getEnemyDamageMult() {
        return 1.5f;
    }

    @Override
    public float getBossHp() {
        return 1000f;
    }

    @Override
    public float getSpawnInterval() {
        return 1.0f;
    }
}
