package com.puk3p.chestscanner.storage;

import com.puk3p.chestscanner.utils.InvSnapshot;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class LogStore {
    private final File dir;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogStore(JavaPlugin plugin) {
        this.dir = new File(plugin.getDataFolder(), "logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private File file(Location l) {
        String name =
                l.getWorld().getName()
                        + "_"
                        + l.getBlockX()
                        + "_"
                        + l.getBlockY()
                        + "_"
                        + l.getBlockZ()
                        + ".log";
        return new File(dir, name);
    }

    public void append(Location l, String playerName, InvSnapshot.Diff diff) {
        try (OutputStreamWriter fw =
                        new OutputStreamWriter(
                                new FileOutputStream(file(l), true), StandardCharsets.UTF_8);
                BufferedWriter bw = new BufferedWriter(fw)) {
            String ts = df.format(new Date());
            bw.write("[" + ts + "] " + playerName + " -> " + pretty(diff));
            bw.newLine();
        } catch (IOException ignored) {
        }
    }

    private String pretty(InvSnapshot.Diff d) {
        return "+ " + d.added + " | - " + d.removed;
    }

    public List<String> tail(Location l, int lines) {
        File f = file(l);
        if (!f.exists()) return Collections.emptyList();
        Deque<String> dq = new ArrayDeque<>();
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            long pos = raf.length() - 1;
            StringBuilder sb = new StringBuilder();
            while (pos >= 0 && dq.size() < lines) {
                raf.seek(pos--);
                int c = raf.read();
                if (c == '\n') {
                    dq.addFirst(sb.reverse().toString());
                    sb.setLength(0);
                } else {
                    sb.append((char) c);
                }
            }
            if (sb.length() > 0 && dq.size() < lines) dq.addFirst(sb.reverse().toString());
        } catch (IOException ignored) {
        }
        return new ArrayList<>(dq);
    }
}
