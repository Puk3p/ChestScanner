package com.puk3p.chestscanner.tasks;

import com.puk3p.chestscanner.ChestScanner;
import com.puk3p.chestscanner.utils.ChunkSpiral;
import com.puk3p.chestscanner.utils.InvUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class ScanTask extends BukkitRunnable {
    private final ChestScanner plugin;
    private final UUID playerId;
    private final World world;
    private final Queue<int[]> spiralQueue;
    private final int targetChunks;
    private final boolean attemptLoad;
    private final Material filterMat; // poate fi null
    private final long startTime;
    private final int chunksPerTick;
    private final int progressEvery;
    private final int maxShow;

    private int processed = 0;
    private int scanned = 0;
    private final List<Location> found = new ArrayList<>();

    public ScanTask(ChestScanner plugin, Player player, int targetChunks, boolean attemptLoad, Material filterMat) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();
        this.world = player.getWorld();
        this.targetChunks = targetChunks;
        this.attemptLoad = attemptLoad;
        this.filterMat = filterMat;

        Chunk center = player.getLocation().getChunk();
        this.spiralQueue = ChunkSpiral.generate(center.getX(), center.getZ(), targetChunks);
        this.startTime = System.currentTimeMillis();

        this.chunksPerTick = Math.max(1, plugin.getConfig().getInt("chunks-per-tick", 2));
        this.progressEvery  = Math.max(5, plugin.getConfig().getInt("progress-every", 20));
        this.maxShow = Math.max(5, plugin.getConfig().getInt("max-list-show", 30));

        player.sendMessage(ChatColor.YELLOW + "[Scan] " +
                "Start: " + targetChunks + " chunk-uri, " +
                (attemptLoad ? "with load" : "no load") +
                (filterMat != null ? (", filter: " + filterMat.name()) : ""));
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            cancel();
            return;
        }

        int steps = 0;
        while (!spiralQueue.isEmpty() && steps < chunksPerTick) {
            int[] c = spiralQueue.poll();
            processed++;

            int cx = c[0], cz = c[1];

            boolean loaded = world.isChunkLoaded(cx, cz);
            if (!loaded && attemptLoad && player.hasPermission("chestscanner.load")) {
                try { loaded = world.loadChunk(cx, cz, false); } catch (Throwable ignored) { loaded = false; }
            }

            if (!loaded) { steps++; continue; }

            scanChunkForChests(world.getChunkAt(cx, cz));
            scanned++; steps++;
        }

        if (processed % progressEvery == 0 || spiralQueue.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "[Scan] " + processed + "/" + targetChunks +
                    " proc., " + scanned + " scan., " + found.size() + " gasite.");
        }

        if (spiralQueue.isEmpty()) {
            long ms = System.currentTimeMillis() - startTime;
            player.sendMessage(ChatColor.GREEN + "[Scan] Gata in " + ms + " ms. Rezultate: " + found.size());

            // salveaza pentru TP/list/export
            plugin.getRegistry().set(playerId, found);

            // arata primele N cu [TP] clicabil
            int show = Math.min(maxShow, found.size());
            for (int i = 0; i < show; i++) {
                sendClickableLine(player, i + 1, found.get(i));
            }
            if (found.size() > show) {
                player.sendMessage(ChatColor.AQUA + "… si inca " + (found.size() - show) + " rezultate. Foloseste /scan list");
            }
            cancel();
        }
    }

    private void scanChunkForChests(Chunk chunk) {
        try {
            for (BlockState st : chunk.getTileEntities()) {
                if (st instanceof Chest) {
                    Chest chest = (Chest) st;
                    Inventory inv = chest.getInventory();
                    if (inv == null) continue;
                    // IGNORA EnderChest (inv-ul e personal)
                    if (chest.getBlock().getType() == Material.ENDER_CHEST) continue;

                    boolean ok = (filterMat == null)
                            ? InvUtils.hasItems(inv)
                            : InvUtils.containsMaterial(inv, filterMat);

                    if (ok) found.add(chest.getLocation());
                }
            }
        } catch (Throwable ignored) { }
    }

    private void sendClickableLine(Player p, int index, Location loc) {
        // textul de baza (fara coduri §)
        TextComponent base = new TextComponent(String.format(
                " %d) %s @ %d,%d,%d (c %d,%d) ",
                index, loc.getWorld().getName(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                loc.getChunk().getX(), loc.getChunk().getZ()
        ));
        base.setColor(ChatColor.AQUA);

        // butonul [TP] clicabil
        TextComponent tp = new TextComponent("[TP]");
        tp.setColor(ChatColor.GREEN);
        tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scan tp " + index));

        // trimite cele doua componente alaturate
        p.spigot().sendMessage(base, tp);
    }
}