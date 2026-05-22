package com.gladiator.arena.managers;

import java.util.ArrayDeque;
import java.util.Deque;

public class GameStateManager {
    public enum State {
        MENU,
        GAME,
        PAUSE,
        UPGRADE,
        GAME_OVER,
        VICTORY
    }

    private final Deque<State> stateStack = new ArrayDeque<>();

    public void set(State state) {
        stateStack.clear();
        stateStack.push(state);
    }

    public void push(State state) {
        stateStack.push(state);
    }

    public State pop() {
        if (stateStack.isEmpty()) {
            return null;
        }
        if (stateStack.size() == 1) {
            return stateStack.peek();
        }
        stateStack.pop();
        return stateStack.peek();
    }

    public State peek() {
        return stateStack.peek();
    }
}
