package com.gladiator.arena.events;

@FunctionalInterface
public interface EventListener {
    void onEvent(GameEvent event);
}
