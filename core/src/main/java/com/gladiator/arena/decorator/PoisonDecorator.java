package com.gladiator.arena.decorator;

public class PoisonDecorator extends PlayerDecorator {
    public PoisonDecorator(PlayerStats wrapped) {
        super(wrapped);
    }
}
