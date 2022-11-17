package com.guflimc.brick.stats.api;

import com.guflimc.brick.stats.api.container.StatsContainer;
import com.guflimc.brick.stats.api.container.StatsRecord;
import com.guflimc.brick.stats.api.entity.RelationProvider;
import com.guflimc.brick.stats.api.key.StatsKey;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public interface StatsManager {

    int read(@NotNull UUID id, @NotNull StatsKey key);

    int read(@NotNull UUID id, @NotNull StatsKey key, UUID relation);

    void update(@NotNull UUID id, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater);

    void update(@NotNull UUID id, @NotNull StatsKey key, UUID relation, @NotNull IntFunction<Integer> updater);

    default void write(@NotNull UUID id, @NotNull StatsKey key, int value) {
        write(id, key, null, value);
    }

    default void write(@NotNull UUID id, @NotNull StatsKey key, UUID relation, int value) {
        update(id, key, relation, ignored -> value);
    }

    StatsContainer readAll(@NotNull UUID id);

    void register(@NotNull RelationProvider relationProvider);

    void registerMilestoneListener(@NotNull StatsKey key, int milestone, @NotNull Consumer<StatsRecord> handler);

    void registerChangeListener(@NotNull StatsKey key, @NotNull BiConsumer<StatsRecord, Integer> handler);

}
