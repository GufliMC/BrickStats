package com.guflimc.brick.stats.spigot.tasks;

import com.guflimc.brick.stats.api.StatsAPI;
import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.key.Keys;
import org.bukkit.Bukkit;

public class PlayTimeTask implements Runnable {

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(p -> StatsAPI.get().update(
                Actor.player(p.getUniqueId()),
                Keys.PLAY_TIME,
                x -> x + 1
        ));
    }

}
