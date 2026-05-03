package com.gladiator.arena.managers;

import com.gladiator.arena.strategy.DifficultyStrategy;
import com.gladiator.arena.strategy.MediumDifficulty;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();

    private final GameStateManager gameStateManager = new GameStateManager();
    private DifficultyStrategy difficulty = new MediumDifficulty();

    private GameManager() {
    }

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public DifficultyStrategy getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyStrategy difficulty) {
        if (difficulty == null) {
            return;
        }
        this.difficulty = difficulty;
    }

    public String getDifficultyName() {
        String className = difficulty.getClass().getSimpleName();
        return className.replace("Difficulty", "");
    }
}
