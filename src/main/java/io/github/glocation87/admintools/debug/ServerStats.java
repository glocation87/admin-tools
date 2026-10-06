package io.github.glocation87.admintools.debug;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.bukkit.Bukkit;

// Rolling tick timing samples for the dashboard history bars
public final class ServerStats {
    private static final int SAMPLES = 30;

    private final Deque<Double> mspt = new ArrayDeque<>();
    private final Deque<Double> tps = new ArrayDeque<>();

    public void sample() {
        try {
            push(mspt, Bukkit.getAverageTickTime());
            push(tps, Bukkit.getTPS()[0]);
        } catch (UnsupportedOperationException e) {
            // test servers do not track tick timing
        }
    }

    private static void push(Deque<Double> history, double value) {
        history.addLast(value);
        while (history.size() > SAMPLES) {
            history.removeFirst();
        }
    }

    public List<Double> mspt() {
        return new ArrayList<>(mspt);
    }

    public List<Double> tps() {
        return new ArrayList<>(tps);
    }

    public double peakMspt() {
        double peak = 0;
        for (double value : mspt) {
            peak = Math.max(peak, value);
        }
        return peak;
    }
}
