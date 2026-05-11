package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.Goblin;

public class GoblinFactory extends EnemyFactory {
    @Override
    protected Enemy createEnemy(float x, float y) {
        return new Goblin(x, y);
    }
}
