package com.gladiator.arena.strategy;

public class MediumDifficulty implements DifficultyStrategy {
    @Override
    public float getEnemySpeedMult() {
        return 1.0f;
    }

    @Override
    public float getEnemyDamageMult() {
        return 1.0f;
    }

    @Override
    public float getBossHp() {
        return 500f;
    }

    @Override
    public float getEnemyCountMultiplier() {
        return 1.0f;
    }

    @Override
    public float getSpawnInterval() {
        return 1.5f;
    }
}
