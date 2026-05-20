package com.gladiator.arena.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
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
import com.gladiator.arena.managers.SoundManager;

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
    private static final float ATTACK_CONE_COS = 0.45f;
    private static final float CRITICAL_CHANCE = 0.16f;
    private static final float CRITICAL_DAMAGE_MULTIPLIER = 1.6f;
    private static final float DASH_SPEED = 470f;
    private static final float DASH_DURATION = 0.18f;
    private static final float DASH_COOLDOWN = 0.9f;
    private static final float DAMAGE_COOLDOWN = 0.65f;
    private static final float DAMAGE_FLASH_DURATION = 0.18f;
    private static final float REVIVE_INVULNERABILITY_DURATION = 1.0f;
    private static final int REVIVE_COST = 10;
    private static final Color DAMAGE_FLASH_COLOR = new Color(1f, 0.46f, 0.46f, 1f);

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
    private float dashCooldownTimer;
    private float dashStateTimer;
    private float dashDirectionX = 1f;
    private float dashDirectionY;
    private float stateTime;
    private float damageCooldownTimer;
    private float damageFlashTimer;
    private float invulnerabilityTimer;
    private float lastDamageTaken;
    private int coins;
    private boolean reviveUsed;

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
        attackTimer = 0f;
        currentState = idleState;
        updateBounds();
    }

    public void update(float delta) {
        update(delta, null);
    }

    public void update(float delta, List<Enemy> enemies) {
        stateTime += delta;
        updateDamageTimers(delta);
        updateInvulnerabilityTimer(delta);
        updateDashCooldownTimer(delta);
        if (isDead()) {
            velocityX = 0f;
            velocityY = 0f;
            setCurrentState(deadState);
            updateBounds();
            return;
        }

        handleMovement(delta);
        updateDashStateTimer(delta);

        attackTimer = Math.max(0f, attackTimer - delta);
        if (isAttackPressed() && MathUtils.isZero(attackTimer)) {
            performAttack(enemies);
            attackTimer = stats.getAttackCooldown();
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
        assets.drawAnimation(
            batch,
            "player." + getAnimationState(),
            stateTime,
            x,
            y,
            SPRITE_WIDTH,
            SPRITE_HEIGHT,
            damageFlashTimer > 0f ? DAMAGE_FLASH_COLOR : null
        );
    }

    public boolean takeContactDamage(float damagePerSecond) {
        // Contact damage used to be frame-based before; scale one hit to keep roughly the same DPS.
        return takeDamage(damagePerSecond * DAMAGE_COOLDOWN);
    }

    public boolean takeDamage(float amount) {
        if (amount <= 0f || damageCooldownTimer > 0f || invulnerabilityTimer > 0f || isDead()) {
            lastDamageTaken = 0f;
            return false;
        }

        float previousHp = hp;
        hp = Math.max(0f, hp - (amount * stats.getIncomingDamageMultiplier()));
        lastDamageTaken = previousHp - hp;
        if (lastDamageTaken <= 0f) {
            return false;
        }
        damageCooldownTimer = DAMAGE_COOLDOWN;
        damageFlashTimer = DAMAGE_FLASH_DURATION;
        if (hp <= 0f) {
            setCurrentState(deadState);
        }
        return true;
    }

    public void reviveAt(float reviveX, float reviveY, float hpPercent) {
        x = MathUtils.clamp(reviveX, 0f, ARENA_WIDTH - SPRITE_WIDTH);
        y = MathUtils.clamp(reviveY, 0f, ARENA_HEIGHT - SPRITE_HEIGHT);
        velocityX = 0f;
        velocityY = 0f;
        hp = MathUtils.clamp(stats.getMaxHp() * hpPercent, 1f, stats.getMaxHp());
        damageCooldownTimer = 0f;
        damageFlashTimer = 0f;
        attackStateTimer = 0f;
        attackEffectTimer = 0f;
        dashCooldownTimer = 0f;
        dashStateTimer = 0f;
        setCurrentState(idleState);
        updateBounds();
    }

    public void grantInvulnerability(float duration) {
        if (duration <= 0f) {
            return;
        }

        invulnerabilityTimer = Math.max(invulnerabilityTimer, duration);
        damageFlashTimer = Math.max(damageFlashTimer, DAMAGE_FLASH_DURATION);
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

    public void addCoins(int amount) {
        if (amount > 0) {
            coins += amount;
        }
    }

    public boolean spendCoins(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (coins < amount) {
            return false;
        }

        coins -= amount;
        return true;
    }

    public float heal(float amount) {
        if (amount <= 0f || isDead()) {
            return 0f;
        }

        float previousHp = hp;
        hp = MathUtils.clamp(hp + amount, 0f, getMaxHp());
        return hp - previousHp;
    }

    public boolean tryRevive() {
        return tryReviveAt(x, y, 0.55f);
    }

    public boolean tryReviveAt(float reviveX, float reviveY, float hpPercent) {
        if (!spendCoins(REVIVE_COST)) {
            return false;
        }

        reviveUsed = true;
        reviveAt(reviveX, reviveY, hpPercent);
        grantInvulnerability(REVIVE_INVULNERABILITY_DURATION);
        return true;
    }

    public boolean canAffordRevive() {
        return coins >= REVIVE_COST;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public boolean isReviveUsed() {
        return reviveUsed;
    }

    public void setReviveUsed(boolean reviveUsed) {
        this.reviveUsed = reviveUsed;
    }

    public int getReviveCost() {
        return REVIVE_COST;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0f;
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

    public float getAttackReadyProgress() {
        float cooldown = stats.getAttackCooldown();
        if (cooldown <= 0f) {
            return 1f;
        }

        return MathUtils.clamp(1f - attackTimer / cooldown, 0f, 1f);
    }

    public float getDashReadyProgress() {
        if (DASH_COOLDOWN <= 0f) {
            return 1f;
        }

        return MathUtils.clamp(1f - dashCooldownTimer / DASH_COOLDOWN, 0f, 1f);
    }

    public boolean isDashing() {
        return dashStateTimer > 0f;
    }

    public float getLastDamageTaken() {
        return lastDamageTaken;
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && MathUtils.isZero(dashCooldownTimer)) {
            startDash(moveX, moveY);
        }

        if (dashStateTimer > 0f) {
            velocityX = dashDirectionX * DASH_SPEED;
            velocityY = dashDirectionY * DASH_SPEED;
            x += velocityX * delta;
            y += velocityY * delta;
            x = MathUtils.clamp(x, 0f, ARENA_WIDTH - SPRITE_WIDTH);
            y = MathUtils.clamp(y, 0f, ARENA_HEIGHT - SPRITE_HEIGHT);
            return;
        }

        velocityX = moveX * stats.getSpeed();
        velocityY = moveY * stats.getSpeed();

        x += velocityX * delta;
        y += velocityY * delta;

        x = MathUtils.clamp(x, 0f, ARENA_WIDTH - SPRITE_WIDTH);
        y = MathUtils.clamp(y, 0f, ARENA_HEIGHT - SPRITE_HEIGHT);
    }

    private void performAttack(List<Enemy> enemies) {
        SoundManager.getInstance().playAttack();
        attackStateTimer = ATTACK_STATE_DURATION;
        attackEffectTimer = ATTACK_EFFECT_DURATION;
        attackStartX = getCenterX();
        attackStartY = getCenterY();

        Enemy target = findClosestEnemyInRange(enemies);
        if (target != null) {
            attackEndX = target.getCenterX();
            attackEndY = target.getCenterY();
            boolean critical = MathUtils.random() < CRITICAL_CHANCE;
            float damage = getDamage() * (critical ? CRITICAL_DAMAGE_MULTIPLIER : 1f);
            target.takeDamage(damage, critical);
            return;
        }

        attackEndX = attackStartX + facingX * ATTACK_RADIUS * 0.75f;
        attackEndY = attackStartY + facingY * ATTACK_RADIUS * 0.75f;
    }

    private boolean isAttackPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.J) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
    }

    private void startDash(float moveX, float moveY) {
        if (MathUtils.isZero(moveX) && MathUtils.isZero(moveY)) {
            moveX = facingX;
            moveY = facingY;
        }
        if (MathUtils.isZero(moveX) && MathUtils.isZero(moveY)) {
            moveX = 1f;
        }

        float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        dashDirectionX = moveX / length;
        dashDirectionY = moveY / length;
        dashStateTimer = DASH_DURATION;
        dashCooldownTimer = DASH_COOLDOWN;
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

    private void updateDashCooldownTimer(float delta) {
        dashCooldownTimer -= delta;
        if (dashCooldownTimer < 0f) {
            dashCooldownTimer = 0f;
        }
    }

    private void updateDashStateTimer(float delta) {
        dashStateTimer -= delta;
        if (dashStateTimer < 0f) {
            dashStateTimer = 0f;
        }
    }

    private void updateDamageTimers(float delta) {
        damageCooldownTimer -= delta;
        if (damageCooldownTimer < 0f) {
            damageCooldownTimer = 0f;
        }

        damageFlashTimer -= delta;
        if (damageFlashTimer < 0f) {
            damageFlashTimer = 0f;
        }
    }

    private void updateInvulnerabilityTimer(float delta) {
        invulnerabilityTimer -= delta;
        if (invulnerabilityTimer < 0f) {
            invulnerabilityTimer = 0f;
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

            float targetX = enemy.getCenterX();
            float targetY = enemy.getCenterY();
            float distance = Vector2.dst(centerX, centerY, targetX, targetY);
            if (!isInsideAttackCone(targetX - centerX, targetY - centerY, distance)) {
                continue;
            }

            if (distance <= bestDistance) {
                bestDistance = distance;
                closest = enemy;
            }
        }

        return closest;
    }

    private boolean isInsideAttackCone(float targetX, float targetY, float distance) {
        if (distance > ATTACK_RADIUS) {
            return false;
        }
        if (distance <= 0.001f) {
            return true;
        }

        float dot = (targetX / distance) * facingX + (targetY / distance) * facingY;
        return dot >= ATTACK_CONE_COS;
    }
}
