package com.gladiator.arena.ai;

import com.gladiator.arena.entities.Boss;

public class ChaseBossState implements BossState {
    private static final float DURATION = 3.0f;

    private final Boss boss;
    private float timer;

    public ChaseBossState(Boss boss) {
        this.boss = boss;
    }

    @Override
    public void enter() {
        timer = DURATION;
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        boss.moveTowardTarget(playerX, playerY, delta, boss.getChaseSpeed());
        timer -= delta;
        if (timer <= 0f) {
            boss.changeState(boss.getDashState());
        }
    }

    @Override
    public void exit() {
    }
}
