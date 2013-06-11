package com.github.CubieX.ModGod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ModGodEntityListener implements Listener
{
   Logger eLog;
   HashSet<String> playersInSM = new HashSet<String>();
   HashMap<String, BukkitTask> playersOnWarmup = new HashMap<String, BukkitTask>();
   HashMap<String, BukkitTask> playersInGracePeriod = new HashMap<String, BukkitTask>();
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
      if(ModGod.debug){event.getPlayer().sendMessage("Item-Slot: " + event.getNewSlot() + " | Item: " + event.getPlayer().getInventory().getItem(event.getNewSlot()));}

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
                  if(ModGod.debug){ModGod.log.info("Item: " + String.valueOf(newItem.getType()));}

                  if(plugin.getConfig().getStringList("serviceItems").contains(newItem.getType().toString()))
                  {
                     doContinue = true;
                  }
               }
               // empty hand will also be treated as non-service-item

               if(doContinue)
               {
                  if(ModGod.debug){ModGod.log.info("ServiceItem " + String.valueOf(newItem) + " erkannt.");}

                  if(ModGod.warmUpTime > 0) // only use timer and playersOnWarmup HashMap if value is > 0 in config
                  {
                     if((!playersOnWarmup.containsKey(event.getPlayer().getName())) && (!playersInSM.contains(event.getPlayer().getName())))
                     {
                        event.getPlayer().sendMessage(ChatColor.YELLOW + "Service-Modus in " + ModGod.warmUpTime + " s...");
                        playersOnWarmup.put(event.getPlayer().getName(), plugin.startWarmUpTimer_DelayedTask(event.getPlayer()));                     
                     }
                  }
                  else
                  {
                     playersInSM.add(event.getPlayer().getName());
                  }

                  // cancel graceTimer if player in service mode switched back from a non-service tool to a valid service tool
                  // before gracePeriod has expired
                  if(playersInSM.contains(event.getPlayer().getName()) && playersInGracePeriod.containsKey(event.getPlayer().getName()))
                  {
                     if (null != playersInGracePeriod.get(event.getPlayer().getName())) // graceTimer is still running
                     {
                        playersInGracePeriod.get(event.getPlayer().getName()).cancel();
                     }
                     playersInGracePeriod.remove(event.getPlayer().getName());
                  }
               }
               else // Player with permission has no service item in hand, so start grace Timer if he is currently in service mode
               {
                  if(ModGod.debug){ModGod.log.info("Kein ServiceItem erkannt");}

                  if(ModGod.gracePeriod > 0) // only use timer and playersInGracePeriod HashMap if value is > 0 in config
                  {
                     if((playersInSM.contains(event.getPlayer().getName())) && (!playersInGracePeriod.containsKey(event.getPlayer().getName())))
                     {
                        playersInGracePeriod.put(event.getPlayer().getName(), plugin.startGracePeriodTimer_DelayedTask(event.getPlayer()));                     
                     }
                  }
                  else
                  {
                     if(playersInSM.contains(event.getPlayer().getName()))
                     {
                        playersInSM.remove(event.getPlayer());
                     }
                  }

                  abortWarmUp(event.getPlayer());
               }

            }
         }
      }
      catch(Exception ex)
      {         
         ex.printStackTrace();
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
               heldItem = player.getItemInHand();
               boolean doContinue = false;

               if (null != heldItem) // is probably never null in this event (will be AIR) But to be sure...
               {
                  if(ModGod.debug){ModGod.log.info("Item: " + String.valueOf(heldItem.getType()));}                  

                  if(plugin.getConfig().getStringList("serviceItems").contains(heldItem.getType().toString()))
                  {
                     doContinue = true;
                  }

                  if(doContinue)
                  {
                     if(ModGod.debug){ModGod.log.info("ServiceItem " + String.valueOf(heldItem) + " erkannt.");}
                     if((!playersOnWarmup.containsKey(player.getName())) && (!playersInSM.contains(player.getName())))
                     {
                        player.sendMessage(ChatColor.YELLOW + "Service-Modus in " + ModGod.warmUpTime + " s...");
                        playersOnWarmup.put(player.getName(), plugin.startWarmUpTimer_DelayedTask(player));                     
                     }

                     // cancel graceTimer if player in service mode switched back from a non-service tool to a valid service tool
                     // before gracePeriod has expired
                     if(playersInSM.contains(player.getName()) && playersInGracePeriod.containsKey(player.getName()))
                     {
                        if (null != playersInGracePeriod.get(player.getName())) // graceTimer is still running
                        {
                           playersInGracePeriod.get(player.getName()).cancel();
                        }
                        playersInGracePeriod.remove(player.getName());
                     }
                  }
                  else // Player with permission has no service item in hand, so start grace Timer if he is currently in service mode
                  {
                     if(ModGod.debug){ModGod.log.info("Kein ServiceItem erkannt");}

                     if((playersInSM.contains(player.getName())) && (!playersInGracePeriod.containsKey(player.getName())))
                     {
                        playersInGracePeriod.put(player.getName(), plugin.startGracePeriodTimer_DelayedTask(player));                     
                     }

                     abortWarmUp(player);
                  }
               }               
            }        
         }
      }
      catch(Exception ex)
      {         
         ex.printStackTrace();
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
         ex.printStackTrace();
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

               if(playersInGracePeriod.containsKey(event.getPlayer().getName()))
               {
                  // check if graceTimer is still running for this player
                  if(null != playersInGracePeriod.get(event.getPlayer().getName()))
                  {
                     // cancel players graceTimer task (which is still running) to prevent task stacking
                     playersInGracePeriod.get(event.getPlayer().getName()).cancel();
                  }

                  playersInGracePeriod.remove(event.getPlayer().getName());    
               }

               return; // player is in the nether but has no permission to use ModGod in the nether. So leave handler.
            }
         }

         if(event.getPlayer().getLocation().getWorld().getName().toLowerCase().contains("the_end"))
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

            if(playersInGracePeriod.containsKey(event.getPlayer().getName()))
            {
               // check if graceTimer is still running for this player
               if(null != playersInGracePeriod.get(event.getPlayer().getName()))
               {
                  // cancel players graceTimer task (which is still running) to prevent task stacking
                  playersInGracePeriod.get(event.getPlayer().getName()).cancel();
               }

               playersInGracePeriod.remove(event.getPlayer().getName());    
            }

            return; // player is in the end. ModGod does not work here. So leave handler.                
         }
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }        
   }

   //----------------------------------------------------------------------------------------------------    
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerItemPickup(PlayerPickupItemEvent event)
   {
      // disable Service mode if picked up item is a non-service item
      // player may be in service mode and have a free hand slot selected and pick up a non-service-mode item!     

      // start players graceTimer (if delay set) to disable service mode, if he has a non-service item in hand after grace period (maybe he just picked it up)
      if(ModGod.gracePeriod > 0) // only use timer and playersInGracePeriod HashMap if value is > 0 in config
      {
         if((playersInSM.contains(event.getPlayer().getName())) && (!playersInGracePeriod.containsKey(event.getPlayer().getName())))
         {
            playersInGracePeriod.put(event.getPlayer().getName(), plugin.startGracePeriodTimer_DelayedTask(event.getPlayer()));                     
         }
      }
      else
      {
         if(playersInSM.contains(event.getPlayer().getName()))
         {
            playersInSM.remove(event.getPlayer());
         }
      }
   }

   // ##########################################################################################

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
      if(null != player)
      {
         if(playersOnWarmup.containsKey(player.getName()))
         {
            // set player in service mode only, if he has held the items for the full warm-up duration
            playersOnWarmup.remove(player.getName());            

            enableServiceMode(player);
         }
      }
   }

   // this is called by the gracePeriod scheduler for a player after switching to non-service item
   public void disableServiceModeWithGrace(Player player)
   {
      if(null != player)
      {
         if(playersInGracePeriod.containsKey(player.getName()))
         {
            playersInGracePeriod.remove(player.getName());
         }

         // if player has no service item in hand after grace period has expired,
         // delete him from the list of service mode users
         if(!plugin.getConfig().getStringList("serviceItems").contains(player.getItemInHand().getType().toString()))
         {
            disableServiceMode(player);
         }
      }
   }

   public void abortWarmUp(Player player)
   {
      if(null != player)
      {
         if(playersOnWarmup.containsKey(player.getName()))
         {
            // check if warm-up is still running for this player
            if(null != playersOnWarmup.get(player.getName()))
            {
               // cancel players warm-up task (which is still running) to prevent task stacking
               playersOnWarmup.get(player.getName()).cancel();
            }

            playersOnWarmup.remove(player.getName());

            if(player.isOnline())
            {
               player.sendMessage(ChatColor.RED + "Abgebrochen");
            }
         }
      }
   }

   public void enableServiceMode(Player player)
   {
      if(null != player)
      {
         if(!playersInSM.contains(player.getName()))
         {
            playersInSM.add(player.getName());

            if(player.isOnline())
            {
               player.sendMessage(ChatColor.GREEN + "Service-Modus AKTIV");
            }
         }
      }
   }

   public void disableServiceMode(Player player)
   {
      if(null != player)
      {
         if(playersInSM.contains(player.getName()))
         {
            playersInSM.remove(player.getName());

            if(player.isOnline())
            {
               player.sendMessage(ChatColor.RED + "Service-Modus AUS");
            }
         }
      }
   }
}
