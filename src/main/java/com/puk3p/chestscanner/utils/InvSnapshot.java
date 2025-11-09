package com.puk3p.chestscanner.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InvSnapshot {
    // key simplu: MATERIAL:data (în 1.8 "data" = durability/variantă)
    private static String key(ItemStack it) {
        short data = it.getDurability();
        return it.getType().name() + ":" + data;
    }

    private static boolean isAir(ItemStack it) {
        return it == null || it.getType() == Material.AIR || it.getAmount() <= 0;
    }

    public static Map<String, Integer> snapshot(Inventory inv) {
        Map<String, Integer> m = new HashMap<>();
        for (ItemStack it : inv.getContents()) {
            if (isAir(it)) continue;
            String k = key(it);
            m.put(k, m.getOrDefault(k, 0) + it.getAmount());
        }
        return m;
    }

    public static Diff diff(Map<String, Integer> before, Map<String, Integer> after) {
        Map<String, Integer> add = new HashMap<>();
        Map<String, Integer> rem = new HashMap<>();

        // chei unite
        Map<String, Integer> all = new HashMap<>(before);
        for (Map.Entry<String, Integer> e : after.entrySet()) {
            if (!all.containsKey(e.getKey())) all.put(e.getKey(), e.getValue());
        }

        for (String k : all.keySet()) {
            int b = before.getOrDefault(k, 0);
            int a = after.getOrDefault(k, 0);
            int d = a - b;
            if (d > 0) add.put(k, d);
            else if (d < 0) rem.put(k, -d);
        }
        return new Diff(add, rem);
    }

    public static class Diff {
        public final Map<String, Integer> added, removed;
        public Diff(Map<String, Integer> ad, Map<String, Integer> rm) { this.added = ad; this.removed = rm; }
        public boolean isEmpty() { return added.isEmpty() && removed.isEmpty(); }
        @Override public String toString() { return "added=" + added + ", removed=" + removed; }
    }
}
