package com.guflimc.brick.stats.api.event;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Filter {
    boolean test(@NotNull Event event);
}
