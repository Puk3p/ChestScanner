package com.puk3p.chestscanner.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.*;

public class LogPretty {

    public static void sendPrettyLog(Player p, List<String> rawLines) {
        header(p, "Ultimele " + rawLines.size() + " acțiuni");
        for (String line : rawLines) {
            Entry e = parse(line);
            if (e == null) {
                // fallback: arată brut dacă nu poate fi parsată linia
                p.sendMessage(ChatColor.GRAY + " - " + line);
                continue;
            }

            // cap de entry: [ora] Nume
            TextComponent bullet = tc("• ", ChatColor.DARK_AQUA, false);
            TextComponent ts = tc("[" + e.time + "] ", ChatColor.GRAY, false);
            TextComponent actor = tc(e.actor, ChatColor.GOLD, true);
            p.spigot().sendMessage(bullet, ts, actor);

            if (!e.added.isEmpty()) {
                p.spigot().sendMessage(tc("   + Adăugate:", ChatColor.GREEN, true));
                sendItemsList(p, e.added, ChatColor.GREEN);
            }
            if (!e.removed.isEmpty()) {
                p.spigot().sendMessage(tc("   - Scoase:", ChatColor.RED, true));
                sendItemsList(p, e.removed, ChatColor.RED);
            }
            if (e.added.isEmpty() && e.removed.isEmpty()) {
                p.spigot().sendMessage(tc("   (fără schimbări)", ChatColor.DARK_GRAY, false));
            }
        }
        footer(p);
    }

    private static void sendItemsList(Player p, Map<String,Integer> items, ChatColor accent) {
        // păstrează ordinea din fișier (LinkedHashMap la parse)
        for (Map.Entry<String,Integer> it : items.entrySet()) {
            String key = it.getKey();          // ex: TRIPWIRE_HOOK:0
            int amount = it.getValue();        // ex: 8
            String niceName = prettify(key);   // ex: Tripwire Hook (data 0)

            TextComponent dot = tc("     • ", ChatColor.DARK_GRAY, false);
            TextComponent amt = tc(amount + "x ", accent, true);
            TextComponent name = tc(niceName, ChatColor.WHITE, false);

            // hover: arată cheia exactă din log (MATERIAL:data)
            name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{tc(key, ChatColor.GRAY, false)}));

            p.spigot().sendMessage(dot, amt, name);
        }
    }

    /* ---------- parsare linii existente din log ---------- */

    private static Entry parse(String line) {
        try {
            int lb = line.indexOf('[');
            int rb = line.indexOf(']');
            if (lb != 0 || rb < 0) return null;
            String time = line.substring(lb + 1, rb).trim();

            int afterTs = rb + 1;
            int arrow = line.indexOf("->", afterTs);
            if (arrow < 0) return null;
            String actor = line.substring(afterTs, arrow).trim();
            if (actor.startsWith(":")) actor = actor.substring(1).trim(); // în caz de spații

            int plusOpen = line.indexOf('{', arrow);
            int plusClose = line.indexOf('}', plusOpen);
            int bar = line.indexOf('|', plusClose);
            int minusOpen = line.indexOf('{', bar);
            int minusClose = line.lastIndexOf('}');

            String plusItems = (plusOpen > 0 && plusClose > plusOpen) ? line.substring(plusOpen + 1, plusClose) : "";
            String minusItems = (minusOpen > 0 && minusClose > minusOpen) ? line.substring(minusOpen + 1, minusClose) : "";

            Entry e = new Entry();
            e.time = time;
            e.actor = actor.replace("  ", " ");
            e.added = parseItems(plusItems);
            e.removed = parseItems(minusItems);
            return e;
        } catch (Exception ex) {
            return null;
        }
    }

    private static Map<String,Integer> parseItems(String s) {
        Map<String,Integer> map = new LinkedHashMap<>();
        if (s == null) return map;
        s = s.trim();
        if (s.isEmpty() || s.equals("()")) return map;

        // format: NAME:DATA=AMOUNT, NAME2:DATA=AMOUNT2
        String[] parts = s.split(",");
        for (String part : parts) {
            String t = part.trim();
            if (t.isEmpty()) continue;
            int eq = t.lastIndexOf('=');
            if (eq < 0) continue;
            String key = t.substring(0, eq).trim();
            String val = t.substring(eq + 1).trim();
            try {
                int amount = Integer.parseInt(val);
                if (amount > 0) map.put(key, amount);
            } catch (NumberFormatException ignored) { }
        }
        return map;
    }

    /* ---------- mici helper-e de styling ---------- */

    private static TextComponent tc(String text, ChatColor color, boolean bold) {
        TextComponent c = new TextComponent(text);
        c.setColor(color);
        c.setBold(bold);
        return c;
    }

    private static void header(Player p, String title) {
        TextComponent left = tc("── ", ChatColor.DARK_GRAY, false);
        TextComponent mid = tc(title, ChatColor.GOLD, true);
        TextComponent right = tc(" ──", ChatColor.DARK_GRAY, false);
        p.spigot().sendMessage(left, mid, right);
    }

    private static void footer(Player p) {
        p.spigot().sendMessage(tc("────────────────────────", ChatColor.DARK_GRAY, false));
    }

    private static String prettify(String key) {
        // key format: MATERIAL:data
        String mat = key;
        String data = "0";
        int colon = key.indexOf(':');
        if (colon >= 0) {
            mat = key.substring(0, colon);
            data = key.substring(colon + 1);
        }
        String nice = capitalizeWords(mat.toLowerCase().replace('_', ' '));
        if (!"0".equals(data)) nice += " (data " + data + ")";
        return nice;
    }

    private static String capitalizeWords(String s) {
        StringBuilder out = new StringBuilder(s.length());
        boolean up = true;
        for (char ch : s.toCharArray()) {
            if (up && ch >= 'a' && ch <= 'z') { out.append((char)(ch - 32)); up = false; }
            else { out.append(ch); }
            if (ch == ' ') up = true;
        }
        return out.toString();
    }

    private static class Entry {
        String time, actor;
        Map<String,Integer> added, removed;
    }
}
