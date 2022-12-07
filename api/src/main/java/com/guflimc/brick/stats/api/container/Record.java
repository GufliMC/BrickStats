package com.guflimc.brick.stats.api.container;

import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.key.StatsKey;

public interface Record {

    Actor.ActorSet actors();

    StatsKey key();

    int value();

}
