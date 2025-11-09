package com.puk3p.chestscanner;

import com.puk3p.chestscanner.commands.ScanCommand;
import com.puk3p.chestscanner.storage.LastScanRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChestScanner extends JavaPlugin {

    private final LastScanRegistry registry = new LastScanRegistry();
    private final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig(); // doar dacă ai config.yml în resources

        // comanda
        if (getCommand("scan") != null) {
            getCommand("scan").setExecutor(new com.puk3p.chestscanner.commands.ScanCommand(this));
        }

        // listeners
        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new com.puk3p.chestscanner.listeners.OwnershipListener(this), this);
        pm.registerEvents(new com.puk3p.chestscanner.listeners.ChestLogListener(this), this);

        getLogger().info("[ChestScanner] enabled");
    }



    public LastScanRegistry getRegistry() {
        return registry;
    }

    public void registerTask(UUID id, BukkitTask task) {
        cancelTask(id);
        activeTasks.put(id, task);
    }

    public boolean cancelTask(UUID id) {
        BukkitTask t = activeTasks.remove(id);
        if (t != null) { t.cancel(); return true; }
        return false;
    }
}
