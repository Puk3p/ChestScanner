package com.puk3p.chestscanner.tasks;

import com.puk3p.chestscanner.ChestScanner;
import com.puk3p.chestscanner.utils.InvUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

public class FullMapScanTask extends BukkitRunnable {
    private final ChestScanner plugin;
    private final UUID playerId;
    private final World world;
    private final int minChunkX;
    private final int maxChunkX;
    private final int minChunkZ;
    private final int maxChunkZ;
    private final long targetChunks;
    private final boolean attemptLoad;
    private final Material filterMat;
    private final long startTime;
    private final int chunksPerTick;
    private final int progressEvery;
    private final int maxShow;

    private int currentX;
    private int currentZ;
    private long processed = 0;
    private long scanned = 0;
    private int lastProcessedChunkX;
    private int lastProcessedChunkZ;
    private final List<Location> found = new ArrayList<>();

    public FullMapScanTask(
            ChestScanner plugin,
            Player player,
            int minChunkX,
            int maxChunkX,
            int minChunkZ,
            int maxChunkZ,
            boolean attemptLoad,
            Material filterMat) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();
        this.world = player.getWorld();
        this.minChunkX = minChunkX;
        this.maxChunkX = maxChunkX;
        this.minChunkZ = minChunkZ;
        this.maxChunkZ = maxChunkZ;
        this.targetChunks = (long) (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
        this.attemptLoad = attemptLoad;
        this.filterMat = filterMat;
        this.startTime = System.currentTimeMillis();
        this.chunksPerTick = Math.max(1, plugin.getConfig().getInt("chunks-per-tick", 2));
        this.progressEvery = Math.max(5, plugin.getConfig().getInt("progress-every", 20));
        this.maxShow = Math.max(5, plugin.getConfig().getInt("max-list-show", 30));
        this.currentX = minChunkX;
        this.currentZ = minChunkZ;
        this.lastProcessedChunkX = minChunkX;
        this.lastProcessedChunkZ = minChunkZ;

        player.sendMessage(
                ChatColor.RED
                        + "[Scan] FULL MAP MODE active. This can lag heavily."
                        + ChatColor.YELLOW
                        + " Chunks: "
                        + targetChunks
                        + ", bounds chunks X:["
                        + minChunkX
                        + ".."
                        + maxChunkX
                        + "] Z:["
                        + minChunkZ
                        + ".."
                        + maxChunkZ
                        + "], "
                        + (attemptLoad ? "with load" : "no load")
                        + (filterMat != null ? (", filter: " + filterMat.name()) : ""));
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            cancel();
            return;
        }

        int steps = 0;
        while (hasNext() && steps < chunksPerTick) {
            int cx = currentX;
            int cz = currentZ;
            advance();
            processed++;
            lastProcessedChunkX = cx;
            lastProcessedChunkZ = cz;

            boolean loaded = world.isChunkLoaded(cx, cz);
            if (!loaded && attemptLoad && player.hasPermission("chestscanner.load")) {
                try {
                    loaded = world.loadChunk(cx, cz, false);
                } catch (Throwable ignored) {
                    loaded = false;
                }
            }

            if (!loaded) {
                steps++;
                continue;
            }

            scanChunkForChests(world.getChunkAt(cx, cz));
            scanned++;
            steps++;
        }

        if (processed % progressEvery == 0 || !hasNext()) {
            player.sendMessage(
                    ChatColor.GRAY
                            + "[Scan] "
                            + processed
                            + "/"
                            + targetChunks
                            + " proc., "
                            + scanned
                            + " scan., "
                            + found.size()
                            + " gasite. @ chunk "
                            + lastProcessedChunkX
                            + ","
                            + lastProcessedChunkZ
                            + " (block "
                            + (lastProcessedChunkX * 16)
                            + ","
                            + (lastProcessedChunkZ * 16)
                            + ")");
        }

        if (!hasNext()) {
            long ms = System.currentTimeMillis() - startTime;
            player.sendMessage(
                    ChatColor.GREEN
                            + "[Scan] Full map gata in "
                            + ms
                            + " ms. Rezultate: "
                            + found.size());

            plugin.getRegistry().set(playerId, found);

            int show = Math.min(maxShow, found.size());
            for (int i = 0; i < show; i++) {
                sendClickableLine(player, i + 1, found.get(i));
            }
            if (found.size() > show) {
                player.sendMessage(
                        ChatColor.AQUA
                                + "… si inca "
                                + (found.size() - show)
                                + " rezultate. Foloseste /scan list");
            }
            cancel();
        }
    }

    private boolean hasNext() {
        return currentX <= maxChunkX;
    }

    private void advance() {
        currentZ++;
        if (currentZ > maxChunkZ) {
            currentZ = minChunkZ;
            currentX++;
        }
    }

    private void scanChunkForChests(Chunk chunk) {
        try {
            for (BlockState st : chunk.getTileEntities()) {
                if (st instanceof Chest) {
                    Chest chest = (Chest) st;
                    Inventory inv = chest.getInventory();
                    if (inv == null) continue;
                    if (chest.getBlock().getType() == Material.ENDER_CHEST) continue;

                    boolean ok =
                            (filterMat == null)
                                    ? InvUtils.hasItems(inv)
                                    : InvUtils.containsMaterial(inv, filterMat);

                    if (ok) found.add(chest.getLocation());
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void sendClickableLine(Player p, int index, Location loc) {
        TextComponent base =
                new TextComponent(
                        String.format(
                                " %d) %s @ %d,%d,%d (c %d,%d) ",
                                index,
                                loc.getWorld().getName(),
                                loc.getBlockX(),
                                loc.getBlockY(),
                                loc.getBlockZ(),
                                loc.getChunk().getX(),
                                loc.getChunk().getZ()));
        base.setColor(ChatColor.AQUA);

        TextComponent tp = new TextComponent("[TP]");
        tp.setColor(ChatColor.GREEN);
        tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scan tp " + index));

        p.spigot().sendMessage(base, tp);
    }
}
