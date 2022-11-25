package com.guflimc.brick.stats.common;

import com.guflimc.brick.orm.api.database.DatabaseContext;
import com.guflimc.brick.scheduler.api.Scheduler;
import com.guflimc.brick.stats.api.StatsManager;
import com.guflimc.brick.stats.api.container.StatsContainer;
import com.guflimc.brick.stats.api.container.StatsRecord;
import com.guflimc.brick.stats.api.event.*;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.api.relation.RelationProvider;
import com.guflimc.brick.stats.common.container.BrickStatsContainer;
import com.guflimc.brick.stats.common.domain.DStatsRecord;
import com.guflimc.brick.stats.common.event.AbstractSubscriptionBuilder;
import com.guflimc.brick.stats.common.event.subscriptions.AbstractSubscription;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
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

            if (r.relation() != null) {
                mapped.computeIfAbsent(r.relation(), (x) -> new ArrayList<>());
                mapped.get(r.relation()).add(r);
            }
        });

        mapped.keySet().forEach(id -> {
            containers.put(id, new BrickStatsContainer(id, mapped.get(id)));
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
        int previousValue = rec.value();

        rec.setValue(updater.apply(rec.value()));
        savingQueue.add(rec);

        handleUpdate(key, rec, previousValue);

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

    private final Set<BrickSubscription> subscriptions = new HashSet<>();

    private void handleUpdate(@NotNull StatsKey key, @NotNull StatsRecord record, int previousValue) {
        Event event = new Event(key, record, previousValue);
        new HashSet<>(subscriptions).forEach(sub -> sub.execute(event));
    }

    @Override
    public SubscriptionBuilder subscribe() {
        return new BrickSubscriptionBuilder();
    }

    //

    private class BrickSubscription extends AbstractSubscription {

        public BrickSubscription(@NotNull EventHandler handler, @NotNull Filter filter) {
            super(handler, filter);
        }

        @Override
        public void unsubscribe() {
            subscriptions.remove(this);
        }
    }

    private class BrickSubscriptionBuilder extends AbstractSubscriptionBuilder {

        private Subscription subscribe(@NotNull Filter extraFilter) {
            Filter combined = event -> filter.test(event) && extraFilter.test(event);
            return subscribe(new BrickSubscription(handler, combined));
        }

        private Subscription subscribe(@NotNull BrickSubscription sub) {
            subscriptions.add(sub);
            return sub;
        }

        //

        @Override
        public Subscription milestone(int milestone) {
            return subscribe(event -> event.previousValue() < milestone
                    && event.record().value() > milestone);
        }

        @Override
        public Subscription change() {
            return subscribe(event -> true);
        }

        @Override
        public Subscription interval(int interval) {
            return subscribe(new BrickSubscription(handler, filter) {
                @Override
                public void execute(Event event) {
                    if (!filter.test(event)) {
                        return;
                    }

                    int oldAmount = event.previousValue() / interval;
                    int newAmount = event.record().value() / interval;
                    int diff = newAmount - oldAmount;
                    if (diff < 0) {
                        return;
                    }

                    for (int i = 0; i < diff; i++) {
                        handler.handle(this, event);
                    }
                }
            });
        }

    }
}
