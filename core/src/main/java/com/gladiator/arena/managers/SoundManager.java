package com.gladiator.arena.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public final class SoundManager {
    private static final SoundManager INSTANCE = new SoundManager();
    private static final String GAMEPLAY_MUSIC_PATH = "music/gameplay.wav";
    private static final float DEFAULT_MUSIC_VOLUME = 0.40f;

    private final Map<String, Sound> sounds = new HashMap<>();
    private Music gameplayMusic;
    private float musicVolume = DEFAULT_MUSIC_VOLUME;
    private boolean musicLoaded;
    private boolean musicEnabled = true;
    private boolean loaded;

    private SoundManager() {
    }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    public void playAttack() {
        play("attack", 0.42f);
    }

    public void playEnemyHit() {
        play("enemy_hit", 0.45f);
    }

    public void playEnemyDeath() {
        play("enemy_death", 0.55f);
    }

    public void playBossPhase() {
        play("boss_phase", 0.72f);
    }

    public void playVictory() {
        play("victory", 0.70f);
    }

    public void playGameOver() {
        play("game_over", 0.68f);
    }

    public void startGameplayMusic() {
        loadGameplayMusic();
        if (!musicEnabled || gameplayMusic == null) {
            return;
        }

        gameplayMusic.setLooping(true);
        gameplayMusic.setVolume(musicVolume);
        if (!gameplayMusic.isPlaying()) {
            gameplayMusic.play();
        }
    }

    public void pauseGameplayMusic() {
        if (gameplayMusic != null && gameplayMusic.isPlaying()) {
            gameplayMusic.pause();
        }
    }

    public void toggleMusic(boolean playNow) {
        musicEnabled = !musicEnabled;
        if (musicEnabled) {
            if (playNow) {
                startGameplayMusic();
            }
        } else {
            pauseGameplayMusic();
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public String getMusicStatusText() {
        return "Music: " + (musicEnabled ? "ON" : "OFF");
    }

    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0f, Math.min(1f, volume));
        if (gameplayMusic != null) {
            gameplayMusic.setVolume(musicVolume);
        }
    }

    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
        if (gameplayMusic != null) {
            gameplayMusic.dispose();
            gameplayMusic = null;
        }
        loaded = false;
        musicLoaded = false;
    }

    private void play(String key, float volume) {
        loadSounds();
        Sound sound = sounds.get(key);
        if (sound != null) {
            sound.play(volume);
        }
    }

    private void loadSounds() {
        if (loaded) {
            return;
        }

        loaded = true;
        load("attack", "sounds/attack.wav");
        load("enemy_hit", "sounds/enemy_hit.wav");
        load("enemy_death", "sounds/enemy_death.wav");
        load("boss_phase", "sounds/boss_phase.wav");
        load("victory", "sounds/victory.wav");
        load("game_over", "sounds/game_over.wav");
    }

    private void load(String key, String path) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                sounds.put(key, Gdx.audio.newSound(file));
            }
        } catch (RuntimeException ignored) {
            // Missing or unsupported audio should never stop the game from running.
        }
    }

    private void loadGameplayMusic() {
        if (musicLoaded) {
            return;
        }

        musicLoaded = true;
        try {
            FileHandle file = Gdx.files.internal(GAMEPLAY_MUSIC_PATH);
            if (file.exists()) {
                gameplayMusic = Gdx.audio.newMusic(file);
                gameplayMusic.setLooping(true);
                gameplayMusic.setVolume(musicVolume);
            }
        } catch (RuntimeException ignored) {
            gameplayMusic = null;
            // Missing or unsupported music should never stop the game from running.
        }
    }
}
