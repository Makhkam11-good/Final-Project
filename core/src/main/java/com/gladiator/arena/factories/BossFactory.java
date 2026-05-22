package com.gladiator.arena.factories;

import com.gladiator.arena.entities.Boss;
import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.managers.GameManager;

public class BossFactory extends EnemyFactory {
    @Override
    protected Enemy createEnemy(float x, float y) {
        Boss boss = new Boss(x, y);
        boss.setHp(GameManager.getInstance().getDifficulty().getBossHp());
        return boss;
    }
}
