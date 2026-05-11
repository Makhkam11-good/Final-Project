package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Boss;
import com.gladiator.arena.entities.Enemy;

public class BossFactory extends EnemyFactory {
    @Override
    protected Enemy createEnemy(float x, float y) {
        return new Boss(x, y);
    }
}
