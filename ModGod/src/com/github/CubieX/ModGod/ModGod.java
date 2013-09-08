/*
 * ModGod - A CraftBukkit plugin that gives limited god mode to moderators to fulfill service tasks
 * Copyright (C) 2013  CubieX
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.github.CubieX.ModGod;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class ModGod extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();

   private ModGod plugin = null;
   private ModGodCommandHandler myComHandler = null;
   private ModGodConfigHandler configHandler = null;
   private ModGodEntityListener eListener = null;
   private ModGodSchedulerHandler schedHandler = null;

   static boolean debug = false;
   static int warmUpTime = 5;
   static double gracePeriod = 1.5f;

   @Override
   public void onEnable()
   {
      this.plugin = this;
      configHandler = new ModGodConfigHandler(this);
      eListener = new ModGodEntityListener(this);
      myComHandler = new ModGodCommandHandler(this, configHandler);
      getCommand("mg").setExecutor(myComHandler);

      readConfigValues();

      schedHandler = new ModGodSchedulerHandler(plugin);

      log.info(this.getName() + " version " + getDescription().getVersion() + " is enabled!");
   }

   public void readConfigValues()
   {
      boolean exceed = false;
      boolean invalid = false;

      debug = configHandler.getConfig().getBoolean("debug");            
      warmUpTime = configHandler.getConfig().getInt("warmUpTime");

      if(warmUpTime < 0){warmUpTime = 0; exceed = true;}
      if(warmUpTime > 10){warmUpTime = 10; exceed = true;}

      gracePeriod = configHandler.getConfig().getDouble("gracePeriod");

      if(gracePeriod < 0){gracePeriod = 0; exceed = true;}
      if(gracePeriod > 10){gracePeriod = 10; exceed = true;}

      if(exceed)
      {
         log.warning("One or more config values are exceeding their allowed range. Please check your config file!");
      }

      if(invalid)
      {
         log.warning("One or more config values are invalid. Please check your config file!");
      }
   }

   @Override
   public void onDisable()
   {
      this.getServer().getScheduler().cancelTasks(this);
      schedHandler = null;
      myComHandler = null;
      eListener = null;
      configHandler = null;
      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");

   }
   
   // ###################################################################################################

   public BukkitTask startWarmUpTimer_DelayedTask(Player player)
   {
      return (schedHandler.startWarmUpTimer_Delayed(player));
   }

   public void activateServiceMode(Player player)
   {
      eListener.activateServiceMode(player);
   }

   public BukkitTask startGracePeriodTimer_DelayedTask(Player player)
   {
      return (schedHandler.startGracePeriodTimer_Delayed(player));
   }
   
   public BukkitTask startDelayedServiceModeCheckTimer_Task(Player player)
   {
      return (schedHandler.startDelayedServiceModeCheckTimer_Task(player));
   }
   
   public void disableServiceModeWithGrace(Player player)
   {
      eListener.disableServiceModeWithGrace(player);
   }
   
   public void handleServiceModeChecks(Player player, ItemStack item)
   {
      eListener.handleServiceModeChecks(player, item);
   }
}


