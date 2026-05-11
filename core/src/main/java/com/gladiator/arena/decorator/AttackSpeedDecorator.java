package com.gladiator.arena.decorator;

public class AttackSpeedDecorator extends PlayerDecorator {
    public AttackSpeedDecorator(PlayerStats wrapped) {
        super(wrapped);
    }

    @Override
    public float getAttackCooldown() {
        return wrapped.getAttackCooldown() * 0.8f;
    }
}
