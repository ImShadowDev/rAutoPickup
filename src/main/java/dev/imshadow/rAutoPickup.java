package dev.imshadow;

import dev.imshadow.API.rAutoPickupAPI;
import dev.imshadow.PickupSystem.AutoPickup;
import dev.imshadow.PickupSystem.AutoSmelt;
import dev.imshadow.commands.AutoPickupCommand;
import dev.imshadow.commands.AutoSmeltCommand;
import dev.imshadow.commands.ReloadCommand;
import dev.imshadow.config.ConfigManager;
import dev.imshadow.listeners.BlockBreakListener;
import dev.imshadow.listeners.PlayerJoinListener;
import dev.imshadow.managers.PlayerDataManager;
import dev.imshadow.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class rAutoPickup extends JavaPlugin {

    private static rAutoPickup instance;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private AutoPickup autoPickup;
    private AutoSmelt autoSmelt;
    private BlockBreakListener blockBreakListener;

    @Override
    public void onEnable() {
        // Guardamos la instancia
        instance = this;

        // Inicializar la API
        rAutoPickupAPI.initialize(this);

        try {
            // Inicializamos ConfigManager y cargamos la configuración
            configManager = new ConfigManager(this);
            configManager.load();

            // Inicializamos PlayerDataManager
            playerDataManager = new PlayerDataManager(this);

            // Inicializamos los sistemas
            autoPickup = new AutoPickup(this);
            autoSmelt = new AutoSmelt(this);

            // Registramos listeners
            blockBreakListener = new BlockBreakListener(this);
            getServer().getPluginManager().registerEvents(blockBreakListener, this);
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

            // Registramos comandos
            getCommand("autopickup").setExecutor(new AutoPickupCommand(this));
            getCommand("autosmelt").setExecutor(new AutoSmeltCommand(this));
            getCommand("rautopickup").setExecutor(new ReloadCommand(this));

            // Cargar los items personalizados
            autoPickup.loadCustomPickupItems();
            blockBreakListener.loadDisabledBlocksConfig();

            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&m------------------------------------------"));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&crAutoPickup&7] &fStarted plugin &a" + getDescription().getVersion()));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&crAutoPickup&7] &eThanks for using my plugin :3"));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&m------------------------------------------"));
        } catch (Exception e) {
            getLogger().severe("Error initializing rAutoPickup: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public BlockBreakListener getBlockBreakListener() {
        return blockBreakListener;
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&m------------------------------------------"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&crAutoPickup&7] &fStopped plugin &a" + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&crAutoPickup&7] &eThanks for using my plugin :3"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&m------------------------------------------"));
    }

    public static rAutoPickup getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public AutoPickup getAutoPickup() {
        return autoPickup;
    }

    public AutoSmelt getAutoSmelt() {
        return autoSmelt;
    }

    public void reloadCustomPickupItems() {
        // Recargamos los items personalizados
        if (autoPickup != null) {
            autoPickup.loadCustomPickupItems();
        }
    }
}