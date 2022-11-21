package com.guflimc.brick.stats.common;

import com.guflimc.brick.orm.api.database.DatabaseContext;
import com.guflimc.brick.scheduler.api.Scheduler;
import com.guflimc.brick.stats.api.StatsManager;
import com.guflimc.brick.stats.api.container.StatsContainer;
import com.guflimc.brick.stats.api.container.StatsRecord;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.api.relation.RelationProvider;
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

            if ( r.relation() != null ) {
                mapped.computeIfAbsent(r.relation(), (x) -> new ArrayList<>());
                mapped.get(r.relation()).add(r);
            }
        });

        mapped.keySet().forEach(id -> {
            containers.put(id, new BrickStatsContainer(id, List.of()));
        });

        scheduler.asyncRepeating(() -> save(30), 5, TimeUnit.SECONDS);
    }

    public void save(int max) {
        Set<Object> recordsToSave = new HashSet<>();
        for (int i = 0; i < max; i++) {
            DStatsRecord record = savingQueue.poll();
            if (record == null) {
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
        return read(id, null, key);
    }

    @Override
    public int read(@NotNull UUID id, UUID relation, @NotNull StatsKey key) {
        return find(id).read(relation, key);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater) {
        update(id, null, key, updater);
    }

    @Override
    public void update(@NotNull UUID id, UUID relation, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater) {
        update(id, relation, key, updater, new HashSet<>());
    }

    public void update(@NotNull UUID id, UUID relation, @NotNull StatsKey key,
                       @NotNull IntFunction<Integer> updater, @NotNull Collection<UUID> passed) {
        DStatsRecord rec = find(id).find(relation, key);
        int oldValue = rec.value();

        rec.setValue(updater.apply(rec.value()));
        savingQueue.add(rec);

        handleUpdate(id, key, oldValue, rec);

        if (key.parent() != null) {
            update(id, relation, key.parent(), updater, new HashSet<>());
        }

        passed.add(id);

        // also update relations
        relationProviders.stream()
                .map(r -> r.relation(id, key).orElse(null))
                .filter(Objects::nonNull)
                .filter(r -> !passed.contains(r))
                .distinct()
                .forEach(r -> update(r, id, key, updater));
    }

    @Override
    public StatsContainer readAll(@NotNull UUID id) {
        return find(id);
    }

    @Override
    public void registerRelationProvider(@NotNull RelationProvider relationProvider) {
        relationProviders.add(relationProvider);
    }

    //

    private final Set<MilestoneListener> milestoneListeners = new HashSet<>();

    private record MilestoneListener(@NotNull StatsKey key, int milestone, @NotNull Consumer<StatsRecord> handler) {
    }

    private final Set<ChangeListener> changeListeners = new HashSet<>();

    private record ChangeListener(@NotNull StatsKey key, @NotNull BiConsumer<StatsRecord, Integer> handler) {
    }

    private final Set<ModuloListener> moduloListeners = new HashSet<>();

    private record ModuloListener(@NotNull StatsKey key, int divisor, @NotNull Consumer<StatsRecord> handler) {
    }

    private void handleUpdate(@NotNull UUID id, @NotNull StatsKey key, int oldValue, StatsRecord record) {
        milestoneListeners.stream().filter(ml -> ml.key.equals(key))
                .filter(ml -> oldValue < ml.milestone && record.value() > ml.milestone)
                .forEach(ml -> ml.handler.accept(record));

        changeListeners.stream().filter(cl -> cl.key.equals(key))
                .forEach(cl -> cl.handler.accept(record, oldValue));

        moduloListeners.stream().filter(ml -> ml.key.equals(key))
                .forEach(ml -> {
                    int oldAmount = oldValue / ml.divisor;
                    int newAmount = record.value() / ml.divisor;
                    int diff = newAmount - oldAmount;
                    if (diff < 0) {
                        return;
                    }

                    for (int i = 0; i < diff; i++) {
                        ml.handler.accept(record);
                    }
                });
    }

    @Override
    public void registerMilestoneListener(@NotNull StatsKey key, int milestone, @NotNull Consumer<StatsRecord> handler) {
        milestoneListeners.add(new MilestoneListener(key, milestone, handler));
    }

    @Override
    public void registerChangeListener(@NotNull StatsKey key, @NotNull BiConsumer<StatsRecord, Integer> handler) {
        changeListeners.add(new ChangeListener(key, handler));
    }

    @Override
    public void registerModuloListener(@NotNull StatsKey key, int divisor, @NotNull Consumer<StatsRecord> handler) {
        moduloListeners.add(new ModuloListener(key, divisor, handler));
    }
}
