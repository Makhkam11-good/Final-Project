package com.gladiator.arena.decorator;

public class SpeedBootsDecorator extends PlayerDecorator {
    public SpeedBootsDecorator(PlayerStats wrapped) {
        super(wrapped);
    }

    @Override
    public float getSpeed() {
        return wrapped.getSpeed() * 1.25f;
    }
}
