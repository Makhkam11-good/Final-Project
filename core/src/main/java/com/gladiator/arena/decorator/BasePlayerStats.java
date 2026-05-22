package com.gladiator.arena.decorator;

public class BasePlayerStats implements PlayerStats {
    @Override
    public float getMaxHp() {
        return 100f;
    }

    @Override
    public float getDamage() {
        return 10f;
    }

    @Override
    public float getSpeed() {
        return 150f;
    }

    @Override
    public float getAttackCooldown() {
        return 1.0f;
    }

    @Override
    public float getIncomingDamageMultiplier() {
        return 1.0f;
    }
}
