package com.guflimc.brick.stats.api;

import org.jetbrains.annotations.ApiStatus;

public class StatsAPI {

    private static StatsManager manager;

    @ApiStatus.Internal
    public static void setManager(StatsManager _manager) {
        manager = _manager;
    }

    //

    public static StatsManager get() {
        return manager;
    }

}
