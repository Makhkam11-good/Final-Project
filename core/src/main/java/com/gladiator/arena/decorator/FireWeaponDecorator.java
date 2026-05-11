package com.gladiator.arena.decorator;

public class FireWeaponDecorator extends PlayerDecorator {
    public FireWeaponDecorator(PlayerStats wrapped) {
        super(wrapped);
    }

    @Override
    public float getDamage() {
        return wrapped.getDamage() + 15f;
    }
}
