package com.guflimc.brick.stats.spigot;

import com.google.gson.Gson;
import com.guflimc.brick.scheduler.spigot.api.SpigotScheduler;
import com.guflimc.brick.stats.api.StatsAPI;
import com.guflimc.brick.stats.common.BrickStatsConfig;
import com.guflimc.brick.stats.common.BrickStatsDatabaseContext;
import com.guflimc.brick.stats.common.BrickStatsManager;
import com.guflimc.brick.stats.spigot.tasks.MovementTask;
import com.guflimc.brick.stats.spigot.tasks.PlayTimeTask;
import com.guflimc.brick.stats.spigot.listeners.BlockListener;
import com.guflimc.brick.stats.spigot.listeners.DeathListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class SpigotBrickStats extends JavaPlugin {

    public static final Gson gson = new Gson();

    private BrickStatsManager manager;

    @Override
    public void onEnable() {

        // load config
        saveResource("config.json", false);
        BrickStatsConfig config;
        try (
                InputStream is = new FileInputStream(new File(getDataFolder(), "config.json"));
                InputStreamReader isr = new InputStreamReader(is)
        ) {
            config = gson.fromJson(isr, BrickStatsConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // initialize database
        BrickStatsDatabaseContext databaseContext = new BrickStatsDatabaseContext(config.database);

        // create scheduler
        SpigotScheduler scheduler = new SpigotScheduler(this, getName());

        // create manager
        manager = new BrickStatsManager(databaseContext, scheduler);
        StatsAPI.setManager(manager);

        // register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new DeathListener(), this);

        // start jobs
        scheduler.syncRepeating(new PlayTimeTask(), 1, TimeUnit.SECONDS);
        scheduler.syncRepeating(new MovementTask(), 1, TimeUnit.SECONDS);

        getLogger().info("Enabled " + nameAndVersion() + ".");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.save(Integer.MAX_VALUE);
        }

        getLogger().info("Disabled " + nameAndVersion() + ".");
    }

    private String nameAndVersion() {
        return getName() + " v" + getDescription().getVersion();
    }

}
