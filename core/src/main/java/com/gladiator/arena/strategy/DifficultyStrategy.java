package com.gladiator.arena.strategy;

public interface DifficultyStrategy {
    float getEnemySpeedMult();

    float getEnemyDamageMult();

    float getBossHp();

    float getSpawnInterval();
}
