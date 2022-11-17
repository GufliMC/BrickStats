package com.guflimc.brick.stats.common.container;

import com.guflimc.brick.stats.api.container.StatsContainer;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.common.domain.DStatsRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BrickStatsContainer implements StatsContainer {

    private final UUID id;
    private final Map<RecordKey, DStatsRecord> records = new ConcurrentHashMap<>();

    private record RecordKey(@NotNull String key, UUID relation) {
    }

    public BrickStatsContainer(UUID id, Collection<DStatsRecord> records) {
        this.id = id;
        for (DStatsRecord rec : records) {
            if ( rec.id().equals(id) ) {
                this.records.put(new RecordKey(rec.key(), rec.relation()), rec);
            } else {
                this.records.put(new RecordKey(rec.key(), rec.id()), rec);
            }
        }
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public int read(@NotNull StatsKey key) {
        return read(null, key);
    }

    @Override
    public int read(UUID relation, @NotNull StatsKey key) {
        RecordKey recKey = new RecordKey(key.name(), relation);
        return records.containsKey(recKey) ? records.get(recKey).value() : 0;
    }

    public DStatsRecord find(@NotNull StatsKey key) {
        return find(null, key);
    }

    public DStatsRecord find(UUID relation, @NotNull StatsKey key) {
        RecordKey recKey = new RecordKey(key.name(), relation);
        records.computeIfAbsent(recKey, k -> new DStatsRecord(id, key.name(), 0));
        return records.get(recKey);
    }
}
