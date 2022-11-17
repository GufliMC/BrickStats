package com.guflimc.brick.stats.spigot.jobs;

import com.guflimc.brick.stats.api.StatsAPI;
import com.guflimc.brick.stats.api.key.Keys;
import org.bukkit.Bukkit;

public class PlayTimeJob implements Runnable {

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(p -> StatsAPI.get().update(
                p.getUniqueId(),
                Keys.PLAY_TIME,
                x -> x + 1
        ));
    }

}
