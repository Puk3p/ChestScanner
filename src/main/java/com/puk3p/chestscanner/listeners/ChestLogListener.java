package com.puk3p.chestscanner.listeners;

import com.puk3p.chestscanner.utils.InvHolderUtils;
import com.puk3p.chestscanner.utils.InvSnapshot;
import com.puk3p.chestscanner.storage.LogStore;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestLogListener implements Listener {
    private final Map<UUID, Session> open = new HashMap<>();
    private final LogStore logs;

    public ChestLogListener(JavaPlugin plugin) {
        this.logs = new LogStore(plugin);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        Inventory inv = e.getInventory();
        Location chestLoc = InvHolderUtils.getChestLocation(inv);
        if (chestLoc == null) return;

        HumanEntity who = e.getPlayer();
        open.put(who.getUniqueId(), new Session(chestLoc, InvSnapshot.snapshot(inv)));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        Session s = open.remove(id);
        if (s == null) return;

        Inventory inv = e.getInventory();
        Location chestLoc = InvHolderUtils.getChestLocation(inv);
        if (chestLoc == null) return;

        InvSnapshot.Diff diff = InvSnapshot.diff(s.before, InvSnapshot.snapshot(inv));
        if (diff.isEmpty()) return;

        logs.append(chestLoc, e.getPlayer().getName(), diff);
    }

    private static class Session {
        final Location loc;
        final Map<String, Integer> before;
        Session(Location l, Map<String, Integer> b) { this.loc=l; this.before=b; }
    }
}
