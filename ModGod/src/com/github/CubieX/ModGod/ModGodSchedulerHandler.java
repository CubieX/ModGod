package com.github.CubieX.ModGod;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ModGodSchedulerHandler
{
   private ModGod plugin = null;   

   public ModGodSchedulerHandler(ModGod plugin)
   {
      this.plugin = plugin;
   }

   public BukkitTask startWarmUpTimer_Delayed(final Player p)
   {
      BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
      {
         public void run()
         {                
            plugin.activateServiceMode(p);
         }
      }, 20L * ModGod.warmUpTime); // delay defined in config

      return (task);
   }

   public BukkitTask startGracePeriodTimer_Delayed(final Player p)
   {
      BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
      {
         public void run()
         {
            plugin.disableServiceModeWithGrace(p);
         }
      }, (long) (Math.round(20 * ModGod.gracePeriod))); // delay defined in config

      return (task);
   }

   public BukkitTask startDelayedServiceModeCheckTimer_Task(final Player p)
   {
      BukkitTask task = plugin.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         public void run()
         {
            plugin.handleServiceModeChecks(p, p.getItemInHand()); // run on next tick
         }
      }); 

      return (task);
   }
}
