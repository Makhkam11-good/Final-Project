package com.gladiator.arena.managers;

import com.gladiator.arena.events.EventBus;
import com.gladiator.arena.events.EventListener;
import com.gladiator.arena.events.GameEvent;

public class LevelManager {
    public static final class WaveSummary {
        private final int waveNumber;
        private final int enemiesKilled;

        private WaveSummary(int waveNumber, int enemiesKilled) {
            this.waveNumber = waveNumber;
            this.enemiesKilled = enemiesKilled;
        }

        public int getWaveNumber() {
            return waveNumber;
        }

        public int getEnemiesKilled() {
            return enemiesKilled;
        }
    }

    private final EventBus eventBus;
    private final EventListener enemyDiedListener;

    private int currentWave;
    private int enemiesAlive;
    private int enemiesKilledThisWave;
    private WaveSummary currentSummary;
    private boolean waveActive;
    private boolean disposed;

    public LevelManager() {
        this(EventBus.getInstance());
    }

    public LevelManager(EventBus eventBus) {
        this.eventBus = eventBus;
        enemyDiedListener = this::handleEnemyDied;
        eventBus.subscribe(GameEvent.Type.ENEMY_DIED, enemyDiedListener);
    }

    public void startWave(int waveNumber, int enemyCount) {
        if (waveNumber < 1) {
            throw new IllegalArgumentException("Wave number must be 1 or greater.");
        }
        if (enemyCount < 0) {
            throw new IllegalArgumentException("Enemy count cannot be negative.");
        }

        currentWave = waveNumber;
        enemiesAlive = enemyCount;
        enemiesKilledThisWave = 0;
        currentSummary = new WaveSummary(currentWave, enemiesKilledThisWave);
        waveActive = enemyCount > 0;

        if (enemyCount == 0) {
            postWaveCleared();
        }
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getEnemiesAlive() {
        return enemiesAlive;
    }

    public int getEnemiesKilledThisWave() {
        return enemiesKilledThisWave;
    }

    public int getEnemiesTotalThisWave() {
        return enemiesAlive + enemiesKilledThisWave;
    }

    public float getWaveProgress() {
        int totalEnemies = getEnemiesTotalThisWave();
        if (totalEnemies <= 0) {
            return 1f;
        }

        return enemiesKilledThisWave / (float) totalEnemies;
    }

    public boolean isWaveActive() {
        return waveActive;
    }

    public WaveSummary getCurrentSummary() {
        return currentSummary;
    }

    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        eventBus.unsubscribe(GameEvent.Type.ENEMY_DIED, enemyDiedListener);
    }

    private void handleEnemyDied(GameEvent event) {
        if (!waveActive || enemiesAlive <= 0) {
            return;
        }

        enemiesAlive--;
        enemiesKilledThisWave++;

        if (enemiesAlive == 0) {
            waveActive = false;
            postWaveCleared();
        }
    }

    private void postWaveCleared() {
        eventBus.post(new GameEvent(
            GameEvent.Type.WAVE_CLEARED,
            currentSummary = new WaveSummary(currentWave, enemiesKilledThisWave)
        ));
    }
}
