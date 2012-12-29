package com.github.CubieX.ModGod;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class ModGod extends JavaPlugin
{
   Logger log;
   ArrayList<String> playersInSM = new ArrayList<String>();

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
      eListener = new ModGodEntityListener(this, configHandler, log);        
      log = this.getLogger();
      log.info("ModGod version " + getDescription().getVersion() + " is enabled!");

      myComHandler = new ModGodCommandHandler(this, configHandler, log);
      getCommand("mg").setExecutor(myComHandler);       
   }

   public void readConfigValues()
   {
      if("true".equalsIgnoreCase(this.getConfig().getString("debug")))
      {
         debug = true;
      }
      else
      {
         debug = false;
      }


   }

   @Override
   public void onDisable()
   {
      log.info("ModGod version " + getDescription().getVersion() + " is disabled!");
      configHandler = null;
   }    
}


