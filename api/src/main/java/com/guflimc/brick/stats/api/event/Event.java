package com.guflimc.brick.stats.api.event;

import com.guflimc.brick.stats.api.container.StatsRecord;
import com.guflimc.brick.stats.api.key.StatsKey;
import org.jetbrains.annotations.NotNull;

public record Event(@NotNull StatsKey key, @NotNull StatsRecord record, int previousValue) {
}