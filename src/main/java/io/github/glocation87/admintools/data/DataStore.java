package io.github.glocation87.admintools.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    public DataStore(File file, Logger log) {
        this.file = file;
        this.log = log;
    }

    public void load() {
        mutes.clear();
        frozen.clear();
        notes.clear();
        snapshots.clear();
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
                    list.add(new StaffNote(String.valueOf(raw.get("author")), ((Number) raw.get("time")).longValue(), String.valueOf(raw.get("text"))));
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
                Map<String, Object> raw = new HashMap<>();
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
}
