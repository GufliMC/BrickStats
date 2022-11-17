package com.guflimc.brick.stats.spigot.listeners;

import com.guflimc.brick.stats.api.StatsAPI;
import com.guflimc.brick.stats.api.key.Keys;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        StatsAPI.get().update(event.getEntity().getUniqueId(), Keys.DEATHS, x -> x + 1);

        Player killer = event.getEntity().getKiller();
        if ( killer == null ) {
            return;
        }

        StatsAPI.get().update(killer.getUniqueId(), Keys.KILLS, x -> x + 1);
    }

}
