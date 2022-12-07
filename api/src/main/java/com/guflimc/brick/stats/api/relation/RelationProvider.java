package com.guflimc.brick.stats.api.relation;

import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.key.StatsKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface RelationProvider {

    @Nullable
    Actor provide(@NotNull Actor actor, @NotNull StatsKey key);

}
