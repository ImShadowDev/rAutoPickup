package dev.imshadow.listeners;

import dev.imshadow.rAutoPickup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final rAutoPickup plugin;

    public PlayerJoinListener(rAutoPickup plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Establecer valor de autopickup y autosmelt para nuevos jugadores
        if (!plugin.getPlayerDataManager().hasPlayerData(player.getUniqueId())) {
            boolean defaultAutoPickup = plugin.getConfigManager().getConfig().getBoolean("join-enabled-autopickup", true);
            boolean defaultAutoSmelt = plugin.getConfigManager().getConfig().getBoolean("join-enabled-autosmelt", true);

            plugin.getPlayerDataManager().setAutoPickupEnabled(player.getUniqueId(), defaultAutoPickup);
            plugin.getPlayerDataManager().setAutoSmeltEnabled(player.getUniqueId(), defaultAutoSmelt);
        }
    }
}