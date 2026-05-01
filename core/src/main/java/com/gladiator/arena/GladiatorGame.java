package com.gladiator.arena;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.ScreenUtils;

public class GladiatorGame extends Game {
    @Override
    public void create() {
        // Phase 1 keeps the app boot skeleton minimal.
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        super.render();
    }
}
