package io.github.glocation87.admintools.debug;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.World;

public final class Diagnostics {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final long MB = 1024 * 1024;

    private final File dir;

    public Diagnostics(File dir) {
        this.dir = dir;
    }

    public static long usedMb() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / MB;
    }

    public static long maxMb() {
        return Runtime.getRuntime().maxMemory() / MB;
    }

    public long gc() {
        long before = usedMb();
        System.gc();
        return before - usedMb();
    }

    public File threadDump() throws IOException {
        File file = target("threads");
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            for (ThreadInfo info : ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)) {
                out.print(info.toString());
            }
        }
        return file;
    }

    public File heapReport() throws IOException {
        File file = target("heap");
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            out.println("Uptime: " + ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + "s");
            out.println("JVM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));
            out.println("Threads: " + ManagementFactory.getThreadMXBean().getThreadCount());
            out.println("Heap used: " + usedMb() + " MB of " + maxMb() + " MB");
            out.println();
            out.println("Memory pools");
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                MemoryUsage usage = pool.getUsage();
                out.println("  " + pool.getName() + ": " + usage.getUsed() / MB + " MB used, " + usage.getCommitted() / MB + " MB committed");
            }
            out.println();
            out.println("Garbage collectors");
            for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                out.println("  " + gc.getName() + ": " + gc.getCollectionCount() + " runs, " + gc.getCollectionTime() + " ms");
            }
            out.println();
            out.println("Worlds");
            for (World world : Bukkit.getWorlds()) {
                out.println("  " + world.getName() + ": " + world.getPlayerCount() + " players, " + world.getEntityCount() + " entities, "
                    + world.getChunkCount() + " chunks, " + world.getTileEntityCount() + " tile entities");
            }
        }
        return file;
    }

    private File target(String kind) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("cannot create " + dir);
        }
        return new File(dir, kind + "-" + STAMP.format(LocalDateTime.now()) + ".txt");
    }
}
