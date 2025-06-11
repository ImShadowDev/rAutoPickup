package dev.imshadow.config;

import dev.imshadow.rAutoPickup;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private rAutoPickup plugin;
    private File configFile;
    private FileConfiguration config;
    private File playerDataFile;
    private FileConfiguration playerData;

    public ConfigManager(rAutoPickup plugin) {
        this.plugin = plugin;
    }

    public void load() {
        // crear config.yml
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // crear playerdata.yml
        playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.getParentFile().mkdirs();
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public void reload() {
        // Reload config.yml
        config = YamlConfiguration.loadConfiguration(configFile);

        // Reload playerdata.yml
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getPlayerData() {
        return playerData;
    }

    public void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }
}