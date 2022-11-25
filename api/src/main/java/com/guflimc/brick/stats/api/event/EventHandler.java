package com.guflimc.brick.stats.api.event;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EventHandler {
    void handle(@NotNull Subscription subscription, @NotNull Event event);
}