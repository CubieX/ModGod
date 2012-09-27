package com.github.CubieX.ModGod;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ModGod extends JavaPlugin
{
    Logger log;
    ArrayList<String> playersInSM = new ArrayList<String>();

    private ModGod plugin = null;
    private ModGodCommandHandler myComHandler = null;
    private ModGodConfigHandler configHandler = null;
    private ModGodEntityListener eListener = null;

    @Override
    public void onEnable()
    {
        this.plugin = this;
        configHandler = new ModGodConfigHandler(this);
        eListener = new ModGodEntityListener(this, configHandler, log);        
        log = this.getLogger();
        log.info("ModGod version " + getDescription().getVersion() + " is enabled!");

        myComHandler = new ModGodCommandHandler(this, configHandler, log);
        getCommand("mg").setExecutor(myComHandler);       
    }

    @Override
    public void onDisable()
    {
        log.info("ModGod version " + getDescription().getVersion() + " is disabled!");
        configHandler = null;
    }    
}


