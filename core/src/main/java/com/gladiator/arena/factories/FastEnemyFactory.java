package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.FastEnemy;

public class FastEnemyFactory extends EnemyFactory {
    @Override
    protected Enemy createEnemy(float x, float y) {
        return new FastEnemy(x, y);
    }
}
