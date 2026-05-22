package com.gladiator.arena.ai;

import com.gladiator.arena.entities.Enemy;
import com.gladiator.arena.entities.Player;

public class GoblinAi implements EnemyAi {
    private final Enemy enemy;
    private boolean aggressive;

    public GoblinAi(Enemy enemy) {
        this.enemy = enemy;
    }

    @Override
    public void onSpawn() {
        aggressive = true;
    }

    @Override
    public void update(float delta, Player player) {
        if (!aggressive || player == null) {
            return;
        }

        enemy.moveToward(player.getCenterX(), player.getCenterY(), delta, enemy.getSpeed());
    }
}
