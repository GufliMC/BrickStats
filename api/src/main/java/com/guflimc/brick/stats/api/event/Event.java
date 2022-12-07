package com.guflimc.brick.stats.api.event;

import com.guflimc.brick.stats.api.container.Record;
import com.guflimc.brick.stats.api.key.StatsKey;
import org.jetbrains.annotations.NotNull;

public record Event(@NotNull Record record, int previousValue) {
}