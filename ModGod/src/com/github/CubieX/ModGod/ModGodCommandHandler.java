package com.github.CubieX.ModGod;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ModGodCommandHandler implements CommandExecutor
{
   private ModGod plugin = null;
   private ModGodConfigHandler configHandler = null;
   private Logger log = null;

   public ModGodCommandHandler(ModGod plugin, ModGodConfigHandler configHandler, Logger log) 
   {
      this.plugin = plugin;
      this.configHandler = configHandler;
      this.log = log;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
      Player player = null;
      if (sender instanceof Player) 
      {
         player = (Player) sender;
      }

      if(ModGod.debug){log.info("onCommand");}
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
               sender.sendMessage(ChatColor.YELLOW + "This server is running ModGod version " + plugin.getDescription().getVersion());
               return true;
            }    
            if (args[0].equalsIgnoreCase("reload")) // argument 0 is given and correct
            {            
               if(sender.hasPermission("modgod.reload"))
               {                        
                  configHandler.reloadConfig(sender);
                  sender.sendMessage("[" + ChatColor.BLUE + "Info" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "ModGod " + plugin.getDescription().getVersion() + " reloaded!");
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
            sender.sendMessage(ChatColor.YELLOW + "Ung�ltige Anzahl Argumente.");
         }                

      }         
      return false; // if false is returned, the help for the command stated in the plugin.yml will be displayed to the player
   }
}
