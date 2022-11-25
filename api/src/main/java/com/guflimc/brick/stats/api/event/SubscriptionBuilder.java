package com.guflimc.brick.stats.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface SubscriptionBuilder {

    SubscriptionBuilder filter(@NotNull Filter filter);

    //

    SubscriptionBuilder handler(@NotNull EventHandler handler);

    default SubscriptionBuilder handler(@NotNull Consumer<Event> handler) {
        return handler((sub, event) -> handler.accept(event));
    }

    //

    Subscription milestone(int milestone);

    Subscription interval(int interval);

    Subscription change();
}





