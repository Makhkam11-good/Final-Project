package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gladiator.arena.events.EventBus;
import com.gladiator.arena.events.GameEvent;

public abstract class Enemy {
    protected static final float ARENA_WIDTH = 800f;
    protected static final float ARENA_HEIGHT = 480f;

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
        updateMovement(delta, player);
        clampToArena();
        updateBounds();

        if (bounds.overlaps(player.getBounds())) {
            player.takeDamage(damage * delta);
            EventBus.getInstance().post(GameEvent.Type.PLAYER_HURT);
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(renderColor);
        shapeRenderer.rect(x, y, spriteWidth, spriteHeight);
    }

    public void takeDamage(float amount) {
        hp = Math.max(0f, hp - amount);
    }

    public boolean isDead() {
        return hp <= 0f;
    }

    public int getScoreReward() {
        return scoreReward;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getCenterX() {
        return x + spriteWidth / 2f;
    }

    public float getCenterY() {
        return y + spriteHeight / 2f;
    }

    protected void moveToward(float targetX, float targetY, float delta, float moveSpeed) {
        Vector2 direction = new Vector2(targetX - getCenterX(), targetY - getCenterY());
        if (direction.isZero(0.001f)) {
            return;
        }

        direction.nor();
        x += direction.x * moveSpeed * delta;
        y += direction.y * moveSpeed * delta;
    }

    protected float distanceTo(Player player) {
        float playerCenterX = player.getX() + Player.SPRITE_WIDTH / 2f;
        float playerCenterY = player.getY() + Player.SPRITE_HEIGHT / 2f;
        return Vector2.dst(getCenterX(), getCenterY(), playerCenterX, playerCenterY);
    }

    protected void updateBounds() {
        bounds.set(x + hitboxOffsetX, y + hitboxOffsetY, hitboxWidth, hitboxHeight);
    }

    protected void clampToArena() {
        x = MathUtils.clamp(x, 0f, ARENA_WIDTH - spriteWidth);
        y = MathUtils.clamp(y, 0f, ARENA_HEIGHT - spriteHeight);
    }

    protected abstract void onWaveSpawn();

    protected abstract void updateMovement(float delta, Player player);
}
