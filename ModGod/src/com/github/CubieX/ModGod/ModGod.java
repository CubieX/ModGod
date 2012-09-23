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

public class ModGod extends JavaPlugin implements Listener
{
    Logger log;
    ArrayList<String> playersInSM = new ArrayList<String>();

    //private ModGodCommandHandler myComHandler;
    private ModGodConfigHandler configHandler = null;

    @Override
    public void onEnable()
    {
        //new ModGodEntityListener(this);
        getServer().getPluginManager().registerEvents(this, this);
        log = this.getLogger();
        log.info("ModGod version " + getDescription().getVersion() + " is enabled!");

        //myComHandler = new ModGodCommandHandler(this);
        //getCommand("mg").setExecutor(myComHandler);
        configHandler = new ModGodConfigHandler(this);
    }

    public void onDisable()
    {
        log.info("ModGod version " + getDescription().getVersion() + " is disabled!");
        configHandler = null;
    }    

    //================================================================================================
    @EventHandler // event has Normal priority
    public void onPlayerLogin(PlayerLoginEvent event)
    {                
        // Your code here... does player have ServiceMode when logged in but did not change its
        // item in hand because he allready has a service item in hand?            
    }   

    //----------------------------------------------------------------------------------------------------    
    @EventHandler // event has Normal priority
    public void onPlayerItemHeld(PlayerItemHeldEvent event)
    {            
        if(!event.getPlayer().isOp()) //if player is op, he does not need service mode. He can use godmode from game.
        {
            boolean doContinue = false;
            if(configHandler.getConfig().getBoolean("debug")){log.info("bin im onPlayerItemEvent");}        
            if(event.getPlayer().hasPermission("modgod.service"))
            {
                if(configHandler.getConfig().getBoolean("debug")){log.info("permission erkannt");}    
                ItemStack newItem;            
                newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
                if (newItem != null) // is null if empty slot
                {                    
                    if(configHandler.getConfig().getBoolean("debug")){log.info("ItemID: " + String.valueOf(newItem.getTypeId()));}

                    switch(newItem.getTypeId()) //check config. Change theses cases also in onInventoryClose Event!
                    {
                    case 8:
                        if(configHandler.getConfig().getBoolean("FLOWING_WATER")){
                            doContinue = true;
                        }
                        break;
                    case 9:
                        if(configHandler.getConfig().getBoolean("STILL_WATER")){
                            doContinue = true;
                        }
                        break;
                    case 10:
                        if(configHandler.getConfig().getBoolean("FLOWING_LAVA")){
                            doContinue = true;
                        }
                        break;
                    case 11:
                        if(configHandler.getConfig().getBoolean("STILL_LAVA")){
                            doContinue = true;
                        }
                        break;
                    case 51:
                        if(configHandler.getConfig().getBoolean("FIRE")){
                            doContinue = true;
                        }
                        break;
                    case 79:
                        if(configHandler.getConfig().getBoolean("ICE_BLOCK")){
                            doContinue = true;
                        }
                        break;                                        
                    }
                }
                if(doContinue)
                {
                    if(configHandler.getConfig().getBoolean("debug")){log.info("ServiceItem " + String.valueOf(newItem) + " erkannt.");}
                    if(false == playersInSM.contains(event.getPlayer().getName()))
                    {
                        event.getPlayer().sendMessage(ChatColor.GREEN + "Service-Modus EIN.");
                        playersInSM.add(event.getPlayer().getName());
                    }                            
                }      
                else // Player with permission has no service item in hand, so delete him from the List if hes on it.
                {
                    if(configHandler.getConfig().getBoolean("debug")){log.info("Kein ServiceItem erkannt");}
                    if(playersInSM.contains(event.getPlayer().getName()))
                    {
                        event.getPlayer().sendMessage(ChatColor.GREEN + "Service-Modus AUS.");
                        playersInSM.remove(event.getPlayer().getName());                    
                    }                    
                }           

            }
        }
    }  

    //----------------------------------------------------------------------------------------------
    @EventHandler // event has Normal priority
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if(!event.getPlayer().isOp()) //if player is op, he does not need service mode. He can use godmode from game.
        {
            ItemStack heldItem;
            heldItem = event.getPlayer().getItemInHand();
            boolean doContinue = false;
            if (heldItem != null) // is null if empty slot
            {
                if(configHandler.getConfig().getBoolean("debug")){log.info("ItemID: " + String.valueOf(heldItem.getTypeId()));}

                switch( heldItem.getTypeId()) //check config
                {
                case 8:
                    if(configHandler.getConfig().getBoolean("FLOWING_WATER")){
                        doContinue = true;
                    }
                    break;
                case 9:
                    if(configHandler.getConfig().getBoolean("STILL_WATER")){
                        doContinue = true;
                    }
                    break;
                case 10:
                    if(configHandler.getConfig().getBoolean("FLOWING_LAVA")){
                        doContinue = true;
                    }
                    break;
                case 11:
                    if(configHandler.getConfig().getBoolean("STILL_LAVA")){
                        doContinue = true;
                    }
                    break;
                case 51:
                    if(configHandler.getConfig().getBoolean("FIRE")){
                        doContinue = true;
                    }
                    break;
                case 79:
                    if(configHandler.getConfig().getBoolean("ICE_BLOCK")){
                        doContinue = true;
                        log.info("Eis auf TRUE erkannt...");
                    }
                    break;                                    
                }

            } 
            if(doContinue)
            {
                if(configHandler.getConfig().getBoolean("debug")){log.info("ServiceItem " + String.valueOf(heldItem) + " erkannt.");}
                if(false == playersInSM.contains(event.getPlayer().getName()))
                {                    
                    playersInSM.add(event.getPlayer().getName());
                }                            
            }      
            else // Player with permission has no service item in hand, so delete him from the List if hes on it.
            {
                if(configHandler.getConfig().getBoolean("debug")){log.info("Kein ServiceItem erkannt");}
                if(playersInSM.contains(event.getPlayer().getName()))
                {                    
                    playersInSM.remove(event.getPlayer().getName());                    
                }                    
            }

        }
        else
        {
            if(configHandler.getConfig().getBoolean("debug")){log.info("leerer Slot");}
            if(playersInSM.contains(event.getPlayer().getName()))
            {                
                playersInSM.remove(event.getPlayer().getName());                    
            }    
        }


    }

    //----------------------------------------------------------------------------------------------------
    @EventHandler
    public void onPlayerDamageEvent (EntityDamageEvent event)
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

    //=============================================================================================================
    // Command Handler -- ToDo: SHOULD BE IN SEPARATE CLASS!!! ---------------------------------------------
    // player typed a command

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) 
        {
            player = (Player) sender;
        }

        if(configHandler.getConfig().getBoolean("debug")){log.info("onCommand");}
        if (cmd.getName().equalsIgnoreCase("mg"))
        { // If the player typed /mf then do the following... (can be run from console also)
            if (args.length == 0)
            { //no arguments, so help will be displayed
                return false;
            }
            if (args.length==1)
            {
                if (args[0].equalsIgnoreCase("version")) // argument 0 is given and correct
                {            
                    sender.sendMessage(ChatColor.YELLOW + "This server is running ModGod version " + this.getDescription().getVersion());
                    return true;
                }    
                if (args[0].equalsIgnoreCase("reload")) // argument 0 is given and correct
                {            
                    if(sender.hasPermission("modgod.reload"))
                    {
                        //this.getServer().getPluginManager().disablePlugin(this);
                        //this.getServer().getPluginManager().enablePlugin(this);
                        configHandler.getConfig();
                        sender.sendMessage("[" + ChatColor.BLUE + "Info" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "ModGod " + this.getDescription().getVersion() + " reloaded!");
                        return true;
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to reload ModGod!");
                    }
                }
            }
            else
            {
                sender.sendMessage(ChatColor.YELLOW + "Ungültige Anzahl Argumente.");
            }                

        }         
        return false; // if false is returned, the help for the command stated in the plugin.yml will be displayed to the player
    }

}


