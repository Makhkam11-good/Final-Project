package com.gladiator.arena.entities.boss;

public interface BossState {
    void enter();

    void update(float delta, float playerX, float playerY);

    void exit();
}
