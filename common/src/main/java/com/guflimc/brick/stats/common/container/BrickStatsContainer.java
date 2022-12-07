package com.guflimc.brick.stats.common.container;

import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.container.Container;
import com.guflimc.brick.stats.api.container.Record;
import com.guflimc.brick.stats.api.key.StatsKey;
import com.guflimc.brick.stats.common.BrickStatsManager;
import com.guflimc.brick.stats.common.domain.DRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class BrickStatsContainer implements Container {

    private final BrickStatsManager manager;

    private final Actor.ActorSet actors;
    private final Set<DRecord> records = new CopyOnWriteArraySet<>();

    public BrickStatsContainer(BrickStatsManager manager, Actor.ActorSet actors) {
        this.manager = manager;
        this.actors = actors;
    }

    public void add(DRecord record) {
        if ( !record.actors().equals(actors) ) {
           throw new IllegalArgumentException("Record actors does not match container actors.");
        }
        records.add(record);
    }

    //

    @Override
    public Actor.ActorSet actors() {
        return actors;
    }

    @Override
    public Collection<StatsKey> stats() {
        return records.stream().map(DRecord::key).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<Record> find(@NotNull StatsKey key) {
        return records.stream()
                .filter(r -> r.key().equals(key))
                .map(r -> (Record) r)
                .findFirst();
    }

    @Override
    public int read(@NotNull StatsKey key) {
        return find(key).map(Record::value).orElse(0);
    }

    @Override
    public void update(@NotNull StatsKey key, @NotNull IntFunction<Integer> updater) {
        DRecord record = (DRecord) find(key).orElse(null);
        if (record == null) {
            record = new DRecord(key, actors);
            records.add(record);
        }

        int previousValue = record.value();
        record.setValue(updater.apply(previousValue));

        manager.handleUpdate(record, previousValue);
//        manager.handlePermutations(record, updater);
    }

}
