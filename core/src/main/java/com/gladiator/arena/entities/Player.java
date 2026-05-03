package com.gladiator.arena.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.gladiator.arena.entities.states.AttackState;
import com.gladiator.arena.entities.states.DeadState;
import com.gladiator.arena.entities.states.IdleState;
import com.gladiator.arena.entities.states.PlayerState;
import com.gladiator.arena.entities.states.RunState;

public class Player {
    public static final float SPRITE_WIDTH = 48f;
    public static final float SPRITE_HEIGHT = 48f;
    private static final float HITBOX_OFFSET_X = 8f;
    private static final float HITBOX_OFFSET_Y = 4f;
    private static final float HITBOX_WIDTH = 32f;
    private static final float HITBOX_HEIGHT = 40f;
    private static final float ARENA_WIDTH = 800f;
    private static final float ARENA_HEIGHT = 480f;
    private static final float BASE_MOVE_SPEED = 150f;
    private static final float ATTACK_COOLDOWN_SECONDS = 1.0f;
    private static final float ATTACK_STATE_DURATION = 0.12f;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float hp;
    private float maxHp;
    private float attackTimer;
    private float attackStateTimer;

    private final Rectangle bounds = new Rectangle();
    private final PlayerState idleState = new IdleState();
    private final PlayerState runState = new RunState();
    private final PlayerState attackState = new AttackState();
    private final PlayerState deadState = new DeadState();
    private PlayerState currentState;

    public Player() {
        x = (ARENA_WIDTH - SPRITE_WIDTH) / 2f;
        y = (ARENA_HEIGHT - SPRITE_HEIGHT) / 2f;
        maxHp = 100f;
        hp = maxHp;
        attackTimer = ATTACK_COOLDOWN_SECONDS;
        currentState = idleState;
        updateBounds();
    }

    public void update(float delta) {
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
            attackTimer = ATTACK_COOLDOWN_SECONDS;
            performAttack();
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
        hp = Math.max(0f, hp - amount);
        if (hp <= 0f) {
            currentState = deadState;
        }
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
        return maxHp;
    }

    public PlayerState getCurrentState() {
        return currentState;
    }

    public float getAttackTimer() {
        return attackTimer;
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

        velocityX = moveX * BASE_MOVE_SPEED;
        velocityY = moveY * BASE_MOVE_SPEED;

        x += velocityX * delta;
        y += velocityY * delta;

        x = MathUtils.clamp(x, 0f, ARENA_WIDTH - SPRITE_WIDTH);
        y = MathUtils.clamp(y, 0f, ARENA_HEIGHT - SPRITE_HEIGHT);
    }

    private void performAttack() {
        attackStateTimer = ATTACK_STATE_DURATION;
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
}
