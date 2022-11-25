package com.guflimc.brick.stats.api;

import com.guflimc.brick.stats.api.container.StatsContainer;
import com.guflimc.brick.stats.api.event.SubscriptionBuilder;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.api.relation.RelationProvider;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.IntFunction;

public interface StatsManager {

    int read(@NotNull UUID id, @NotNull StatsKey key);

    int read(@NotNull UUID id, UUID relation, @NotNull StatsKey key);

    void update(@NotNull UUID id, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater);

    void update(@NotNull UUID id, UUID relation, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater);

    default void write(@NotNull UUID id, @NotNull StatsKey key, int value) {
        write(id, null, key, value);
    }

    default void write(@NotNull UUID id, UUID relation, @NotNull StatsKey key, int value) {
        update(id, relation, key, ignored -> value);
    }

    StatsContainer readAll(@NotNull UUID id);

    void registerRelationProvider(@NotNull RelationProvider relationProvider);

    SubscriptionBuilder subscribe();

}
