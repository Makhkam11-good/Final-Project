package com.gladiator.arena.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gladiator.arena.managers.AssetManager;
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
    private static final float ATTACK_STATE_DURATION = 0.18f;
    private static final float ATTACK_EFFECT_DURATION = 0.22f;
    private static final float ATTACK_RADIUS = 80f;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float facingX = 1f;
    private float facingY;
    private float hp;
    private float attackTimer;
    private float attackStateTimer;
    private float attackEffectTimer;
    private float attackStartX;
    private float attackStartY;
    private float attackEndX;
    private float attackEndY;
    private float stateTime;

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
        stateTime += delta;
        if (isDead()) {
            velocityX = 0f;
            velocityY = 0f;
            setCurrentState(deadState);
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
        updateAttackEffectTimer(delta);
        if (attackStateTimer > 0f) {
            setCurrentState(attackState);
        } else if (MathUtils.isZero(velocityX) && MathUtils.isZero(velocityY)) {
            setCurrentState(idleState);
        } else {
            setCurrentState(runState);
        }

        updateBounds();
    }

    public void render(SpriteBatch batch, AssetManager assets) {
        assets.drawAnimation(batch, "player." + getAnimationState(), stateTime, x, y, SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    public void takeDamage(float amount) {
        hp = Math.max(0f, hp - (amount * stats.getIncomingDamageMultiplier()));
        if (hp <= 0f) {
            setCurrentState(deadState);
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

    public float getCenterX() {
        return x + SPRITE_WIDTH / 2f;
    }

    public float getCenterY() {
        return y + SPRITE_HEIGHT / 2f;
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

    public boolean isAttackEffectActive() {
        return attackEffectTimer > 0f;
    }

    public float getAttackEffectProgress() {
        return MathUtils.clamp(attackEffectTimer / ATTACK_EFFECT_DURATION, 0f, 1f);
    }

    public float getAttackStartX() {
        return attackStartX;
    }

    public float getAttackStartY() {
        return attackStartY;
    }

    public float getAttackEndX() {
        return attackEndX;
    }

    public float getAttackEndY() {
        return attackEndY;
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
            facingX = moveX;
            facingY = moveY;
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
        attackEffectTimer = ATTACK_EFFECT_DURATION;
        attackStartX = getCenterX();
        attackStartY = getCenterY();

        Enemy target = findClosestEnemyInRange(enemies);
        if (target != null) {
            attackEndX = target.getCenterX();
            attackEndY = target.getCenterY();
            target.takeDamage(getDamage());
            return;
        }

        attackEndX = attackStartX + facingX * ATTACK_RADIUS * 0.75f;
        attackEndY = attackStartY + facingY * ATTACK_RADIUS * 0.75f;
    }

    private void updateAttackStateTimer(float delta) {
        attackStateTimer -= delta;
        if (attackStateTimer < 0f) {
            attackStateTimer = 0f;
        }
    }

    private void updateAttackEffectTimer(float delta) {
        attackEffectTimer -= delta;
        if (attackEffectTimer < 0f) {
            attackEffectTimer = 0f;
        }
    }

    private void updateBounds() {
        bounds.set(x + HITBOX_OFFSET_X, y + HITBOX_OFFSET_Y, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    private void setCurrentState(PlayerState nextState) {
        if (currentState == nextState) {
            return;
        }

        currentState = nextState;
        stateTime = 0f;
    }

    private String getAnimationState() {
        String stateName = currentState.getName().toLowerCase();
        if ("dead".equals(stateName)) {
            return "dead";
        }
        if ("attack".equals(stateName)) {
            return "attack";
        }
        if ("run".equals(stateName)) {
            return "run";
        }
        return "idle";
    }

    private boolean isDead() {
        return hp <= 0f;
    }

    private Enemy findClosestEnemyInRange(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }

        float centerX = getCenterX();
        float centerY = getCenterY();
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
