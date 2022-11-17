package com.guflimc.brick.stats.api.container;

import java.util.UUID;

public interface StatsRecord {

    UUID id();

    UUID relation();

    int value();

}
