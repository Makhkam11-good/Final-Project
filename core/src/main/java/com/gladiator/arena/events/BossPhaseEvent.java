package com.gladiator.arena.events;

public final class BossPhaseEvent {
    private final int phase;
    private final String message;

    public BossPhaseEvent(int phase, String message) {
        this.phase = phase;
        this.message = message;
    }

    public int getPhase() {
        return phase;
    }

    public String getMessage() {
        return message;
    }
}
