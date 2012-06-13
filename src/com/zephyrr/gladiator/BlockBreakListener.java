package com.zephyrr.gladiator;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author Phoenix
 */
public class BlockBreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().isOp())
            return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(!event.getPlayer().isOp());
    }
}
