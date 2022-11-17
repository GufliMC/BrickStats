package com.guflimc.brick.stats.api.container;

import com.guflimc.brick.stats.api.key.StatsKey;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface StatsContainer {

    UUID id();

    int read(@NotNull StatsKey key);

    int read(UUID relation, @NotNull StatsKey key);

}
