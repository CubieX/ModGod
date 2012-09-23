package com.github.CubieX.ModGod;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.CubieX.ModGod.ModGod;

public class ModGodConfigHandler 
{
    private FileConfiguration config;
    private final ModGod plugin;

    public ModGodConfigHandler(ModGod plugin) 
    {
        this.plugin = plugin;
        config = plugin.getConfig();

        if (config.get("language") == null) {
            config.set("language", "de");
        }
        if (config.get("debug") == null) {
            config.set("debug", false);
        }
        if (config.get("FLOWING_WATER") == null) {
            config.set("FLOWING_WATER", true);
        }
        if (config.get("STILL_WATER") == null) {
            config.set("STILL_WATER", true);
        }
        if (config.get("FLOWING_LAVA") == null) {
            config.set("FLOWING_LAVA", true);
        }
        if (config.get("STILL_LAVA") == null) {
            config.set("STILL_LAVA", true);
        }
        if (config.get("FIRE") == null) {
            config.set("FIRE", true);
        }
        if (config.get("ICE_BLOCK") == null) {
            config.set("ICE_BLOCK", true);
        }        

        plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        return config;
    }
}
