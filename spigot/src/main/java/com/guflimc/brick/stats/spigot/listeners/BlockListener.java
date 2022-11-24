package com.guflimc.brick.stats.spigot.listeners;

import com.guflimc.brick.stats.api.StatsAPI;
import com.guflimc.brick.stats.api.key.Keys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        StatsAPI.get().update(
                event.getPlayer().getUniqueId(),
                Keys.BLOCKS_BROKEN.with(event.getBlock().getType()),
                x -> x + 1
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlace(BlockBreakEvent event) {
        StatsAPI.get().update(
                event.getPlayer().getUniqueId(),
                Keys.BLOCKS_PLACED.with(event.getBlock().getType()),
                x -> x + 1
        );
    }

}
