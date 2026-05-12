package com.gladiator.arena.ai;

import com.gladiator.arena.entities.Player;

public interface EnemyAi {
    void onSpawn();

    void update(float delta, Player player);
}
