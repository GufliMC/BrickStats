package com.guflimc.brick.stats.api;

import com.guflimc.brick.stats.api.container.StatsContainer;
import com.guflimc.brick.stats.api.container.StatsRecord;
import com.guflimc.brick.stats.api.relation.RelationProvider;
import com.guflimc.brick.stats.api.key.StatsKey;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

    void registerMilestoneListener(@NotNull StatsKey key, int milestone, @NotNull Consumer<StatsRecord> handler);

    void registerChangeListener(@NotNull StatsKey key, @NotNull BiConsumer<StatsRecord, Integer> handler);

    void registerIntervalListener(@NotNull StatsKey key, int interval, @NotNull Consumer<StatsRecord> handler);

}
