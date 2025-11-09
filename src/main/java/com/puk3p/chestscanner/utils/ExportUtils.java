package com.puk3p.chestscanner.utils;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ExportUtils {

    public static File exportYaml(File folder, String name, List<Location> list) throws Exception {
        YamlConfiguration yml = new YamlConfiguration();
        int i = 1;
        for (Location l : list) {
            String base = "results." + i++;
            yml.set(base + ".world", l.getWorld().getName());
            yml.set(base + ".x", l.getBlockX());
            yml.set(base + ".y", l.getBlockY());
            yml.set(base + ".z", l.getBlockZ());
            yml.set(base + ".chunkX", l.getChunk().getX());
            yml.set(base + ".chunkZ", l.getChunk().getZ());
        }
        File out = new File(folder, name + ".yml");
        yml.save(out);
        return out;
    }

    public static File exportCsv(File folder, String name, List<Location> list) throws Exception {
        File out = new File(folder, name + ".csv");
        try (FileWriter fw = new FileWriter(out)) {
            fw.write("world,x,y,z,chunkX,chunkZ\n");
            for (Location l : list) {
                fw.write(String.format("%s,%d,%d,%d,%d,%d%n",
                        l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ(),
                        l.getChunk().getX(), l.getChunk().getZ()));
            }
        }
        return out;
    }
}
