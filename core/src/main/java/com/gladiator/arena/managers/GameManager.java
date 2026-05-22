package com.gladiator.arena.managers;

import com.gladiator.arena.strategy.DifficultyStrategy;
import com.gladiator.arena.strategy.MediumDifficulty;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();

    private final GameStateManager gameStateManager = new GameStateManager();
    private DifficultyStrategy difficulty = new MediumDifficulty();
    private int enemiesKilled;
    private int coinsCollected;
    private int bestCombo;
    private int levelReached = 1;

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

    public void resetRunStats() {
        enemiesKilled = 0;
        coinsCollected = 0;
        bestCombo = 0;
        levelReached = 1;
    }

    public void recordEnemyKill() {
        enemiesKilled++;
    }

    public void recordCoinsCollected(int amount) {
        if (amount > 0) {
            coinsCollected += amount;
        }
    }

    public void recordBestCombo(int combo) {
        if (combo > bestCombo) {
            bestCombo = combo;
        }
    }

    public void recordLevelReached(int level) {
        if (level > levelReached) {
            levelReached = level;
        }
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public int getCoinsCollected() {
        return coinsCollected;
    }

    public int getBestCombo() {
        return bestCombo;
    }

    public int getLevelReached() {
        return levelReached;
    }
}
