package com.github.CubieX.ModGod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ModGodEntityListener implements Listener
{
   Logger eLog;
   HashSet<String> playersInSM = new HashSet<String>();
   HashMap<String, BukkitTask> playersOnWarmup = new HashMap<String, BukkitTask>();
   private ModGod plugin = null;

   public ModGodEntityListener(ModGod plugin)
   {
      this.plugin = plugin;      

      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   //----------------------------------------------------------------------------------------------------    
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerItemHeld(PlayerItemHeldEvent event)
   {
      try
      {
         if(!event.getPlayer().isOp()) //if player is op, he does not need service mode. He can use godmode from game.
         {                       
            if(event.getPlayer().getWorld().getEnvironment().equals(Environment.NETHER))
            {
               if(!hasNetherPerms(event.getPlayer()))
               {
                  return; // player is in the nether but has no permission to use ModGod in the nether. So leave handler.
               }
            }

            if(event.getPlayer().getWorld().getEnvironment().equals(Environment.THE_END))
            {               
               return; // player is in the end. ModGod does not work here. So leave handler.                
            }

            boolean doContinue = false;

            if(ModGod.debug){ModGod.log.info("bin im onPlayerItemEvent");}        
            if(event.getPlayer().hasPermission("modgod.service"))
            {
               if(ModGod.debug){ModGod.log.info("permission erkannt");}    
               ItemStack newItem;            
               newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());

               if (null != newItem) // is null if empty slot
               {                    
                  if(ModGod.debug){ModGod.log.info("ItemID: " + String.valueOf(newItem.getTypeId()));}

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
                  case 7:
                     if(plugin.getConfig().getBoolean("BEDROCK")){
                        doContinue = true;
                     }
                     break;
                  }
               }

               if(doContinue)
               {
                  if(ModGod.debug){ModGod.log.info("ServiceItem " + String.valueOf(newItem) + " erkannt.");}
                  if((!playersOnWarmup.containsKey(event.getPlayer().getName())) && (!playersInSM.contains(event.getPlayer().getName())))
                  {
                     event.getPlayer().sendMessage(ChatColor.YELLOW + "Service-Modus in " + ModGod.warmUpTime + " s...");
                     playersOnWarmup.put(event.getPlayer().getName(), plugin.startWarmUpTimer_DelayedTask(event.getPlayer()));                     
                  }
               }
               else // Player with permission has no service item in hand, so delete him from the List if he's on it.
               {
                  if(ModGod.debug){ModGod.log.info("Kein ServiceItem erkannt");}
                  if(playersInSM.contains(event.getPlayer().getName()))
                  {
                     event.getPlayer().sendMessage(ChatColor.RED + "Service-Modus AUS.");
                     playersInSM.remove(event.getPlayer().getName());                    
                  }

                  if(playersOnWarmup.containsKey(event.getPlayer().getName()))
                  {
                     event.getPlayer().sendMessage(ChatColor.RED + "Abgebrochen.");

                     // check if warm-up is still running for this player
                     if(null != playersOnWarmup.get(event.getPlayer().getName()))
                     {
                        // cancel players warm-up task (which is still running) to prevent task stacking
                        playersOnWarmup.get(event.getPlayer().getName()).cancel();
                     }

                     playersOnWarmup.remove(event.getPlayer().getName());                    
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
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onInventoryClose(InventoryCloseEvent event)
   {
      try
      {
         if(event.getPlayer() instanceof Player)
         {
            Player player = (Player) event.getPlayer();

            if(!event.getPlayer().isOp()) //if player is op, he does not need service mode. He can use godmode from game.
            {
               ItemStack heldItem;
               heldItem = event.getPlayer().getItemInHand();
               boolean doContinue = false;

               if (null != heldItem) // is null if empty slot
               {
                  if(ModGod.debug){ModGod.log.info("ItemID: " + String.valueOf(heldItem.getTypeId()));}

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
                  case 7:
                     if(plugin.getConfig().getBoolean("BEDROCK")){
                        doContinue = true;
                     }
                     break;
                  }
               }            

               if(doContinue)
               {
                  if(ModGod.debug){ModGod.log.info("ServiceItem " + String.valueOf(heldItem) + " erkannt.");}
                  if((!playersOnWarmup.containsKey(event.getPlayer().getName())) && (!playersInSM.contains(event.getPlayer().getName())))
                  {
                     player.sendMessage(ChatColor.YELLOW + "Service-Modus in " + ModGod.warmUpTime + " s...");
                     playersOnWarmup.put(event.getPlayer().getName(), plugin.startWarmUpTimer_DelayedTask(player));                     
                  }
               }      
               else // Player with permission has no service item in hand, so delete him from the List if he's on it.
               {
                  if(ModGod.debug){ModGod.log.info("Kein ServiceItem erkannt");}

                  if(playersInSM.contains(event.getPlayer().getName()))
                  {
                     player.sendMessage(ChatColor.RED + "Service-Modus AUS.");
                     playersInSM.remove(event.getPlayer().getName());                    
                  }

                  if(playersOnWarmup.containsKey(event.getPlayer().getName()))
                  {
                     player.sendMessage(ChatColor.RED + "Abgebrochen.");

                     // check if warm-up is still running for this player
                     if(null != playersOnWarmup.get(event.getPlayer().getName()))
                     {
                        // cancel players warm-up task (which is still running) to prevent task stacking
                        playersOnWarmup.get(event.getPlayer().getName()).cancel();
                     }

                     playersOnWarmup.remove(event.getPlayer().getName());
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
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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

               if(playersOnWarmup.containsKey(event.getPlayer().getName()))
               {
                  event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Berechtigung fuer den Service-Modus im Nether!");

                  // check if warm-up is still running for this player
                  if(null != playersOnWarmup.get(event.getPlayer().getName()))
                  {
                     // cancel players warm-up task (which is still running) to prevent task stacking
                     playersOnWarmup.get(event.getPlayer().getName()).cancel();
                  }

                  playersOnWarmup.remove(event.getPlayer().getName());                    
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

            if(playersOnWarmup.containsKey(event.getPlayer().getName()))
            {
               event.getPlayer().sendMessage(ChatColor.RED + "Der Service-Modus ist im End nicht erlaubt!");

               // check if warm-up is still running for this player
               if(null != playersOnWarmup.get(event.getPlayer().getName()))
               {
                  // cancel players warm-up task (which is still running) to prevent task stacking
                  playersOnWarmup.get(event.getPlayer().getName()).cancel();
               }

               playersOnWarmup.remove(event.getPlayer().getName());

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

   // this is called by the warm-up scheduler for a player after warm-up delay has exceeded
   public void activateServiceMode(Player player)
   {
      if(playersOnWarmup.containsKey(player.getName()))
      {
         // set player in service mode only, if he has held the items for the full warm-up duration
         playersOnWarmup.remove(player.getName());
         playersInSM.add(player.getName());         
         player.sendMessage(ChatColor.GREEN + "Service-Modus AKTIV");
      }
   }
}
