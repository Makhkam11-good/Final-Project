package com.gladiator.arena.ai;

public interface BossState {
    void enter();

    void update(float delta, float playerX, float playerY);

    void exit();
}
