package com.gladiator.arena.ai;

import com.badlogic.gdx.math.Vector2;
import com.gladiator.arena.entities.Boss;

public class DashBossState implements BossState {
    private static final float DURATION = 0.6f;

    private final Boss boss;
    private final Vector2 dashDirection = new Vector2();
    private float timer;
    private boolean dashHitRegistered;
    private boolean directionLocked;

    public DashBossState(Boss boss) {
        this.boss = boss;
    }

    @Override
    public void enter() {
        timer = DURATION;
        dashHitRegistered = false;
        directionLocked = true;
        dashDirection.set(boss.getPreparedDashX(), boss.getPreparedDashY());
        if (dashDirection.isZero(0.001f)) {
            dashDirection.set(1f, 0f);
        } else {
            dashDirection.nor();
        }
        boss.clearDashTelegraph();
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        if (!directionLocked) {
            lockDashDirection(playerX, playerY);
        }

        boss.moveBy(dashDirection.x * boss.getDashSpeed() * delta, dashDirection.y * boss.getDashSpeed() * delta);
        boss.finishStateStep();

        if (!dashHitRegistered && boss.overlapsTargetPlayer()) {
            boss.damageTargetPlayer(boss.getDashDamage());
            dashHitRegistered = true;
        }

        timer -= delta;
        if (timer <= 0f) {
            boss.changeState(boss.getIdleState());
        }
    }

    @Override
    public void exit() {
        boss.clearDashTelegraph();
    }

    private void lockDashDirection(float playerX, float playerY) {
        dashDirection.set(playerX - boss.getCenterX(), playerY - boss.getCenterY());
        if (dashDirection.isZero(0.001f)) {
            dashDirection.set(1f, 0f);
        } else {
            dashDirection.nor();
        }
        directionLocked = true;
    }
}
