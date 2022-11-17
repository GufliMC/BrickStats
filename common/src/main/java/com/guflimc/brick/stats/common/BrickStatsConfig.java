package com.guflimc.brick.stats.common;

import com.guflimc.brick.orm.ebean.database.EbeanConfig;

import java.util.List;

public class BrickStatsConfig {

    public EbeanConfig database;

    public int powerGiftAmount;
    public int powerGiftInterval;

    public String clanChatPrefix;
    public String clanNametagPrefix;
    public String clanRegionNone;

    public SidebarTemplate attackSidebar;
    public int attackDuration; // minutes

    public static class SidebarTemplate {

        public String title;
        public List<String> lines;

    }

}
