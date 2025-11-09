package com.puk3p.chestscanner.storage;

import org.bukkit.Location;

import java.util.*;

public class LastScanRegistry {
    private final Map<UUID, List<Location>> results = new HashMap<>();

    public void set(UUID playerId, List<Location> locs) {
        results.put(playerId, new ArrayList<>(locs));
    }

    public List<Location> get(UUID playerId) {
        return results.getOrDefault(playerId, Collections.emptyList());
    }

    public Location getByIndex(UUID playerId, int index1based) {
        List<Location> list = get(playerId);
        int idx = index1based - 1;
        return (idx >= 0 && idx < list.size()) ? list.get(idx) : null;
    }

    public int size(UUID playerId) {
        return get(playerId).size();
    }
}