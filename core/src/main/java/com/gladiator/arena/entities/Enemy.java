package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gladiator.arena.events.EnemyDamagedEvent;
import com.gladiator.arena.events.EventBus;
import com.gladiator.arena.events.GameEvent;
import com.gladiator.arena.managers.AssetManager;

public abstract class Enemy {
    protected static final float ARENA_WIDTH = 800f;
    protected static final float ARENA_HEIGHT = 480f;
    private static final float HIT_FLASH_DURATION = 0.12f;
    private static final float FALLBACK_DEATH_DURATION = 0.45f;
    private static final Color HIT_FLASH_COLOR = new Color(1f, 0.88f, 0.42f, 1f);

    protected final Rectangle bounds = new Rectangle();
    protected float x;
    protected float y;
    protected float hp;
    protected float maxHp;
    protected float damage;
    protected float speed;
    protected float spriteWidth;
    protected float spriteHeight;
    protected float hitboxOffsetX;
    protected float hitboxOffsetY;
    protected float hitboxWidth;
    protected float hitboxHeight;
    protected int scoreReward;
    protected Color renderColor;
    protected float stateTime;
    private float deathTimer;
    private boolean dying;
    private float hitFlashTimer;

    protected Enemy(
        float x,
        float y,
        float hp,
        float damage,
        float speed,
        float spriteWidth,
        float spriteHeight,
        float hitboxOffsetX,
        float hitboxOffsetY,
        float hitboxWidth,
        float hitboxHeight,
        int scoreReward,
        Color renderColor
    ) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.damage = damage;
        this.speed = speed;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.hitboxOffsetX = hitboxOffsetX;
        this.hitboxOffsetY = hitboxOffsetY;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.scoreReward = scoreReward;
        this.renderColor = renderColor;
        updateBounds();
    }

    public final void waveSpawn() {
        onWaveSpawn();
    }

    public void update(float delta, Player player) {
        stateTime += delta;
        updateHitFlash(delta);
        if (dying) {
            updateDeathTimer(delta);
            return;
        }

        updateMovement(delta, player);
        clampToArena();
        updateBounds();

        if (bounds.overlaps(player.getBounds()) && player.takeContactDamage(damage)) {
            EventBus.getInstance().post(GameEvent.Type.PLAYER_HURT);
        }
    }

    public void render(SpriteBatch batch, AssetManager assets) {
        String animationState = isDead() ? "dead" : "run";
        assets.drawAnimation(
            batch,
            getSpriteKey() + "." + animationState,
            stateTime,
            x,
            y,
            spriteWidth,
            spriteHeight,
            hitFlashTimer > 0f ? HIT_FLASH_COLOR : null
        );
    }

    public void takeDamage(float amount) {
        if (amount <= 0f || dying) {
            return;
        }

        float previousHp = hp;
        hp = Math.max(0f, hp - amount);
        float actualDamage = previousHp - hp;
        hitFlashTimer = HIT_FLASH_DURATION;
        if (actualDamage > 0f) {
            EventBus.getInstance().post(new GameEvent(
                GameEvent.Type.ENEMY_DAMAGED,
                new EnemyDamagedEvent(getCenterX(), y + spriteHeight, actualDamage)
            ));
        }
        if (hp <= 0f) {
            startDying();
        }
    }

    public boolean isDead() {
        return dying;
    }

    public boolean isReadyToRemove() {
        return dying && deathTimer <= 0f;
    }

    public int getScoreReward() {
        return scoreReward;
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getSpeed() {
        return speed;
    }

    public void applyDifficulty(float speedMultiplier, float damageMultiplier) {
        speed *= speedMultiplier;
        damage *= damageMultiplier;
    }

    public void setHp(float hp) {
        this.hp = hp;
        this.maxHp = hp;
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
        return x + spriteWidth / 2f;
    }

    public float getCenterY() {
        return y + spriteHeight / 2f;
    }

    public float getSpriteWidth() {
        return spriteWidth;
    }

    public float getSpriteHeight() {
        return spriteHeight;
    }

    public void moveToward(float targetX, float targetY, float delta, float moveSpeed) {
        Vector2 direction = new Vector2(targetX - getCenterX(), targetY - getCenterY());
        if (direction.isZero(0.001f)) {
            return;
        }

        direction.nor();
        x += direction.x * moveSpeed * delta;
        y += direction.y * moveSpeed * delta;
    }

    public void moveBy(float deltaX, float deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public float distanceTo(Player player) {
        return Vector2.dst(getCenterX(), getCenterY(), player.getCenterX(), player.getCenterY());
    }

    public boolean isTouchingArenaEdge() {
        return x <= 0f || x >= ARENA_WIDTH - spriteWidth || y <= 0f || y >= ARENA_HEIGHT - spriteHeight;
    }

    protected void updateBounds() {
        bounds.set(x + hitboxOffsetX, y + hitboxOffsetY, hitboxWidth, hitboxHeight);
    }

    protected void clampToArena() {
        x = MathUtils.clamp(x, 0f, ARENA_WIDTH - spriteWidth);
        y = MathUtils.clamp(y, 0f, ARENA_HEIGHT - spriteHeight);
    }

    protected void updateHitFlash(float delta) {
        hitFlashTimer -= delta;
        if (hitFlashTimer < 0f) {
            hitFlashTimer = 0f;
        }
    }

    protected void updateDeathTimer(float delta) {
        deathTimer -= delta;
        if (deathTimer < 0f) {
            deathTimer = 0f;
        }
    }

    private void startDying() {
        dying = true;
        hitFlashTimer = 0f;
        stateTime = 0f;
        deathTimer = AssetManager.getInstance().getAnimationDuration(
            getSpriteKey() + ".dead",
            FALLBACK_DEATH_DURATION
        );
    }

    protected abstract void onWaveSpawn();

    protected abstract void updateMovement(float delta, Player player);

    protected abstract String getSpriteKey();
}
