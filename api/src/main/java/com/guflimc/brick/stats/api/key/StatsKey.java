package com.guflimc.brick.stats.api.key;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class StatsKey {

    private final static Map<String, StatsKey> KEYS = new ConcurrentHashMap<>();

    private final String name;
    private final StatsKey parent;

    private StatsKey(@NotNull String name, @Nullable StatsKey parent) {
        if ( name.contains(".") ) {
            throw new IllegalArgumentException("Name cannot contain dots.");
        }
        this.name = name.toLowerCase();
        this.parent = parent;
        KEYS.put(name(), this);
    }

    private StatsKey(@NotNull String name) {
        this(name, null);
    }

    public String name() {
        return parent == null ? name : parent.name() + "." + name;
    }

    public StatsKey parent() {
        return parent;
    }

    //

    public StatsKey with(@NotNull String sub) {
        return new StatsKey(sub, this);
    }

    public StatsKey with(@NotNull Object sub) {
        return with(sub.toString());
    }

    public StatsKey with(@NotNull Enum<?> sub) {
        return with(sub.name());
    }

    //

    public static StatsKey of(String name) {
        if ( KEYS.containsKey(name) ) {
            return KEYS.get(name);
        }
        String[] subs = name.split(Pattern.quote("."));
        StatsKey key = null;
        for ( String sub : subs ) {
            key = new StatsKey(sub, key);
        }
        return key;
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
