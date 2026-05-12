package com.gladiator.arena.ai;

import com.gladiator.arena.entities.Boss;

public class IdleBossState implements BossState {
    private static final float DURATION = 1.5f;

    private final Boss boss;
    private float timer;

    public IdleBossState(Boss boss) {
        this.boss = boss;
    }

    @Override
    public void enter() {
        timer = DURATION;
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        timer -= delta;
        if (timer <= 0f) {
            boss.changeState(boss.getChaseState());
        }
    }

    @Override
    public void exit() {
    }
}
