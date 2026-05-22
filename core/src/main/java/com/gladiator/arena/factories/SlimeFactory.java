package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.Slime;

public class SlimeFactory extends EnemyFactory {
    @Override
    protected Enemy createEnemy(float x, float y) {
        return new Slime(x, y);
    }
}
