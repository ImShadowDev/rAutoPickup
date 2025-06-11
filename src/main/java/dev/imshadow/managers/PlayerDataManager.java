package dev.imshadow.managers;

import dev.imshadow.rAutoPickup;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final rAutoPickup plugin;
    private final Map<UUID, Boolean> autoPickupEnabled = new HashMap<>();
    private final Map<UUID, Boolean> autoSmeltEnabled = new HashMap<>();

    public PlayerDataManager(rAutoPickup plugin) {
        this.plugin = plugin;
        loadPlayerData();
    }

    private void loadPlayerData() {
        FileConfiguration playerData = plugin.getConfigManager().getPlayerData();
        if (playerData.contains("players")) {
            for (String uuidString : playerData.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                boolean autopickup = playerData.getBoolean("players." + uuidString + ".autopickup", true);
                boolean autosmelt = playerData.getBoolean("players." + uuidString + ".autosmelt", true);

                autoPickupEnabled.put(uuid, autopickup);
                autoSmeltEnabled.put(uuid, autosmelt);
            }
        }
    }

    public boolean hasPlayerData(UUID uuid) {
        return autoPickupEnabled.containsKey(uuid);
    }

    public boolean hasAutoPickupEnabled(UUID uuid) {
        return autoPickupEnabled.getOrDefault(uuid, true);
    }

    public boolean hasAutoSmeltEnabled(UUID uuid) {
        return autoSmeltEnabled.getOrDefault(uuid, true);
    }

    public void setAutoPickupEnabled(UUID uuid, boolean enabled) {
        autoPickupEnabled.put(uuid, enabled);
        savePlayerData(uuid);
    }

    public void setAutoSmeltEnabled(UUID uuid, boolean enabled) {
        autoSmeltEnabled.put(uuid, enabled);
        savePlayerData(uuid);
    }

    private void savePlayerData(UUID uuid) {
        FileConfiguration playerData = plugin.getConfigManager().getPlayerData();
        playerData.set("players." + uuid.toString() + ".autopickup", autoPickupEnabled.get(uuid));
        playerData.set("players." + uuid.toString() + ".autosmelt", autoSmeltEnabled.get(uuid));
        plugin.getConfigManager().savePlayerData();
    }

    public void saveAll() {
        FileConfiguration playerData = plugin.getConfigManager().getPlayerData();
        for (Map.Entry<UUID, Boolean> entry : autoPickupEnabled.entrySet()) {
            playerData.set("players." + entry.getKey().toString() + ".autopickup", entry.getValue());
        }
        for (Map.Entry<UUID, Boolean> entry : autoSmeltEnabled.entrySet()) {
            playerData.set("players." + entry.getKey().toString() + ".autosmelt", entry.getValue());
        }
        plugin.getConfigManager().savePlayerData();
    }
}