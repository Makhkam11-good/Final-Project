package com.gladiator.arena.decorator;

public class ArmorDecorator extends PlayerDecorator {
    public ArmorDecorator(PlayerStats wrapped) {
        super(wrapped);
    }

    @Override
    public float getIncomingDamageMultiplier() {
        return wrapped.getIncomingDamageMultiplier() * 0.8f;
    }
}
