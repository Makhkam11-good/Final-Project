package com.gladiator.arena.events;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    private final Map<GameEvent.Type, List<EventListener>> listeners = new EnumMap<>(GameEvent.Type.class);

    private EventBus() {
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public void subscribe(GameEvent.Type type, EventListener listener) {
        if (type == null || listener == null) {
            return;
        }

        List<EventListener> typeListeners = listeners.computeIfAbsent(type, key -> new ArrayList<>());
        if (!typeListeners.contains(listener)) {
            typeListeners.add(listener);
        }
    }

    public void unsubscribe(GameEvent.Type type, EventListener listener) {
        if (type == null || listener == null) {
            return;
        }

        List<EventListener> typeListeners = listeners.get(type);
        if (typeListeners == null) {
            return;
        }

        typeListeners.remove(listener);
        if (typeListeners.isEmpty()) {
            listeners.remove(type);
        }
    }

    public void post(GameEvent.Type type) {
        post(new GameEvent(type));
    }

    public void post(GameEvent event) {
        if (event == null) {
            return;
        }

        List<EventListener> typeListeners = listeners.get(event.getType());
        if (typeListeners == null || typeListeners.isEmpty()) {
            return;
        }

        List<EventListener> snapshot = new ArrayList<>(typeListeners);
        for (EventListener listener : snapshot) {
            listener.onEvent(event);
        }
    }
}
