package com.puk3p.chestscanner.listeners;

import com.puk3p.chestscanner.storage.OwnerStore;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class OwnershipListener implements Listener {
    private final OwnerStore owners;

    public OwnershipListener(JavaPlugin plugin) {
        this.owners = new OwnerStore(plugin);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Block b = e.getBlockPlaced();
        if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
            Player p = e.getPlayer();
            owners.setOwner(b.getLocation(), p.getUniqueId());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
            owners.clearOwner(b.getLocation());
        }
    }
}
