package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.RangedEnemy;

public class RangedEnemyFactory extends EnemyFactory {
    @Override
    protected Enemy createEnemy(float x, float y) {
        return new RangedEnemy(x, y);
    }
}
