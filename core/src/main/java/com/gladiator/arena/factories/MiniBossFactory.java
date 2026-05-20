package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.MiniBoss;

public class MiniBossFactory extends EnemyFactory {
    @Override
    protected Enemy createEnemy(float x, float y) {
        return new MiniBoss(x, y);
    }
}
