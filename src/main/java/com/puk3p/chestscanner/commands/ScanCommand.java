package com.puk3p.chestscanner.commands;

import com.puk3p.chestscanner.ChestScanner;
import com.puk3p.chestscanner.tasks.ScanTask;
import com.puk3p.chestscanner.utils.ExportUtils;
import com.puk3p.chestscanner.utils.TeleportUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class ScanCommand implements CommandExecutor {

    private final ChestScanner plugin;

    public ScanCommand(ChestScanner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (!(sender instanceof Player)) { sender.sendMessage("Doar jucatorii."); return true; }
        Player p = (Player) sender;
        if (!p.hasPermission("chestscanner.scan")) { p.sendMessage("Fara permisiune."); return true; }

        if (a.length == 0 || a[0].equalsIgnoreCase("start")) {
            int count = (a.length >= 2) ? clampInt(a[1], 1, 5000, 100) : 100;
            boolean load = (a.length >= 3) && a[2].equalsIgnoreCase("load");
            Material filter = null; // fara filtru
            startTask(p, count, load, filter);
            return true;
        }

        if (a[0].equalsIgnoreCase("find")) {
            if (a.length < 2) { p.sendMessage("Uso: /scan find <MATERIAL> [count] [load]"); return true; }
            Material m = Material.matchMaterial(a[1]);
            if (m == null) { p.sendMessage("Material invalid: " + a[1]); return true; }
            int count = (a.length >= 3) ? clampInt(a[2], 1, 5000, 100) : 100;
            boolean load = (a.length >= 4) && a[3].equalsIgnoreCase("load");
            startTask(p, count, load, m);
            return true;
        }

        if (a[0].equalsIgnoreCase("stop")) {
            boolean ok = plugin.cancelTask(p.getUniqueId());
            p.sendMessage(ok ? ChatColor.YELLOW + "Scan oprit." : ChatColor.RED + "Nu aveai scan activ.");
            return true;
        }

        if (a[0].equalsIgnoreCase("list")) {
            int page = (a.length >= 2) ? clampInt(a[1], 1, 999, 1) : 1;
            List<Location> list = plugin.getRegistry().get(p.getUniqueId());
            if (list.isEmpty()) { p.sendMessage("Nu ai rezultate. Ruleaza /scan start"); return true; }
            int per = 10, from = (page - 1) * per, to = Math.min(from + per, list.size());
            p.sendMessage(ChatColor.AQUA + "Rezultate (" + list.size() + "), pagina " + page + ":");
            for (int i = from; i < to; i++) {
                Location l = list.get(i);
                p.sendMessage(ChatColor.GRAY + String.format(" %d) %s @ %d,%d,%d", i+1,
                        l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()));
            }
            return true;
        }

        if (a[0].equalsIgnoreCase("tp")) {
            if (!p.hasPermission("chestscanner.tp")) { p.sendMessage("Fara permisiune."); return true; }
            if (a.length < 2) { p.sendMessage("Uso: /scan tp <index>"); return true; }
            int idx = clampInt(a[1], 1, 1_000_000, -1);
            Location target = plugin.getRegistry().getByIndex(p.getUniqueId(), idx);
            if (target == null) { p.sendMessage("Index invalid."); return true; }
            Location safe = TeleportUtils.findSafeSpotNear(target);
            p.teleport(safe);
            p.sendMessage(ChatColor.GREEN + "Teleportat la " + safe.getBlockX() + "," + safe.getBlockY() + "," + safe.getBlockZ());
            return true;
        }

        if (a[0].equalsIgnoreCase("export")) {
            if (!p.hasPermission("chestscanner.export")) { p.sendMessage("Fara permisiune."); return true; }
            if (a.length < 2) { p.sendMessage("Uso: /scan export <csv|yml>"); return true; }
            List<Location> list = plugin.getRegistry().get(p.getUniqueId());
            if (list.isEmpty()) { p.sendMessage("Nu ai rezultate."); return true; }
            try {
                File folder = plugin.getDataFolder();
                folder.mkdirs();
                String name = "scan-" + p.getUniqueId().toString().substring(0, 8);
                if (a[1].equalsIgnoreCase("csv")) {
                    File f = ExportUtils.exportCsv(folder, name, list);
                    p.sendMessage(ChatColor.YELLOW + "Export CSV: " + f.getName());
                } else {
                    File f = ExportUtils.exportYaml(folder, name, list);
                    p.sendMessage(ChatColor.YELLOW + "Export YML: " + f.getName());
                }
            } catch (Exception ex) {
                p.sendMessage(ChatColor.RED + "Eroare la export: " + ex.getMessage());
            }
            return true;
        }

        if (a[0].equalsIgnoreCase("owner")) {
            if (a.length < 2) { p.sendMessage("Uso: /scan owner <index>"); return true; }
            int idx = clampInt(a[1], 1, 1_000_000, -1);
            org.bukkit.Location loc = plugin.getRegistry().getByIndex(p.getUniqueId(), idx);
            if (loc == null) { p.sendMessage("Index invalid."); return true; }

            com.puk3p.chestscanner.storage.OwnerStore owners = new com.puk3p.chestscanner.storage.OwnerStore(plugin);
            java.util.UUID u = owners.getOwner(loc);
            if (u == null) { p.sendMessage("Niciun owner înregistrat pentru acest chest."); return true; }
            String name = org.bukkit.Bukkit.getOfflinePlayer(u).getName();
            p.sendMessage("Owner: " + (name!=null ? name : u.toString()));
            return true;
        }

        if (a[0].equalsIgnoreCase("who")) {
            if (a.length < 2) { p.sendMessage("Uso: /scan who <index> [linii]"); return true; }
            int idx = clampInt(a[1], 1, 1_000_000, -1);
            int lines = (a.length >= 3) ? clampInt(a[2], 1, 50, 10) : 10;

            org.bukkit.Location loc = plugin.getRegistry().getByIndex(p.getUniqueId(), idx);
            if (loc == null) { p.sendMessage("Index invalid."); return true; }

            com.puk3p.chestscanner.storage.LogStore ls = new com.puk3p.chestscanner.storage.LogStore(plugin);
            java.util.List<String> tail = ls.tail(loc, lines);
            if (tail.isEmpty()) {
                p.sendMessage("Nu există log pentru acest chest.");
            } else {
                com.puk3p.chestscanner.utils.LogPretty.sendPrettyLog(p, tail);
            }
            return true;
        }

        // help scurt
        p.sendMessage(ChatColor.YELLOW + "Uso: /scan start [count] [load] | find <MAT> [count] [load] | list | tp <i> | export <csv|yml> | stop");
        return true;
    }

    private void startTask(Player p, int count, boolean load, Material filter) {
        UUID id = p.getUniqueId();
        BukkitTask t = new ScanTask(plugin, p, count, load, filter).runTaskTimer(plugin, 1L, 1L);
        plugin.registerTask(id, t);
    }

    private int clampInt(String s, int min, int max, int def) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(s))); } catch (Exception e) { return def; }
    }
}