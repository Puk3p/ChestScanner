package com.puk3p.chestscanner.storage;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

public class OwnerStore {
    private final File file;
    private final YamlConfiguration yml;

    public OwnerStore(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "owners.yml");
        this.yml = YamlConfiguration.loadConfiguration(file);
    }

    private String key(Location l) {
        return l.getWorld().getName()+":"+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
    }

    public void setOwner(Location loc, UUID owner) {
        yml.set(key(loc), owner.toString());
        save();
    }

    public UUID getOwner(Location loc) {
        String s = yml.getString(key(loc));
        return s == null ? null : UUID.fromString(s);
    }

    public void clearOwner(Location loc) {
        yml.set(key(loc), null);
        save();
    }

    private void save() {
        try { yml.save(file); } catch (Exception ignored) {}
    }
}
