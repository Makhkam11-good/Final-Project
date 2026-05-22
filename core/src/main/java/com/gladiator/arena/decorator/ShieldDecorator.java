package com.gladiator.arena.decorator;

public class ShieldDecorator extends PlayerDecorator {
    public ShieldDecorator(PlayerStats wrapped) {
        super(wrapped);
    }

    @Override
    public float getMaxHp() {
        return wrapped.getMaxHp() + 30f;
    }
}
