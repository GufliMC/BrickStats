package com.guflimc.brick.stats.common.event;

import com.guflimc.brick.stats.api.event.EventHandler;
import com.guflimc.brick.stats.api.event.Filter;
import com.guflimc.brick.stats.api.event.SubscriptionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSubscriptionBuilder implements SubscriptionBuilder {

    protected List<Filter> filters = new ArrayList<>();
    protected EventHandler handler;

    @Override
    public SubscriptionBuilder filter(@NotNull Filter filter) {
        this.filters.add(filter);
        return this;
    }

    @Override
    public SubscriptionBuilder handler(@NotNull EventHandler handler) {
        this.handler = handler;
        return this;
    }

}