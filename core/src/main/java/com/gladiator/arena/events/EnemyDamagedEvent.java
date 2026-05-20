package com.gladiator.arena.events;

public final class EnemyDamagedEvent {
    private final float x;
    private final float y;
    private final float amount;
    private final boolean critical;
    private final boolean boss;

    public EnemyDamagedEvent(float x, float y, float amount) {
        this(x, y, amount, false, false);
    }

    public EnemyDamagedEvent(float x, float y, float amount, boolean critical, boolean boss) {
        this.x = x;
        this.y = y;
        this.amount = amount;
        this.critical = critical;
        this.boss = boss;
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

    public boolean isCritical() {
        return critical;
    }

    public boolean isBoss() {
        return boss;
    }
}
