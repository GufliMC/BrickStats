package com.guflimc.brick.stats.api.entity;

import com.guflimc.brick.stats.api.key.StatsKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@FunctionalInterface
public interface RelationProvider {

    Optional<UUID> relation(@NotNull UUID playerId, @NotNull StatsKey key);

}
