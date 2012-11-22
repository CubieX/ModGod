package com.github.CubieX.ModGod;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class ModGodSchedulerHandler
{
    private ModGod plugin = null;

    public ModGodSchedulerHandler(ModGod plugin)
    {
        this.plugin = plugin;
    }

    public void startPlayerHoldsItemTimeMeasureScheduler_Delayed(Player player, Item item)
    {      
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {                
                try
                {
                    
                }
                catch(Exception ex)
                {
                    // player probably no longer online
                }                
            }
        }, 20*5L); // 0 sec delay, 5 sec period        
    }
}
