package io.github.glocation87.admintools.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class DataStore {
    private final File file;
    private final Logger log;
    private final Map<UUID, Mute> mutes = new HashMap<>();
    private final Set<UUID> frozen = new HashSet<>();
    private final Map<UUID, List<StaffNote>> notes = new HashMap<>();
    private final Map<UUID, StaffSnapshot> snapshots = new HashMap<>();
    private final Map<UUID, List<Punishment>> history = new HashMap<>();
    private final Map<UUID, Report> reports = new LinkedHashMap<>();
    private final Map<UUID, Set<String>> addresses = new HashMap<>();

    public DataStore(File file, Logger log) {
        this.file = file;
        this.log = log;
    }

    public void load() {
        mutes.clear();
        frozen.clear();
        notes.clear();
        snapshots.clear();
        history.clear();
        reports.clear();
        addresses.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection muteSection = yaml.getConfigurationSection("mutes");
        if (muteSection != null) {
            for (String key : muteSection.getKeys(false)) {
                ConfigurationSection entry = muteSection.getConfigurationSection(key);
                mutes.put(UUID.fromString(key), new Mute(entry.getLong("expires", -1), entry.getString("reason", ""), entry.getString("by", "")));
            }
        }
        for (String key : yaml.getStringList("frozen")) {
            frozen.add(UUID.fromString(key));
        }
        ConfigurationSection noteSection = yaml.getConfigurationSection("notes");
        if (noteSection != null) {
            for (String key : noteSection.getKeys(false)) {
                List<StaffNote> list = new ArrayList<>();
                for (Map<?, ?> raw : noteSection.getMapList(key)) {
                    list.add(new StaffNote(String.valueOf(raw.get("author")), number(raw.get("time")), String.valueOf(raw.get("text"))));
                }
                notes.put(UUID.fromString(key), list);
            }
        }
        ConfigurationSection snapshotSection = yaml.getConfigurationSection("snapshots");
        if (snapshotSection != null) {
            for (String key : snapshotSection.getKeys(false)) {
                try {
                    snapshots.put(UUID.fromString(key), StaffSnapshot.read(snapshotSection.getConfigurationSection(key)));
                } catch (RuntimeException e) {
                    log.warning("Could not read staff snapshot for " + key + ": " + e.getMessage());
                }
            }
        }
        ConfigurationSection historySection = yaml.getConfigurationSection("history");
        if (historySection != null) {
            for (String key : historySection.getKeys(false)) {
                List<Punishment> list = new ArrayList<>();
                for (Map<?, ?> raw : historySection.getMapList(key)) {
                    list.add(new Punishment(
                        Punishment.Type.valueOf(String.valueOf(raw.get("type")).toUpperCase(Locale.ROOT)),
                        Punishment.Category.valueOf(String.valueOf(raw.get("category")).toUpperCase(Locale.ROOT)),
                        (int) number(raw.get("severity")), String.valueOf(raw.get("by")), number(raw.get("time")),
                        number(raw.get("expires")), String.valueOf(raw.get("reason"))));
                }
                history.put(UUID.fromString(key), list);
            }
        }
        for (Map<?, ?> raw : yaml.getMapList("reports")) {
            Report report = new Report(UUID.fromString(String.valueOf(raw.get("id"))), UUID.fromString(String.valueOf(raw.get("reporter"))),
                String.valueOf(raw.get("reporter-name")), UUID.fromString(String.valueOf(raw.get("target"))),
                String.valueOf(raw.get("target-name")), String.valueOf(raw.get("reason")), number(raw.get("time")));
            reports.put(report.id(), report);
        }
        ConfigurationSection addressSection = yaml.getConfigurationSection("addresses");
        if (addressSection != null) {
            for (String key : addressSection.getKeys(false)) {
                addresses.put(UUID.fromString(key), new LinkedHashSet<>(addressSection.getStringList(key)));
            }
        }
    }

    private static long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Mute> entry : mutes.entrySet()) {
            ConfigurationSection section = yaml.createSection("mutes." + entry.getKey());
            section.set("expires", entry.getValue().expiresAt());
            section.set("reason", entry.getValue().reason());
            section.set("by", entry.getValue().by());
        }
        List<String> frozenIds = new ArrayList<>();
        for (UUID id : frozen) {
            frozenIds.add(id.toString());
        }
        yaml.set("frozen", frozenIds);
        for (Map.Entry<UUID, List<StaffNote>> entry : notes.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (StaffNote note : entry.getValue()) {
                Map<String, Object> raw = new LinkedHashMap<>();
                raw.put("author", note.author());
                raw.put("time", note.time());
                raw.put("text", note.text());
                list.add(raw);
            }
            yaml.set("notes." + entry.getKey(), list);
        }
        for (Map.Entry<UUID, StaffSnapshot> entry : snapshots.entrySet()) {
            entry.getValue().write(yaml.createSection("snapshots." + entry.getKey()));
        }
        for (Map.Entry<UUID, List<Punishment>> entry : history.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Punishment punishment : entry.getValue()) {
                Map<String, Object> raw = new LinkedHashMap<>();
                raw.put("type", punishment.type().name());
                raw.put("category", punishment.category().name());
                raw.put("severity", punishment.severity());
                raw.put("by", punishment.by());
                raw.put("time", punishment.time());
                raw.put("expires", punishment.expiresAt());
                raw.put("reason", punishment.reason());
                list.add(raw);
            }
            yaml.set("history." + entry.getKey(), list);
        }
        List<Map<String, Object>> reportList = new ArrayList<>();
        for (Report report : reports.values()) {
            Map<String, Object> raw = new LinkedHashMap<>();
            raw.put("id", report.id().toString());
            raw.put("reporter", report.reporter().toString());
            raw.put("reporter-name", report.reporterName());
            raw.put("target", report.target().toString());
            raw.put("target-name", report.targetName());
            raw.put("reason", report.reason());
            raw.put("time", report.time());
            reportList.add(raw);
        }
        yaml.set("reports", reportList);
        for (Map.Entry<UUID, Set<String>> entry : addresses.entrySet()) {
            yaml.set("addresses." + entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        try {
            file.getParentFile().mkdirs();
            yaml.save(file);
        } catch (IOException e) {
            log.severe("Could not save " + file.getName() + ": " + e.getMessage());
        }
    }

    public Optional<Mute> mute(UUID id) {
        Mute mute = mutes.get(id);
        if (mute != null && mute.expired()) {
            mutes.remove(id);
            save();
            return Optional.empty();
        }
        return Optional.ofNullable(mute);
    }

    public void mute(UUID id, Mute mute) {
        mutes.put(id, mute);
        save();
    }

    public boolean unmute(UUID id) {
        boolean removed = mutes.remove(id) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean frozen(UUID id) {
        return frozen.contains(id);
    }

    public void frozen(UUID id, boolean value) {
        if (value ? frozen.add(id) : frozen.remove(id)) {
            save();
        }
    }

    public List<StaffNote> notes(UUID id) {
        return Collections.unmodifiableList(notes.getOrDefault(id, List.of()));
    }

    public void addNote(UUID id, StaffNote note) {
        notes.computeIfAbsent(id, key -> new ArrayList<>()).add(note);
        save();
    }

    public void removeNote(UUID id, StaffNote note) {
        List<StaffNote> list = notes.get(id);
        if (list != null && list.remove(note)) {
            if (list.isEmpty()) {
                notes.remove(id);
            }
            save();
        }
    }

    public StaffSnapshot snapshot(UUID id) {
        return snapshots.get(id);
    }

    public void snapshot(UUID id, StaffSnapshot snapshot) {
        snapshots.put(id, snapshot);
        save();
    }

    public void clearSnapshot(UUID id) {
        if (snapshots.remove(id) != null) {
            save();
        }
    }

    public List<Punishment> history(UUID id) {
        return Collections.unmodifiableList(history.getOrDefault(id, List.of()));
    }

    public void addPunishment(UUID id, Punishment punishment) {
        history.computeIfAbsent(id, key -> new ArrayList<>()).add(punishment);
        save();
    }

    public void removePunishment(UUID id, Punishment punishment) {
        List<Punishment> list = history.get(id);
        if (list != null && list.remove(punishment)) {
            if (list.isEmpty()) {
                history.remove(id);
            }
            save();
        }
    }

    public List<Report> reports() {
        return List.copyOf(reports.values());
    }

    public void addReport(Report report) {
        reports.put(report.id(), report);
        save();
    }

    public void removeReport(UUID id) {
        if (reports.remove(id) != null) {
            save();
        }
    }

    public void recordAddress(UUID id, String address) {
        if (addresses.computeIfAbsent(id, key -> new LinkedHashSet<>()).add(address)) {
            save();
        }
    }

    public Set<String> addresses(UUID id) {
        return Collections.unmodifiableSet(addresses.getOrDefault(id, Set.of()));
    }

    // Everyone who ever shared an address with this player
    public Set<UUID> alts(UUID id) {
        Set<String> own = addresses(id);
        Set<UUID> alts = new LinkedHashSet<>();
        for (Map.Entry<UUID, Set<String>> entry : addresses.entrySet()) {
            if (!entry.getKey().equals(id) && !Collections.disjoint(own, entry.getValue())) {
                alts.add(entry.getKey());
            }
        }
        return alts;
    }
}
