package com.guflimc.brick.stats.api;

import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.container.Container;
import com.guflimc.brick.stats.api.container.Record;
import com.guflimc.brick.stats.api.event.SubscriptionBuilder;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.api.relation.RelationProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.IntFunction;

public interface StatsManager {

    Container find(@NotNull Actor... actors);

    Container find(@NotNull Actor.ActorSet actors);

    //

    /**
     * This will also update permutations of the actor set.
     */
    void update(@NotNull Actor.ActorSet actors, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater);

    /**
     * This will first find the full actor set by traversing relations and then {@link #update(Actor.ActorSet, StatsKey, IntFunction)}.
     */
    void update(@NotNull Actor actor, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater);

    //

    List<Record> select(@NotNull String actorType, @NotNull StatsKey key, int limit, boolean asc);

    default List<Record> select(@NotNull String actorType, @NotNull StatsKey key, int limit) {
        return select(actorType, key, limit, false);
    }

    default List<Record> select(@NotNull String actorType, @NotNull StatsKey key) {
        return select(actorType, key, Integer.MAX_VALUE);
    }

    //

    void registerRelations(@NotNull RelationProvider relationProvider);

    void unregisterRelations(@NotNull RelationProvider relationProvider);

    //

    SubscriptionBuilder subscribe();

}
