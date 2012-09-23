package com.github.CubieX.ModGod;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ModGodCommandHandler implements CommandExecutor
{
    private ModGod plugin;

    public ModGodCommandHandler(ModGod plugin) 
    {
        this.plugin = plugin;
    }

    // Command Handler ------------------------------------------------------------------------------
    // player typed a command
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) 
        {
            player = (Player) sender;
        }

        if (cmd.getName().equalsIgnoreCase("mg"))
        { // If the player typed /mf then do the following... (can be run from console also)
            if (args.length == 0)
            { //no arguments, so help will be displayed
                return false;
            }
            if (args[0].equalsIgnoreCase("version")) // argument 0 is given and correct
            {            
                sender.sendMessage(ChatColor.YELLOW + "This server is running ModGod version " + plugin.getDescription().getVersion());
                return true;
            }    
            if (args[0].equalsIgnoreCase("reload")) // argument 0 is given and correct
            {            
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                plugin.getServer().getPluginManager().enablePlugin(plugin);
                sender.sendMessage("[" + ChatColor.BLUE + "Info" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "ModGod Reloaded!");
                return true;
            }
            
        }         
        return false; // if false is returned, the help for the command stated in the plugin.yml will be displayed to the player
    }
}
