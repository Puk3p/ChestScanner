package com.puk3p.chestscanner.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class InvUtils {
    public static boolean hasItems(Inventory inv) {
        if (inv == null) return false;
        ItemStack[] contents = inv.getContents();
        if (contents == null) return false;
        for (ItemStack it : contents) {
            if (it != null && it.getType() != Material.AIR && it.getAmount() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsMaterial(Inventory inv, Material mat) {
        if (inv == null || mat == null) return false;
        for (ItemStack it : inv.getContents()) {
            if (it != null && it.getType() == mat && it.getAmount() > 0) {
                return true;
            }
        }
        return false;
    }
}