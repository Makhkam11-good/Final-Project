package com.gladiator.arena.ai;

import com.gladiator.arena.entities.Boss;

public class ChaseBossState implements BossState {
    private final Boss boss;
    private float timer;

    public ChaseBossState(Boss boss) {
        this.boss = boss;
    }

    @Override
    public void enter() {
        timer = boss.getChaseDuration();
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        boss.moveTowardTarget(playerX, playerY, delta, boss.getChaseSpeed());
        timer -= delta;
        if (timer <= 0f) {
            boss.changeState(boss.getTelegraphState());
        }
    }

    @Override
    public void exit() {
    }
}
