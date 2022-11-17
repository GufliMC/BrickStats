package com.guflimc.brick.stats.api.key;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StatsKey {

    private final static Map<String, StatsKey> KEYS = new ConcurrentHashMap<>();

    private final String name;
    private final StatsKey parent;

    private StatsKey(@NotNull String name, StatsKey parent) {
        this.name = name.toLowerCase();
        this.parent = parent;
        KEYS.put(name(), this);
    }

    public StatsKey(@NotNull String name) {
        this(name, null);
    }

    public String name() {
        return parent == null ? name : parent.name() + "." + name;
    }

    public StatsKey parent() {
        return parent;
    }

    //

    public StatsKey of(@NotNull String sub) {
        return new StatsKey(sub, this);
    }

    public StatsKey of(@NotNull Object sub) {
        return of(sub.toString());
    }

    public StatsKey of(@NotNull Enum<?> sub) {
        return of(sub.name());
    }

    //

    public static StatsKey valueOf(String name) {
        return KEYS.get(name);
    }

    public static StatsKey[] values() {
        return KEYS.values().toArray(StatsKey[]::new);
    }

    //

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StatsKey sk && sk.name().equals(name());
    }
}
