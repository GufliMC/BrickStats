package com.guflimc.brick.stats.common;

import com.guflimc.brick.orm.api.database.DatabaseContext;
import com.guflimc.brick.scheduler.api.Scheduler;
import com.guflimc.brick.stats.api.StatsManager;
import com.guflimc.brick.stats.api.container.StatsContainer;
import com.guflimc.brick.stats.api.container.StatsRecord;
import com.guflimc.brick.stats.api.entity.RelationProvider;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.common.container.BrickStatsContainer;
import com.guflimc.brick.stats.common.domain.DStatsRecord;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class BrickStatsManager implements StatsManager {

    private final Map<UUID, BrickStatsContainer> containers = new ConcurrentHashMap<>();
    private final Set<RelationProvider> relationProviders = new CopyOnWriteArraySet<>();

    private final Queue<DStatsRecord> savingQueue = new ArrayDeque<>();

    private final DatabaseContext databaseContext;

    public BrickStatsManager(DatabaseContext databaseContext, Scheduler scheduler) {
        this.databaseContext = databaseContext;

        List<DStatsRecord> records = databaseContext.findAllAsync(DStatsRecord.class).join();
        Map<UUID, Collection<DStatsRecord>> mapped = new HashMap<>();
        records.forEach(r -> {
            mapped.computeIfAbsent(r.id(), (x) -> new ArrayList<>());
            mapped.get(r.id()).add(r);
        });

        mapped.keySet().forEach(id -> {
            containers.put(id, new BrickStatsContainer(id, List.of()));
        });

        scheduler.asyncRepeating(() -> save(30), 5, TimeUnit.SECONDS);
    }

    public void save(int max) {
        Set<Object> recordsToSave = new HashSet<>();
        for ( int i = 0; i < max; i++ ) {
            DStatsRecord record = savingQueue.poll();
            if ( record == null ) {
                break;
            }
            recordsToSave.add(record);
        }

        databaseContext.persistAsync(recordsToSave).join();
    }

    private BrickStatsContainer find(@NotNull UUID id) {
        containers.computeIfAbsent(id, (v) -> new BrickStatsContainer(id, List.of()));
        return containers.get(id);
    }

    @Override
    public int read(@NotNull UUID id, @NotNull StatsKey key) {
        return read(id, key, null);
    }

    @Override
    public int read(@NotNull UUID id, @NotNull StatsKey key, UUID relation) {
        return find(id).read(key, relation);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater) {
        update(id, key, null, updater);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull StatsKey key, UUID relation, @NotNull IntFunction<Integer> updater) {
        DStatsRecord rec = find(id).find(key, relation);
        int oldValue = rec.value();

        rec.setValue(updater.apply(rec.value()));
        savingQueue.add(rec);

        handleUpdate(id, key, oldValue, rec);

        if ( key.parent() != null ) {
            update(id, key.parent(), relation, updater);
        }

        if (relation != null) {
            return;
        }

        // also update relations
        relationProviders.stream()
                .map(r -> r.relation(id, key).orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .forEach(r -> update(r, key, id, updater));
    }

    @Override
    public StatsContainer readAll(@NotNull UUID id) {
        return find(id);
    }

    @Override
    public void register(@NotNull RelationProvider relationProvider) {
        relationProviders.add(relationProvider);
    }

    private void handleUpdate(@NotNull UUID id, @NotNull StatsKey key, int oldValue, StatsRecord record) {
        // TODO
    }

    @Override
    public void registerMilestoneListener(@NotNull StatsKey key, int milestone, @NotNull Consumer<StatsRecord> handler) {

    }

    @Override
    public void registerChangeListener(@NotNull StatsKey key, @NotNull BiConsumer<StatsRecord, Integer> handler) {

    }
}
