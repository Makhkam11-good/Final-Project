package com.gladiator.arena.ai;

import com.badlogic.gdx.math.Vector2;
import com.gladiator.arena.entities.Boss;

public class TelegraphBossState implements BossState {
    private final Boss boss;
    private final Vector2 dashDirection = new Vector2();
    private float timer;

    public TelegraphBossState(Boss boss) {
        this.boss = boss;
    }

    @Override
    public void enter() {
        timer = boss.getTelegraphDuration();
        boss.clearDashTelegraph();
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        dashDirection.set(playerX - boss.getCenterX(), playerY - boss.getCenterY());
        if (dashDirection.isZero(0.001f)) {
            dashDirection.set(1f, 0f);
        } else {
            dashDirection.nor();
        }

        boss.setPreparedDashDirection(dashDirection.x, dashDirection.y);
        boss.setDashTelegraphVisible(true);

        timer -= delta;
        if (timer <= 0f) {
            boss.changeState(boss.getDashState());
        }
    }

    @Override
    public void exit() {
    }
}
