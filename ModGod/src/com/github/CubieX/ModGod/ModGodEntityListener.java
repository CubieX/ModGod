package com.github.CubieX.ModGod;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.minecraft.server.Item;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class ModGodEntityListener implements Listener
{
    Logger eLog;
    ArrayList<String> playersInSM = new ArrayList<String>();
    private ModGod plugin;
    
    public ModGodEntityListener(ModGod plugin)
    {        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);    
        eLog.info("Bin im onPlayerLogin");
    }

    @EventHandler // event has Normal priority
    public void onPlayerLogin(PlayerLoginEvent event)
    {                
        // Your code here... does player have ServiceMode when logged in but did not change its
        // item in hand because he allready has a service item in hand?
        event.getPlayer().sendMessage("ModGod gruesst " + event.getPlayer().getName() + "!");
        eLog.info("Bin im onPlayerLogin");
    }   

    @EventHandler // event has Normal priority
    public void onPlayerItemHeld(PlayerItemHeldEvent event)
    {            
        eLog.info("onPlayerItemEvent");
        if(event.getPlayer().hasPermission("modgod.service"))
        {
            eLog.info("permission erkannt");
            if(event.getPlayer().getItemInHand().equals(Item.byId[8]) ||
                    event.getPlayer().getItemInHand().equals(Item.byId[9]) ||
                    event.getPlayer().getItemInHand().equals(Item.byId[10]) ||
                    event.getPlayer().getItemInHand().equals(Item.byId[11]) ||
                    event.getPlayer().getItemInHand().equals(Item.byId[51]) ) //Water, Lava, Fire? (the cheated one. Not the bukkets)
            {
                eLog.info("item erkannt");
                if(false == playersInSM.contains(event.getPlayer().getName()))
                {
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Service-Modus EIN.");
                    playersInSM.add(event.getPlayer().getName());
                }                
            }
            else // Player with permission has no service item in hand, so delete him from the List if hes on it.
            {
                eLog.info("kein item erkannt");
                if(playersInSM.contains(event.getPlayer().getName()))
                {
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Service-Modus AUS.");
                    playersInSM.remove(event.getPlayer().getName());
                }                
            }
        }
    }   

    @EventHandler
    public void onPlayerDamageEvent (EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        if (entity instanceof Player)
        {
            if(playersInSM.contains(((Player) entity).getName())) //check if player is in ServiceMode
            {
                //if(event.getCause() == event.) // optional: look what caused the damage. (to not have a complete god mode)
                event.setCancelled(true); // if yes, dont apply damage (e.g. from falling into lava or creeper explosion)
            }
        }
    }
}
