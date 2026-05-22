package com.gladiator.arena.strategy;

public class MediumDifficulty implements DifficultyStrategy {
    @Override
    public float getEnemySpeedMult() {
        return 1.0f;
    }

    @Override
    public float getEnemyDamageMult() {
        return 0.9f;
    }

    @Override
    public float getBossHp() {
        return 450f;
    }

    @Override
    public float getSpawnInterval() {
        return 2.0f;
    }
}
