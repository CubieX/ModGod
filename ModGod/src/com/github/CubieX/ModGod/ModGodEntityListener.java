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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class ModGodEntityListener implements Listener
{
    Logger eLog;
    ArrayList<String> playersInSM = new ArrayList<String>();
    private ModGod plugin = null;
    private ModGodConfigHandler configHandler = null;
    private Logger log = null;

    public ModGodEntityListener(ModGod plugin, ModGodConfigHandler configHandler, Logger log)
    {        
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.log = log;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    //----------------------------------------------------------------------------------------------------    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event)
    {   
        try
        {
            if(!event.getPlayer().isOp()) //if player is op, he does not need service mode. He can use godmode from game.
            {                       
                if(event.getPlayer().getLocation().getWorld().getName().toLowerCase().contains("nether"))
                {
                    if(!hasNetherPerms(event.getPlayer()))
                    {
                        return; // player is in the nether but has no permission to use ModGod in the nether. So leave handler.
                    }
                }

                if(event.getPlayer().getLocation().getWorld().getName().toLowerCase().contains("the_end"))
                {               
                    return; // player is in the end. ModGod does not work here. So leave handler.                
                }

                boolean doContinue = false;

                if(ModGod.debug){log.info("bin im onPlayerItemEvent");}        
                if(event.getPlayer().hasPermission("modgod.service"))
                {
                    if(ModGod.debug){log.info("permission erkannt");}    
                    ItemStack newItem;            
                    newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
                    if (null != newItem) // is null if empty slot
                    {                    
                        if(ModGod.debug){log.info("ItemID: " + String.valueOf(newItem.getTypeId()));}

                        switch(newItem.getTypeId()) //check config. Change theses cases also in onInventoryClose Event!
                        {
                        case 8:
                            if(plugin.getConfig().getBoolean("FLOWING_WATER")){
                                doContinue = true;
                            }
                            break;
                        case 9:
                            if(plugin.getConfig().getBoolean("STILL_WATER")){
                                doContinue = true;
                            }
                            break;
                        case 10:
                            if(plugin.getConfig().getBoolean("FLOWING_LAVA")){
                                doContinue = true;
                            }
                            break;
                        case 11:
                            if(plugin.getConfig().getBoolean("STILL_LAVA")){
                                doContinue = true;
                            }
                            break;
                        case 51:
                            if(plugin.getConfig().getBoolean("FIRE")){
                                doContinue = true;
                            }
                            break;
                        case 79:
                            if(plugin.getConfig().getBoolean("ICE_BLOCK")){
                                doContinue = true;
                            }
                            break;                                        
                        }
                    }
                    if(doContinue)
                    {
                        if(ModGod.debug){log.info("ServiceItem " + String.valueOf(newItem) + " erkannt.");}
                        if(false == playersInSM.contains(event.getPlayer().getName()))
                        {
                            event.getPlayer().sendMessage(ChatColor.GREEN + "Service-Modus EIN.");
                            playersInSM.add(event.getPlayer().getName());
                        }                            
                    }      
                    else // Player with permission has no service item in hand, so delete him from the List if he's on it.
                    {
                        if(ModGod.debug){log.info("Kein ServiceItem erkannt");}
                        if(playersInSM.contains(event.getPlayer().getName()))
                        {
                            event.getPlayer().sendMessage(ChatColor.GREEN + "Service-Modus AUS.");
                            playersInSM.remove(event.getPlayer().getName());                    
                        }                    
                    }           

                }
            }
        }
        catch(Exception ex)
        {
            // player is probably no longer online
        }
    }  

    //----------------------------------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event)
    {
        try
        {
            if(!event.getPlayer().isOp()) //if player is op, he does not need service mode. He can use godmode from game.
            {
                ItemStack heldItem;
                heldItem = event.getPlayer().getItemInHand();
                boolean doContinue = false;
                if (heldItem != null) // is null if empty slot
                {
                    if(ModGod.debug){log.info("ItemID: " + String.valueOf(heldItem.getTypeId()));}

                    switch( heldItem.getTypeId()) //check config
                    {
                    case 8:
                        if(plugin.getConfig().getBoolean("FLOWING_WATER")){
                            doContinue = true;
                        }
                        break;
                    case 9:
                        if(plugin.getConfig().getBoolean("STILL_WATER")){
                            doContinue = true;
                        }
                        break;
                    case 10:
                        if(plugin.getConfig().getBoolean("FLOWING_LAVA")){
                            doContinue = true;
                        }
                        break;
                    case 11:
                        if(plugin.getConfig().getBoolean("STILL_LAVA")){
                            doContinue = true;
                        }
                        break;
                    case 51:
                        if(plugin.getConfig().getBoolean("FIRE")){
                            doContinue = true;
                        }
                        break;
                    case 79:
                        if(plugin.getConfig().getBoolean("ICE_BLOCK")){
                            doContinue = true;                        
                        }
                        break;                                    
                    }
                } 
                if(doContinue)
                {
                    if(ModGod.debug){log.info("ServiceItem " + String.valueOf(heldItem) + " erkannt.");}
                    if(false == playersInSM.contains(event.getPlayer().getName()))
                    {                    
                        playersInSM.add(event.getPlayer().getName());
                    }                            
                }      
                else // Player with permission has no service item in hand, so delete him from the List if he's on it.
                {
                    if(ModGod.debug){log.info("Kein ServiceItem erkannt");}
                    if(playersInSM.contains(event.getPlayer().getName()))
                    {                    
                        playersInSM.remove(event.getPlayer().getName());                    
                    }                    
                }
            }
            else
            {
                if(ModGod.debug){log.info("leerer Slot");}
                if(playersInSM.contains(event.getPlayer().getName()))
                {                
                    playersInSM.remove(event.getPlayer().getName());                    
                }    
            }
        }
        catch(Exception ex)
        {
            // player is probably no longer online
        }        
    }

    //----------------------------------------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDamageEvent (EntityDamageEvent event)
    {
        try
        {
            if(event.getEntity() instanceof Player)
            {
                Player p = (Player) event.getEntity();

                if(playersInSM.contains(p.getName())) //check if player is in ServiceMode
                {
                    //if(event.getCause() == event.) // optional: look what caused the damage. (to not have a complete god mode)
                    event.setCancelled(true); // if yes, dont apply damage (e.g. from falling into lava or creeper explosion)
                }    
            }
        }
        catch(Exception ex)
        {
            // player is probably no longer online
        }        
    }

    //----------------------------------------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChangedWorldEvent (PlayerChangedWorldEvent event)
    {      
        try
        {
            if(event.getPlayer().getLocation().getWorld().getName().toLowerCase().contains("nether"))
            {
                if(!hasNetherPerms(event.getPlayer()))
                {
                    if(playersInSM.contains(event.getPlayer().getName()))
                    {
                        event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Berechtigung fuer den Service-Modus im Nether!");
                        playersInSM.remove(event.getPlayer().getName());                    
                    }
                    return; // player is in the nether but has no permission to use ModGod in the nether. So leave handler.
                }
            }

            if(event.getPlayer().getLocation().getWorld().getName().toLowerCase().contains("the_end"))
            {
                if(playersInSM.contains(event.getPlayer().getName()))
                {
                    event.getPlayer().sendMessage(ChatColor.RED + "Der Service-Modus ist im End nicht erlaubt!");
                    playersInSM.remove(event.getPlayer().getName());                    
                }
                return; // player is in the end. ModGod does not work here. So leave handler.                
            }
        }
        catch(Exception ex)
        {
            // player is probably no longer online
        }        
    }

    private boolean hasNetherPerms(Player playerToCheck)
    {
        boolean hasPermission = false;

        try
        {      
            if(playerToCheck.hasPermission("modgod.nether"))
            {
                hasPermission = true;
            }            
        }
        catch(Exception ex)
        {
            // player is probably no longer online
        }    

        return (hasPermission);
    }
}
