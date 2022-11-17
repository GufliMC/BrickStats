package com.guflimc.brick.stats.common.container;

import com.guflimc.brick.stats.api.container.StatsContainer;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.common.domain.DStatsRecord;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public class BrickStatsContainer implements StatsContainer {

    private final UUID id;
    private final Map<RecordKey, DStatsRecord> records = new ConcurrentHashMap<>();

    private record RecordKey(@NotNull String key, UUID relation) {}

    public BrickStatsContainer(UUID id, Collection<DStatsRecord> records) {
        this.id = id;
        for ( DStatsRecord rec : records ) {
            this.records.put(new RecordKey(rec.key(), rec.relation()), rec);
        }
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public int read(@NotNull StatsKey key) {
        return read(key, null);
    }

    @Override
    public int read(@NotNull StatsKey key, UUID relation) {
        RecordKey recKey = new RecordKey(key.name(), relation);
        return records.containsKey(recKey) ? records.get(recKey).value() : 0;
    }

    public DStatsRecord find(@NotNull StatsKey key) {
        return find(key, null);
    }

    public DStatsRecord find(@NotNull StatsKey key, UUID relation) {
        RecordKey recKey = new RecordKey(key.name(), relation);
        records.computeIfAbsent(recKey, k -> new DStatsRecord(id, key.name(), 0));
        return records.get(recKey);
    }
}
