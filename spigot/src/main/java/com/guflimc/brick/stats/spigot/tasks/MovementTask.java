package com.guflimc.brick.stats.spigot.tasks;

import com.guflimc.brick.stats.api.StatsAPI;
import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.key.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class MovementTask implements Runnable {

    private final Map<UUID, Location> locations = new HashMap<>();
    private final Map<UUID, Double> cache = new HashMap<>();

    @Override
    public void run() {
        Set<UUID> passed = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            passed.add(id);

            if (!locations.containsKey(id)) {
                locations.put(id, player.getLocation());
                return;
            }

            if (!cache.containsKey(id)) {
                cache.put(id, 0d);
            }

            double current = cache.get(id);
            current += player.getLocation().distance(locations.get(id));

            if (current > 1) {
                int floor = (int) Math.floor(current);
                current -= floor;
                StatsAPI.get().update(Actor.player(id), Keys.DISTANCE_MOVED, x -> x + floor);
            }

            cache.put(id, current);
            locations.put(id, player.getLocation());
        }

        locations.keySet().removeIf(passed::contains);
        cache.keySet().removeIf(passed::contains);
    }

}
