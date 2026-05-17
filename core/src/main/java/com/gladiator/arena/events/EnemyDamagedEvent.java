package com.gladiator.arena.events;

public final class EnemyDamagedEvent {
    private final float x;
    private final float y;
    private final float amount;

    public EnemyDamagedEvent(float x, float y, float amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAmount() {
        return amount;
    }
}
