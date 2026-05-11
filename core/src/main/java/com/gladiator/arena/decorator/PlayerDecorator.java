package com.gladiator.arena.decorator;

public abstract class PlayerDecorator implements PlayerStats {
    protected final PlayerStats wrapped;

    protected PlayerDecorator(PlayerStats wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public float getMaxHp() {
        return wrapped.getMaxHp();
    }

    @Override
    public float getDamage() {
        return wrapped.getDamage();
    }

    @Override
    public float getSpeed() {
        return wrapped.getSpeed();
    }

    @Override
    public float getAttackCooldown() {
        return wrapped.getAttackCooldown();
    }

    @Override
    public float getIncomingDamageMultiplier() {
        return wrapped.getIncomingDamageMultiplier();
    }
}
