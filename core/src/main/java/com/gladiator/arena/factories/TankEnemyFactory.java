package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.TankEnemy;

public class TankEnemyFactory extends EnemyFactory {
    @Override
    protected Enemy createEnemy(float x, float y) {
        return new TankEnemy(x, y);
    }
}
