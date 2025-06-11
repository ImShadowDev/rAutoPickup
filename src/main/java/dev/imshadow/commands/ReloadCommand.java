package dev.imshadow.commands;

import dev.imshadow.listeners.BlockBreakListener;
import dev.imshadow.rAutoPickup;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

public class ReloadCommand implements CommandExecutor {

    private final rAutoPickup plugin;
    private BlockBreakListener blockBreakListener; // Añadir esta propiedad

    public ReloadCommand(rAutoPickup plugin) {
        this.plugin = plugin;
        // Buscar el listener entre los listeners registrados
        findBlockBreakListener();
    }

    /**
     * Busca la instancia del BlockBreakListener entre los listeners registrados
     */
    private void findBlockBreakListener() {
        for (RegisteredListener listener : HandlerList.getRegisteredListeners(plugin)) {
            if (listener.getListener() instanceof BlockBreakListener) {
                blockBreakListener = (BlockBreakListener) listener.getListener();
                break;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Mostrar ayuda si no se proporciona ningún argumento
            showHelp(sender);
            return true;
        }

        // Si el argumento es 'reload', recargar la configuración
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rautopickup.admin")) {
                sender.sendMessage(safeColorize(plugin.getConfigManager().getConfig().getString("permission")));
                return true;
            }

            // Recargar archivos de configuración
            plugin.getConfigManager().reload();

            // Recargar la configuración de Fortune en BlockBreakListener
            if (blockBreakListener != null) {
                blockBreakListener.loadFortuneBlocksConfig();
            } else {
                // Si no se encontró el listener anteriormente, intentar encontrarlo ahora
                findBlockBreakListener();
                if (blockBreakListener != null) {
                    blockBreakListener.loadFortuneBlocksConfig();
                } else {
                    sender.sendMessage(safeColorize("&cCould not find BlockBreakListener to update Fortune configuration."));
                }
            }

            // Asegúrar de que se recarguen también los elementos personalizados
            plugin.reloadCustomPickupItems();
            plugin.getBlockBreakListener().loadDisabledBlocksConfig();

            sender.sendMessage(safeColorize(plugin.getConfigManager().getConfig().getString("reload")));
            return true;
        }

        // Si el argumento no es válido, mostrar ayuda
        showHelp(sender);
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(safeColorize("&4===== &c&lrAutoPickup Help &4====="));
        sender.sendMessage(safeColorize("&r"));
        sender.sendMessage(safeColorize("&c/rautopickup reload &7- Reload the configuration"));
        sender.sendMessage(safeColorize("&c/autopickup &7- Enable/Disable AutoPickup"));
        sender.sendMessage(safeColorize("&c/autosmelt &7- Enable/Disable AutoSmelt"));
        sender.sendMessage(safeColorize("&r"));
        sender.sendMessage(safeColorize("&4=========================="));
    }

    // Método interno de respaldo que no depende de CC.color
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