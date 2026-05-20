package com.gladiator.arena.events;

public class GameEvent {
    public enum Type {
        ENEMY_DIED,
        ENEMY_DAMAGED,
        WAVE_CLEARED,
        PLAYER_HURT,
        PLAYER_DIED,
        BOSS_DIED,
        BOSS_PHASE_CHANGED
    }

    private final Type type;
    private final Object payload;

    public GameEvent(Type type) {
        this(type, null);
    }

    public GameEvent(Type type, Object payload) {
        if (type == null) {
            throw new IllegalArgumentException("GameEvent type cannot be null.");
        }
        this.type = type;
        this.payload = payload;
    }

    public Type getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
