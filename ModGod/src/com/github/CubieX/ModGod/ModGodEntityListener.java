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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ModGodEntityListener implements Listener
{
   Logger eLog;
   HashSet<String> playersInSM = new HashSet<String>();
   HashMap<String, BukkitTask> playersOnWarmup = new HashMap<String, BukkitTask>();
   HashMap<String, BukkitTask> playersInGracePeriod = new HashMap<String, BukkitTask>();
   HashMap<String, BukkitTask> playersScheduledForDelayedServiceModeCheck = new HashMap<String, BukkitTask>();
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

      handleServiceModeChecks(event.getPlayer(), event.getPlayer().getInventory().getItem(event.getNewSlot()));
   }

   //----------------------------------------------------------------------------------------------
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onInventoryClose(InventoryCloseEvent event)
   {
      if(event.getPlayer() instanceof Player)
      {
         Player player = (Player) event.getPlayer();
         handleServiceModeChecks(player, event.getPlayer().getItemInHand());         
      }
   }

   //----------------------------------------------------------------------------------------------------
   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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

   //----------------------------------------------------------------------------------------------------
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerChangedWorldEvent (PlayerChangedWorldEvent event)
   {

      if(event.getPlayer().getWorld().getEnvironment() == Environment.NETHER)
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

      if(event.getPlayer().getWorld().getEnvironment() == Environment.THE_END)
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

   //----------------------------------------------------------------------------------------------------    
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerItemPickup(PlayerPickupItemEvent event)
   {
      // CAUTION:This event fires, BEFORE the picked up item is added to the inventory!
      // therefore, delay all checks for 1 tick because "itemInHand()" is needed for checks

      // this event can be potentially fired every tick, so make sure the handling (with schedulers...) is as
      // efficient as possible for normal players

      // check if this player is already scheduled for a pickup check (must be happened last tick) and if so,
      // if the check has not yet been executed.
      // if so, cancel the scheduler and re-schedule the check
      // otherwise do some preconditions checks and if successful, schedule the check for next tick

      if(playersScheduledForDelayedServiceModeCheck.containsKey(event.getPlayer().getName()) &&
            (null != playersScheduledForDelayedServiceModeCheck.get(event.getPlayer().getName())))
      {
         // a new itemPickUpEvent has fired while the check routine that is scheduled for the
         // current tick has not yet been called for this player
         // so cancel the task and re-schedule it for the next tick
         playersScheduledForDelayedServiceModeCheck.get(event.getPlayer().getName()).cancel();         
      }

      if(preconditionsOK(event.getPlayer())) // may player use MG at all in current situation?
      {
         if(!playersInSM.contains(event.getPlayer().getName()) &&
               !playersInGracePeriod.containsKey(event.getPlayer().getName()) &&
               !playersOnWarmup.containsKey(event.getPlayer().getName())) // check only if player is NOT in SM and not already handled by any check timer
         {
            playersScheduledForDelayedServiceModeCheck.put(event.getPlayer().getName(),
                  plugin.startDelayedServiceModeCheckTimer_Task(event.getPlayer())); // this will schedule the check handling for next tick
         }
      }
   }

   //----------------------------------------------------------------------------------------------------
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerDropItem(PlayerDropItemEvent event)
   {
      // CAUTION:This event fires, BEFORE the dropped item is removed from the inventory!
      // therefore, delay all checks for 1 tick because "itemInHand()" is needed for checks

      // this event can be potentially fired every tick, (well...you need fast fingers)
      // so make sure the handling (with schedulers...) is as efficient as possible for normal players

      // check if this player is already scheduled for a delayed check (must be happened last tick) and if so,
      // if the check has not yet been executed.
      // if so, cancel the scheduler and re-schedule the check
      // otherwise do some preconditions checks and if successful, schedule the check for next tick

      if(playersScheduledForDelayedServiceModeCheck.containsKey(event.getPlayer().getName()) &&
            (null != playersScheduledForDelayedServiceModeCheck.get(event.getPlayer().getName())))
      {
         // a new PlayerDropItemEvent has fired while the check routine that is scheduled for the
         // current tick has not yet been called for this player
         // so cancel the task and re-schedule it for the next tick
         playersScheduledForDelayedServiceModeCheck.get(event.getPlayer().getName()).cancel();         
      }

      if(preconditionsOK(event.getPlayer())) // may player use MG at all in current situation?
      {
         boolean doCheck = false;

         if(playersInSM.contains(event.getPlayer().getName()))
         {
            if(!playersInGracePeriod.containsKey(event.getPlayer().getName()))
            {
               doCheck = true;               
            }
         }
         else
         {
            if(playersOnWarmup.containsKey(event.getPlayer().getName()))
            {
               doCheck = true;
            }
         }

         if(doCheck)
         {
            // check only if player is in SM and not already handled by any timer
            playersScheduledForDelayedServiceModeCheck.put(event.getPlayer().getName(),
                  plugin.startDelayedServiceModeCheckTimer_Task(event.getPlayer())); // this will schedule the check handling for next tick
         }
      }
   }

   //-------------------------------------------------------------------------------
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onBlockPlace(BlockPlaceEvent event)
   {
      // CAUTION:This event fires, BEFORE the placed block is removed from the inventory!
      // therefore, delay all checks for 1 tick because "itemInHand()" is needed for checks

      // this event can be potentially fired every tick, (well...you need fast fingers)
      // so make sure the handling (with schedulers...) is as efficient as possible for normal players

      // check if this player is already scheduled for a delayed check (must be happened last tick) and if so,
      // if the check has not yet been executed.
      // if so, cancel the scheduler and re-schedule the check
      // otherwise do some preconditions checks and if successful, schedule the check for next tick

      if(playersScheduledForDelayedServiceModeCheck.containsKey(event.getPlayer().getName()) &&
            (null != playersScheduledForDelayedServiceModeCheck.get(event.getPlayer().getName())))
      {
         // a new BlockPlaceEvent has fired while the check routine that is scheduled for the
         // current tick has not yet been called for this player
         // so cancel the task and re-schedule it for the next tick
         playersScheduledForDelayedServiceModeCheck.get(event.getPlayer().getName()).cancel();         
      }

      if(preconditionsOK(event.getPlayer())) // may player use MG at all in current situation?
      {
         boolean doCheck = false;

         if(playersInSM.contains(event.getPlayer().getName()))
         {
            if(!playersInGracePeriod.containsKey(event.getPlayer().getName()))
            {
               doCheck = true;               
            }
         }
         else
         {
            if(playersOnWarmup.containsKey(event.getPlayer().getName()))
            {
               doCheck = true;
            }
         }

         if(doCheck)
         {
            // check only if player is in SM and not already handled by any timer
            playersScheduledForDelayedServiceModeCheck.put(event.getPlayer().getName(),
                  plugin.startDelayedServiceModeCheckTimer_Task(event.getPlayer())); // this will schedule the check handling for next tick
         }
      }
   }

   //---------------------------------------------------------------------------------
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerQuit(PlayerQuitEvent event)
   {
      // cleanup
      if(playersInSM.contains(event.getPlayer().getName()))
      {
         playersInSM.remove(event.getPlayer().getName());
      }

      if(playersInGracePeriod.containsKey(event.getPlayer().getName()))
      {
         playersInGracePeriod.remove(event.getPlayer().getName());
      }

      if(playersOnWarmup.containsKey(event.getPlayer().getName()))
      {
         playersOnWarmup.remove(event.getPlayer().getName());
      }
   }

   // ##########################################################################################

   public void handleServiceModeChecks(Player player, ItemStack item)
   {
      if(null != player) // this may be called by a scheduler, so make sure those references are still valid!
      {
         if(playersScheduledForDelayedServiceModeCheck.containsKey(player.getName())) // delete the player from the list of scheduled checks, as he is now beeing handled
         {
            playersScheduledForDelayedServiceModeCheck.remove(player.getName());
         }

         if(preconditionsOK(player))
         {
            boolean doContinue = false;

            if (null != item) // is null if empty slot when changing the item via PlayerItemHeldEvent.
            {
               if(ModGod.debug){ModGod.log.info("Item: " + item.getType().toString());}

               if(plugin.getConfig().getStringList("serviceItems").contains(item.getType().toString()))
               {
                  if(ModGod.debug){ModGod.log.info("ServiceItem " + item.getType().toString() + " erkannt.");}
                  doContinue = true;                     
               }
            }
            // empty hand will also be treated as non-service-item

            if(doContinue && !playersInSM.contains(player.getName()))
            {
               if(ModGod.warmUpTime > 0) // only use timer and playersOnWarmup HashMap if value is > 0 in config
               {
                  if((!playersOnWarmup.containsKey(player.getName())) && (!playersInSM.contains(player.getName())))
                  {
                     player.sendMessage(ChatColor.YELLOW + "Service-Modus in " + ModGod.warmUpTime + " s...");                     
                     playersOnWarmup.put(player.getName(), plugin.startWarmUpTimer_DelayedTask(player));                     
                  }
               }
               else
               {
                  playersInSM.add(player.getName());
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
            else // Player with permission has no service item in hand, so start grace Timer (if set) if he is currently in service mode
            {
               if(ModGod.debug){ModGod.log.info("Kein ServiceItem erkannt");}

               if(playersInSM.contains(player.getName()))
               {
                  if(ModGod.gracePeriod > 0) // only use timer and playersInGracePeriod HashMap if value is > 0 in config
                  {
                     if(!playersInGracePeriod.containsKey(player.getName()))
                     {
                        playersInGracePeriod.put(player.getName(), plugin.startGracePeriodTimer_DelayedTask(player));                     
                     }
                  }
                  else
                  {
                     playersInSM.remove(player);                     
                  }
               }
               else
               {
                  abortWarmUp(player);
               }
            }
         }         
      }
   }

   private boolean preconditionsOK(Player player)
   {
      boolean res = false;

      // CHECK 1
      if(!player.isOnline()) // this may happen because "player" reference may be delivered by a scheduler
      {
         return false;
      }

      // CHECK 2
      if(!player.isOp()) //if player is op, he does not need service mode. He can use godmode from game.
      {
         // CHECK 3
         if(player.getWorld().getEnvironment() == Environment.NETHER)
         {
            if(!hasNetherPerms(player))
            {
               return false; // player is in the nether but has no permission to use ModGod in the nether. So leave handler.
            }
         }

         // CHECK 4
         if(player.getWorld().getEnvironment() == Environment.THE_END)
         {
            return false; // player is in the end. ModGod does not work here. So leave handler.                
         }

         // CHECK 5
         if(player.hasPermission("modgod.service"))
         {
            if(ModGod.debug){ModGod.log.info("permission erkannt");}
            res = true;
         }
      }
      else
      {
         return false;
      }

      return res; // only reached if all checks were successful
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
      if(null != player)
      {
         if(playersOnWarmup.containsKey(player.getName()))
         {
            // set player in service mode only, if he has held the items for the full warm-up duration
            playersOnWarmup.remove(player.getName());  
         }

         enableServiceMode(player);
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
