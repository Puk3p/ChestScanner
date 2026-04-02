package com.puk3p.chestscanner.utils;

import java.util.ArrayDeque;
import java.util.Queue;

public class ChunkSpiral {

    /**
     * Creeaza o ordine spiralata a celor mai apropiate coordonate de chunk in jurul (cx, cz), pana
     * la maxCount pozitii. Include centrul.
     */
    public static Queue<int[]> generate(int cx, int cz, int maxCount) {
        ArrayDeque<int[]> q = new ArrayDeque<>();
        if (maxCount <= 0) return q;

        q.add(new int[] {cx, cz});
        if (maxCount == 1) return q;

        int layer = 1;
        while (q.size() < maxCount) {
            int minX = cx - layer, maxX = cx + layer;
            int minZ = cz - layer, maxZ = cz + layer;

            for (int x = minX; x <= maxX && q.size() < maxCount; x++) q.add(new int[] {x, minZ});
            for (int z = minZ + 1; z <= maxZ && q.size() < maxCount; z++)
                q.add(new int[] {maxX, z});
            for (int x = maxX - 1; x >= minX && q.size() < maxCount; x--)
                q.add(new int[] {x, maxZ});
            for (int z = maxZ - 1; z > minZ && q.size() < maxCount; z--) q.add(new int[] {minX, z});

            layer++;
        }
        return q;
    }
}
