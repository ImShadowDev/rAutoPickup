package dev.imshadow.commands;

import dev.imshadow.rAutoPickup;
import dev.imshadow.utils.CC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoPickupCommand implements CommandExecutor {

    private final rAutoPickup plugin;

    public AutoPickupCommand(rAutoPickup plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;
        String permission = plugin.getConfigManager().getConfig().getString("permissions.AutoPickup");

        if (!player.hasPermission(permission)) {
            player.sendMessage(safeColorize(plugin.getConfigManager().getConfig().getString("permission")));
            return true;
        }

        boolean currentState = plugin.getPlayerDataManager().hasAutoPickupEnabled(player.getUniqueId());
        plugin.getPlayerDataManager().setAutoPickupEnabled(player.getUniqueId(), !currentState);

        boolean showMessage;
        if (!currentState) {
            showMessage = plugin.getConfigManager().getConfig().getBoolean("options.AutoPickup-enabled-messages");
            if (showMessage) {
                player.sendMessage(safeColorize(plugin.getConfigManager().getConfig().getString("AutoPickup.enabled")));
            }
        } else {
            showMessage = plugin.getConfigManager().getConfig().getBoolean("options.AutoPickup-disabled-messages");
            if (showMessage) {
                player.sendMessage(safeColorize(plugin.getConfigManager().getConfig().getString("AutoPickup.disabled")));
            }
        }

        return true;
    }

    private String safeColorize(String text) {
        if (text == null) return "";
        try {
            // Intenta usar el método CC.color primero
            return dev.imshadow.utils.CC.color(text);
        } catch (Throwable e) {
            // Si falla, usa directamente ChatColor
            return ChatColor.translateAlternateColorCodes('&', text);
        }
    }
}