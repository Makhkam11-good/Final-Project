package com.gladiator.arena.strategy;

public class EasyDifficulty implements DifficultyStrategy {
    @Override
    public float getEnemySpeedMult() {
        return 0.8f;
    }

    @Override
    public float getEnemyDamageMult() {
        return 0.7f;
    }

    @Override
    public float getBossHp() {
        return 300f;
    }

    @Override
    public float getSpawnInterval() {
        return 2.0f;
    }
}
