package com.github.CubieX.ModGod;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ModGod extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();

   private ModGod plugin = null;
   private ModGodCommandHandler myComHandler = null;
   private ModGodConfigHandler configHandler = null;
   private ModGodEntityListener eListener = null;

   static boolean debug = false;

   @Override
   public void onEnable()
   {
      this.plugin = this;
      configHandler = new ModGodConfigHandler(this);
      eListener = new ModGodEntityListener(this, configHandler);
      myComHandler = new ModGodCommandHandler(this, configHandler);
      getCommand("mg").setExecutor(myComHandler);

      log.info(this.getName() + " version " + getDescription().getVersion() + " is enabled!");
   }

   public void readConfigValues()
   {
      if("true".equalsIgnoreCase(this.getConfig().getString("debug")))
      {
         debug = configHandler.getConfig().getBoolean("debug");
      }
   }

   @Override
   public void onDisable()
   {
      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
      configHandler = null;
   }    
}


