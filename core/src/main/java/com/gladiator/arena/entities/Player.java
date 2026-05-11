package com.gladiator.arena.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gladiator.arena.entities.states.AttackState;
import com.gladiator.arena.entities.states.DeadState;
import com.gladiator.arena.entities.states.IdleState;
import com.gladiator.arena.entities.states.PlayerState;
import com.gladiator.arena.entities.states.RunState;
import com.gladiator.arena.decorator.BasePlayerStats;
import com.gladiator.arena.decorator.PlayerStats;

import java.util.List;
import java.util.function.UnaryOperator;

public class Player {
    public static final float SPRITE_WIDTH = 48f;
    public static final float SPRITE_HEIGHT = 48f;
    private static final float HITBOX_OFFSET_X = 8f;
    private static final float HITBOX_OFFSET_Y = 4f;
    private static final float HITBOX_WIDTH = 32f;
    private static final float HITBOX_HEIGHT = 40f;
    private static final float ARENA_WIDTH = 800f;
    private static final float ARENA_HEIGHT = 480f;
    private static final float ATTACK_STATE_DURATION = 0.12f;
    private static final float ATTACK_RADIUS = 80f;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float hp;
    private float attackTimer;
    private float attackStateTimer;

    private final Rectangle bounds = new Rectangle();
    private PlayerStats stats;
    private final PlayerState idleState = new IdleState();
    private final PlayerState runState = new RunState();
    private final PlayerState attackState = new AttackState();
    private final PlayerState deadState = new DeadState();
    private PlayerState currentState;

    public Player() {
        x = (ARENA_WIDTH - SPRITE_WIDTH) / 2f;
        y = (ARENA_HEIGHT - SPRITE_HEIGHT) / 2f;
        stats = new BasePlayerStats();
        hp = stats.getMaxHp();
        attackTimer = stats.getAttackCooldown();
        currentState = idleState;
        updateBounds();
    }

    public void update(float delta) {
        update(delta, null);
    }

    public void update(float delta, List<Enemy> enemies) {
        if (isDead()) {
            velocityX = 0f;
            velocityY = 0f;
            currentState = deadState;
            updateBounds();
            return;
        }

        handleMovement(delta);

        attackTimer -= delta;
        if (attackTimer <= 0f) {
            attackTimer = stats.getAttackCooldown();
            performAttack(enemies);
        }

        updateAttackStateTimer(delta);
        if (attackStateTimer > 0f) {
            currentState = attackState;
        } else if (MathUtils.isZero(velocityX) && MathUtils.isZero(velocityY)) {
            currentState = idleState;
        } else {
            currentState = runState;
        }

        updateBounds();
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    public void takeDamage(float amount) {
        hp = Math.max(0f, hp - (amount * stats.getIncomingDamageMultiplier()));
        if (hp <= 0f) {
            currentState = deadState;
        }
    }

    public void applyUpgrade(UnaryOperator<PlayerStats> upgradeFactory) {
        if (upgradeFactory == null) {
            return;
        }

        float previousMaxHp = getMaxHp();
        stats = upgradeFactory.apply(stats);
        float newMaxHp = getMaxHp();

        if (newMaxHp > previousMaxHp) {
            hp += newMaxHp - previousMaxHp;
        }

        hp = MathUtils.clamp(hp, 0f, newMaxHp);
        attackTimer = Math.min(attackTimer, stats.getAttackCooldown());
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return stats.getMaxHp();
    }

    public PlayerState getCurrentState() {
        return currentState;
    }

    public float getAttackTimer() {
        return attackTimer;
    }

    public float getDamage() {
        return stats.getDamage();
    }

    public float getSpeed() {
        return stats.getSpeed();
    }

    public float getAttackCooldown() {
        return stats.getAttackCooldown();
    }

    public float getIncomingDamageMultiplier() {
        return stats.getIncomingDamageMultiplier();
    }

    private void handleMovement(float delta) {
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveY += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveY -= 1f;
        }

        if (!MathUtils.isZero(moveX) || !MathUtils.isZero(moveY)) {
            float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;
        }

        velocityX = moveX * stats.getSpeed();
        velocityY = moveY * stats.getSpeed();

        x += velocityX * delta;
        y += velocityY * delta;

        x = MathUtils.clamp(x, 0f, ARENA_WIDTH - SPRITE_WIDTH);
        y = MathUtils.clamp(y, 0f, ARENA_HEIGHT - SPRITE_HEIGHT);
    }

    private void performAttack(List<Enemy> enemies) {
        attackStateTimer = ATTACK_STATE_DURATION;

        Enemy target = findClosestEnemyInRange(enemies);
        if (target != null) {
            target.takeDamage(getDamage());
        }
    }

    private void updateAttackStateTimer(float delta) {
        attackStateTimer -= delta;
        if (attackStateTimer < 0f) {
            attackStateTimer = 0f;
        }
    }

    private void updateBounds() {
        bounds.set(x + HITBOX_OFFSET_X, y + HITBOX_OFFSET_Y, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    private boolean isDead() {
        return hp <= 0f;
    }

    private Enemy findClosestEnemyInRange(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }

        float centerX = x + SPRITE_WIDTH / 2f;
        float centerY = y + SPRITE_HEIGHT / 2f;
        float bestDistance = ATTACK_RADIUS;
        Enemy closest = null;

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                continue;
            }

            float distance = Vector2.dst(centerX, centerY, enemy.getCenterX(), enemy.getCenterY());
            if (distance <= bestDistance) {
                bestDistance = distance;
                closest = enemy;
            }
        }

        return closest;
    }
}
