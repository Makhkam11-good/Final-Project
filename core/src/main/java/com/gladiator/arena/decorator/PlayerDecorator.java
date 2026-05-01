package com.gladiator.arena.decorator;

public abstract class PlayerDecorator implements PlayerStats {
    protected final PlayerStats wrapped;

    protected PlayerDecorator(PlayerStats wrapped) {
        this.wrapped = wrapped;
    }
}
