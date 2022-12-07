package com.guflimc.brick.stats.api.container;

import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.key.StatsKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntFunction;

public interface Container {

    Actor.ActorSet actors();

    Collection<StatsKey> stats();

    //

    Optional<Record> find(@NotNull StatsKey key);

    int read(@NotNull StatsKey key);

    void update(@NotNull StatsKey key, @NotNull IntFunction<Integer> updater);

    default void write(@NotNull StatsKey key, int value) {
        update(key, i -> value);
    }

}
