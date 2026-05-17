package com.gladiator.arena.entities;

import com.badlogic.gdx.graphics.Color;
import com.gladiator.arena.ai.BossState;
import com.gladiator.arena.ai.ChaseBossState;
import com.gladiator.arena.ai.DashBossState;
import com.gladiator.arena.ai.IdleBossState;
import com.gladiator.arena.ai.TelegraphBossState;
import com.gladiator.arena.events.EventBus;
import com.gladiator.arena.events.GameEvent;

public class Boss extends Enemy {
    public static final float SPRITE_WIDTH = 96f;
    public static final float SPRITE_HEIGHT = 96f;
    private static final float DEFAULT_HP = 500f;
    private static final float DASH_DAMAGE = 40f;
    private static final float CHASE_SPEED = 80f;
    private static final float DASH_SPEED_MULTIPLIER = 5f;
    private static final float HITBOX_OFFSET_X = 12f;
    private static final float HITBOX_OFFSET_Y = 8f;
    private static final float HITBOX_WIDTH = 72f;
    private static final float HITBOX_HEIGHT = 80f;
    private static final int SCORE_REWARD = 1000;
    private static final Color DARK_RED = new Color(0.45f, 0f, 0f, 1f);

    private final BossState idleState;
    private final BossState chaseState;
    private final BossState telegraphState;
    private final BossState dashState;
    private BossState currentState;
    private Player targetPlayer;
    private float preparedDashX = 1f;
    private float preparedDashY;
    private boolean dashTelegraphVisible;

    public Boss(float x, float y) {
        super(
            x,
            y,
            DEFAULT_HP,
            DASH_DAMAGE,
            CHASE_SPEED,
            SPRITE_WIDTH,
            SPRITE_HEIGHT,
            HITBOX_OFFSET_X,
            HITBOX_OFFSET_Y,
            HITBOX_WIDTH,
            HITBOX_HEIGHT,
            SCORE_REWARD,
            DARK_RED
        );
        idleState = new IdleBossState(this);
        chaseState = new ChaseBossState(this);
        telegraphState = new TelegraphBossState(this);
        dashState = new DashBossState(this);
        changeState(idleState);
    }

    @Override
    public void update(float delta, Player player) {
        stateTime += delta;
        updateHitFlash(delta);
        if (isDead()) {
            updateDeathTimer(delta);
            return;
        }
        if (player == null) {
            return;
        }

        targetPlayer = player;
        float playerX = player.getCenterX();
        float playerY = player.getCenterY();
        currentState.update(delta, playerX, playerY);
        finishStateStep();
    }

    public void changeState(BossState nextState) {
        if (nextState == null || currentState == nextState) {
            return;
        }

        if (currentState != null) {
            currentState.exit();
        }
        currentState = nextState;
        currentState.enter();
    }

    public BossState getIdleState() {
        return idleState;
    }

    public BossState getChaseState() {
        return chaseState;
    }

    public BossState getDashState() {
        return dashState;
    }

    public BossState getTelegraphState() {
        return telegraphState;
    }

    public float getChaseSpeed() {
        return speed;
    }

    public float getDashSpeed() {
        return speed * DASH_SPEED_MULTIPLIER;
    }

    public float getDashDamage() {
        return damage;
    }

    public void setPreparedDashDirection(float directionX, float directionY) {
        preparedDashX = directionX;
        preparedDashY = directionY;
    }

    public float getPreparedDashX() {
        return preparedDashX;
    }

    public float getPreparedDashY() {
        return preparedDashY;
    }

    public boolean isDashTelegraphVisible() {
        return dashTelegraphVisible;
    }

    public void setDashTelegraphVisible(boolean dashTelegraphVisible) {
        this.dashTelegraphVisible = dashTelegraphVisible;
    }

    public void clearDashTelegraph() {
        dashTelegraphVisible = false;
    }

    public void moveTowardTarget(float targetX, float targetY, float delta, float moveSpeed) {
        moveToward(targetX, targetY, delta, moveSpeed);
    }

    public void finishStateStep() {
        clampToArena();
        updateBounds();
    }

    public boolean overlapsTargetPlayer() {
        return targetPlayer != null && bounds.overlaps(targetPlayer.getBounds());
    }

    public void damageTargetPlayer(float amount) {
        if (targetPlayer == null) {
            return;
        }

        if (targetPlayer.takeDamage(amount)) {
            EventBus.getInstance().post(GameEvent.Type.PLAYER_HURT);
        }
    }

    @Override
    protected void onWaveSpawn() {
        clearDashTelegraph();
        changeState(idleState);
    }

    @Override
    protected void updateMovement(float delta, Player player) {
    }

    @Override
    protected String getSpriteKey() {
        return "boss";
    }
}
