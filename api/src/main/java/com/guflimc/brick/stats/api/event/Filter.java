package com.guflimc.brick.stats.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface Filter {
    boolean test(@NotNull Event event);

    static Filter allOf(Collection<Filter> filters) {
        return event -> filters.stream().allMatch(f -> f.test(event));
    }

    static Filter allOf(Filter... filters) {
        return allOf(List.of(filters));
    }
}
