package com.puk3p.chestscanner.utils;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Method;

public class InvHolderUtils {

    public static Location getChestLocation(Inventory inv) {
        InventoryHolder h = inv.getHolder();
        if (h instanceof Chest) {
            return ((Chest) h).getLocation();
        }

        // Detectează DoubleChest fără import
        if (h != null && "org.bukkit.inventory.DoubleChest".equals(h.getClass().getName())) {
            try {
                Method getLeft = h.getClass().getMethod("getLeftSide");
                Method getRight = h.getClass().getMethod("getRightSide");
                Object left = getLeft.invoke(h);
                Object right = getRight.invoke(h);

                Location a = (left instanceof Chest) ? ((Chest) left).getLocation() : null;
                Location b = (right instanceof Chest) ? ((Chest) right).getLocation() : null;

                if (a != null && b != null) return canonical(a, b);
                if (a != null) return a;
                if (b != null) return b;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Location canonical(Location a, Location b) {
        if (a.getBlockX() < b.getBlockX()) return a;
        if (a.getBlockX() > b.getBlockX()) return b;
        return (a.getBlockZ() <= b.getBlockZ()) ? a : b;
    }
}
