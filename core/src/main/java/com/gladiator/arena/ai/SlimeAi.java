package com.gladiator.arena.ai;

import com.badlogic.gdx.math.MathUtils;
import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.Player;

public class SlimeAi implements EnemyAi {
    private static final float SIGHT_RANGE = 200f;
    private static final float WANDER_CHANGE_INTERVAL = 1.2f;

    private final Enemy enemy;
    private float wanderX;
    private float wanderY;
    private float wanderTimer;

    public SlimeAi(Enemy enemy) {
        this.enemy = enemy;
    }

    @Override
    public void onSpawn() {
        pickWanderDirection();
    }

    @Override
    public void update(float delta, Player player) {
        if (player != null && enemy.distanceTo(player) <= SIGHT_RANGE) {
            enemy.moveToward(player.getCenterX(), player.getCenterY(), delta, enemy.getSpeed());
            return;
        }

        wanderTimer -= delta;
        if (wanderTimer <= 0f) {
            pickWanderDirection();
        }

        enemy.moveBy(wanderX * enemy.getSpeed() * 0.55f * delta, wanderY * enemy.getSpeed() * 0.55f * delta);

        if (enemy.isTouchingArenaEdge()) {
            pickWanderDirection();
        }
    }

    private void pickWanderDirection() {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        wanderX = MathUtils.cos(angle);
        wanderY = MathUtils.sin(angle);
        wanderTimer = WANDER_CHANGE_INTERVAL;
    }
}
