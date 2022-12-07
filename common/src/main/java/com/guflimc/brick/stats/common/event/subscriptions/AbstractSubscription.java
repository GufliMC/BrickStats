package com.guflimc.brick.stats.common.event.subscriptions;

import com.guflimc.brick.stats.api.event.Event;
import com.guflimc.brick.stats.api.event.EventHandler;
import com.guflimc.brick.stats.api.event.Filter;
import com.guflimc.brick.stats.api.event.Subscription;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSubscription implements Subscription {

    protected final EventHandler handler;
    protected final Filter filter;

    public AbstractSubscription(@NotNull EventHandler handler, @NotNull Filter filter) {
        this.handler = handler;
        this.filter = filter;
    }

    public void execute(Event event) {
        if (!filter.test(event)) {
            return;
        }

        handler.handle(this, event);
    }
}
