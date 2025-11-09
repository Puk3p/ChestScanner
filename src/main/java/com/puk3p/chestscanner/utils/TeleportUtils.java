package com.puk3p.chestscanner.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class TeleportUtils {

    public static Location findSafeSpotNear(Location around) {
        World w = around.getWorld();
        int bx = around.getBlockX();
        int by = Math.min(Math.max(around.getBlockY() + 1, 2), w.getMaxHeight() - 3);
        int bz = around.getBlockZ();

        int[][] offsets = {
                {0,0},{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1},
                {2,0},{-2,0},{0,2},{0,-2}
        };

        for (int dy = 0; dy <= 3; dy++) {
            for (int[] off : offsets) {
                int x = bx + off[0];
                int z = bz + off[1];
                int y = by + dy;

                Block feet = w.getBlockAt(x, y, z);
                Block head = w.getBlockAt(x, y + 1, z);
                Block below = w.getBlockAt(x, y - 1, z);

                if (isAir(feet) && isAir(head) && below.getType().isSolid()) {
                    return new Location(w, x + 0.5, y, z + 0.5, around.getYaw(), around.getPitch());
                }
            }
        }
        // fallback: deasupra locației
        return around.clone().add(0.5, 1, 0.5);
    }

    private static boolean isAir(Block b) {
        Material m = b.getType();
        return m == Material.AIR;
    }
}