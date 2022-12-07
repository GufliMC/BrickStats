package com.guflimc.brick.stats.common;

import com.guflimc.brick.orm.api.database.DatabaseContext;
import com.guflimc.brick.scheduler.api.Scheduler;
import com.guflimc.brick.stats.api.StatsManager;
import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.container.Container;
import com.guflimc.brick.stats.api.container.Record;
import com.guflimc.brick.stats.api.event.*;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.api.relation.RelationProvider;
import com.guflimc.brick.stats.common.container.BrickStatsContainer;
import com.guflimc.brick.stats.common.domain.DActor;
import com.guflimc.brick.stats.common.domain.DRecord;
import com.guflimc.brick.stats.common.event.AbstractSubscriptionBuilder;
import com.guflimc.brick.stats.common.event.subscriptions.AbstractSubscription;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class BrickStatsManager implements StatsManager {

    private final DatabaseContext databaseContext;

    private final Queue<DRecord> savingQueue = new ArrayDeque<>();

    private final Map<Actor.TempActor, DActor> actors = new ConcurrentHashMap<>();
    private final Map<Actor.ActorSet, BrickStatsContainer> containers = new ConcurrentHashMap<>();

    private final Set<BrickSubscription> subscriptions = new HashSet<>();

    private final Set<RelationProvider> relationProviders = new CopyOnWriteArraySet<>();

    public BrickStatsManager(DatabaseContext databaseContext, Scheduler scheduler) {
        this.databaseContext = databaseContext;

        // load actors
        databaseContext.findAllAsync(DActor.class).join()
                .forEach(actor -> actors.put(new Actor.TempActor(actor.id(), actor.type()), actor));

        // load records
        databaseContext.findAllAsync(DRecord.class).join()
                .forEach(record -> containers.computeIfAbsent(record.actors(),
                                a -> new BrickStatsContainer(this, a))
                        .add(record));

        // save task
        scheduler.asyncRepeating(() -> save(30), 5, TimeUnit.SECONDS);

        // info task
        scheduler.asyncRepeating(() -> {
            System.out.println(" ----- Stats of BrickStats -----");
            System.out.println("Actors: " + actors.size());
            System.out.println("Containers: " + containers.size());
            System.out.println("Records: " + containers.values().stream().mapToInt(c -> c.stats().size()).sum());
        }, 300, TimeUnit.SECONDS);
    }

    public void save(int max) {
        Set<Object> entitiesToSave = new HashSet<>();
        for (int i = 0; i < max; i++) {
            Object entity = savingQueue.poll();
            if (entity == null) {
                break;
            }
            entitiesToSave.add(entity);
        }

        databaseContext.persistAsync(entitiesToSave).join();
    }

    //

    private DActor find(@NotNull Actor.TempActor actor) {
        if (actors.containsKey(actor)) {
            return actors.get(actor);
        }
        DActor dactor = new DActor(actor.id(), actor.type());
        actors.put(actor, dactor);
        return dactor;
    }

    @Override
    public Container find(Actor.@NotNull ActorSet actors) {
        Actor.ActorSet copy = new Actor.ActorSet(actors.stream()
                .map(a -> a instanceof Actor.TempActor ta ? find(ta) : a).toList());

        if (!containers.containsKey(copy)) {
            containers.put(copy, new BrickStatsContainer(this, copy));
        }

        return containers.get(copy);
    }

    @Override
    public Container find(@NotNull Actor... actors) {
        return find(new Actor.ActorSet(actors));
    }

    //


    @Override
    public void update(Actor.@NotNull ActorSet actors, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater) {
        find(actors).update(key, updater);

        if (actors.size() <= 1) {
            return;
        }

        for (Actor actor : actors) {
            Set<Actor> ns = actors.copySet();
            ns.remove(actor);

            update(new Actor.ActorSet(ns), key, updater);
        }
    }

    @Override
    public void update(@NotNull Actor actor, @NotNull StatsKey key, @NotNull IntFunction<Integer> updater) {
        Set<Actor> actors = relationProviders.stream()
                .map(r -> r.provide(actor, key))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        actors.add(actor);

        update(new Actor.ActorSet(actors), key, updater);
    }

    //

    @Override
    public List<Record> select(@NotNull String actorType, @NotNull StatsKey key, int limit, boolean asc) {
        Comparator<Record> comp = Comparator.comparingInt(Record::value);
        if (!asc) {
            comp = comp.reversed();
        }

        List<Record> records = containers.keySet().stream()
                .filter(a -> a.size() == 1)
                .filter(a -> a.iterator().next().type().equals(actorType))
                .map(a -> find(a).find(key).orElse(null))
                .filter(Objects::nonNull)
                .sorted(comp)
                .toList();

        if (records.size() > limit) {
            records = records.subList(0, limit);
        }
        return records;
    }

    //

    @Override
    public void registerRelations(@NotNull RelationProvider relationProvider) {
        relationProviders.add(relationProvider);
    }

    @Override
    public void unregisterRelations(@NotNull RelationProvider relationProvider) {
        relationProviders.remove(relationProvider);
    }

    //

    public void handleUpdate(@NotNull Record record, int previousValue) {
        Event event = new Event(record, previousValue);
        new HashSet<>(subscriptions).forEach(sub -> sub.execute(event));

        savingQueue.add((DRecord) record);
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
