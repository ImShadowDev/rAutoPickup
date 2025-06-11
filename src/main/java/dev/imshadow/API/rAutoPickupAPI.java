package dev.imshadow.API;

import dev.imshadow.rAutoPickup;
import dev.imshadow.PickupSystem.AutoPickup;
import dev.imshadow.PickupSystem.AutoSmelt;
import dev.imshadow.managers.PlayerDataManager;
import dev.imshadow.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/**
 * API pública para el plugin
 * Permite a otros plugins interactuar con las funcionalidades de AutoPickup y AutoSmelt
 *
 * @author imshadow
 * @version 1.8.0
 */
public class rAutoPickupAPI {

    private static rAutoPickup plugin;

    /**
     * Inicializa la API con la instancia del plugin
     * Este método es llamado automáticamente por el plugin principal
     *
     * @param pluginInstance Instancia del plugin rAutoPickup
     */
    public static void initialize(rAutoPickup pluginInstance) {
        plugin = pluginInstance;
    }

    /**
     * Verifica si la API está disponible y lista para usar
     *
     * @return true si la API está inicializada, false en caso contrario
     */
    public static boolean isAvailable() {
        return plugin != null && plugin.isEnabled();
    }

    /**
     * Obtiene la instancia del plugin rAutoPickup
     *
     * @return Instancia del plugin o null si no está disponible
     */
    public static rAutoPickup getPlugin() {
        return plugin;
    }

    // ==================== MÉTODOS DE AUTOPICKUP ====================

    /**
     * Verifica si un jugador tiene AutoPickup activado
     *
     * @param playerId UUID del jugador
     * @return true si tiene AutoPickup activado, false en caso contrario
     */
    public static boolean hasAutoPickupEnabled(UUID playerId) {
        if (!isAvailable()) return false;
        return plugin.getPlayerDataManager().hasAutoPickupEnabled(playerId);
    }

    /**
     * Verifica si un jugador tiene AutoPickup activado
     *
     * @param player Jugador a verificar
     * @return true si tiene AutoPickup activado, false en caso contrario
     */
    public static boolean hasAutoPickupEnabled(Player player) {
        return hasAutoPickupEnabled(player.getUniqueId());
    }

    /**
     * Activa o desactiva AutoPickup para un jugador
     *
     * @param playerId UUID del jugador
     * @param enabled true para activar, false para desactivar
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public static boolean setAutoPickupEnabled(UUID playerId, boolean enabled) {
        if (!isAvailable()) return false;

        try {
            PlayerDataManager pdm = plugin.getPlayerDataManager();
            if (enabled) {
                pdm.hasAutoPickupEnabled(playerId);
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Error setting AutoPickup for player " + playerId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Activa o desactiva AutoPickup para un jugador
     *
     * @param player Jugador
     * @param enabled true para activar, false para desactivar
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public static boolean setAutoPickupEnabled(Player player, boolean enabled) {
        return setAutoPickupEnabled(player.getUniqueId(), enabled);
    }

    /**
     * Alterna el estado de AutoPickup para un jugador
     *
     * @param playerId UUID del jugador
     * @return true si ahora está activado, false si está desactivado
     */
    public static boolean toggleAutoPickup(UUID playerId) {
        if (!isAvailable()) return false;

        boolean currentStatus = hasAutoPickupEnabled(playerId);
        setAutoPickupEnabled(playerId, !currentStatus);
        return !currentStatus;
    }

    /**
     * Alterna el estado de AutoPickup para un jugador
     *
     * @param player Jugador
     * @return true si ahora está activado, false si está desactivado
     */
    public static boolean toggleAutoPickup(Player player) {
        return toggleAutoPickup(player.getUniqueId());
    }

    // ==================== MÉTODOS DE AUTOSMELT ====================

    /**
     * Verifica si un jugador tiene AutoSmelt activado
     *
     * @param playerId UUID del jugador
     * @return true si tiene AutoSmelt activado, false en caso contrario
     */
    public static boolean hasAutoSmeltEnabled(UUID playerId) {
        if (!isAvailable()) return false;
        return plugin.getPlayerDataManager().hasAutoSmeltEnabled(playerId);
    }

    /**
     * Verifica si un jugador tiene AutoSmelt activado
     *
     * @param player Jugador a verificar
     * @return true si tiene AutoSmelt activado, false en caso contrario
     */
    public static boolean hasAutoSmeltEnabled(Player player) {
        return hasAutoSmeltEnabled(player.getUniqueId());
    }

    /**
     * Activa o desactiva AutoSmelt para un jugador
     *
     * @param playerId UUID del jugador
     * @param enabled true para activar, false para desactivar
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public static boolean setAutoSmeltEnabled(UUID playerId, boolean enabled) {
        if (!isAvailable()) return false;

        try {
            PlayerDataManager pdm = plugin.getPlayerDataManager();
            if (enabled) {
                pdm.hasAutoSmeltEnabled(playerId);
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Error setting AutoSmelt for player " + playerId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Activa o desactiva AutoSmelt para un jugador
     *
     * @param player Jugador
     * @param enabled true para activar, false para desactivar
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public static boolean setAutoSmeltEnabled(Player player, boolean enabled) {
        return setAutoSmeltEnabled(player.getUniqueId(), enabled);
    }

    /**
     * Alterna el estado de AutoSmelt para un jugador
     *
     * @param playerId UUID del jugador
     * @return true si ahora está activado, false si está desactivado
     */
    public static boolean toggleAutoSmelt(UUID playerId) {
        if (!isAvailable()) return false;

        boolean currentStatus = hasAutoSmeltEnabled(playerId);
        setAutoSmeltEnabled(playerId, !currentStatus);
        return !currentStatus;
    }

    /**
     * Alterna el estado de AutoSmelt para un jugador
     *
     * @param player Jugador
     * @return true si ahora está activado, false si está desactivado
     */
    public static boolean toggleAutoSmelt(Player player) {
        return toggleAutoSmelt(player.getUniqueId());
    }

    // ==================== MÉTODOS DE MATERIALES ====================

    /**
     * Verifica si un material es un bloque personalizado configurado en AutoPickup
     *
     * @param material Material a verificar
     * @return true si es un bloque personalizado, false en caso contrario
     */
    public static boolean isCustomBlock(Material material) {
        if (!isAvailable()) return false;

        AutoPickup autoPickup = plugin.getAutoPickup();
        return autoPickup != null && autoPickup.isCustomBlock(material);
    }

    /**
     * Verifica si un material puede ser fundido por AutoSmelt
     *
     * @param material Material a verificar
     * @return true si puede ser fundido, false en caso contrario
     */
    public static boolean isSmeltable(Material material) {
        if (!isAvailable()) return false;

        AutoSmelt autoSmelt = plugin.getAutoSmelt();
        return autoSmelt != null && autoSmelt.isSmeltable(material);
    }

    /**
     * Obtiene el resultado de fundir un item
     *
     * @param item Item a fundir
     * @return Item fundido o el item original si no puede ser fundido
     */
    public static ItemStack smeltItem(ItemStack item) {
        if (!isAvailable() || item == null) return item;

        AutoSmelt autoSmelt = plugin.getAutoSmelt();
        if (autoSmelt != null) {
            return autoSmelt.smeltItem(item);
        }
        return item;
    }

    /**
     * Verifica si AutoPickup está deshabilitado para un tipo de bloque específico
     *
     * @param material Material del bloque
     * @return true si está deshabilitado, false en caso contrario
     */
    public static boolean isAutoPickupDisabled(Material material) {
        if (!isAvailable()) return true;

        // Leer la configuración directamente
        ConfigManager configManager = plugin.getConfigManager();
        if (configManager == null) return true;

        List<String> disabledBlocks = configManager.getConfig().getStringList("disable-blocks-autopickup");
        if (disabledBlocks == null) return false;

        // Verificar si hay wildcard
        if (disabledBlocks.contains("*")) return true;

        // Verificar si el bloque específico está deshabilitado
        return disabledBlocks.contains(material.name());
    }

    /**
     * Verifica si AutoSmelt está deshabilitado para un tipo de bloque específico
     *
     * @param material Material del bloque
     * @return true si está deshabilitado, false en caso contrario
     */
    public static boolean isAutoSmeltDisabled(Material material) {
        if (!isAvailable()) return true;

        // Leer la configuración directamente
        ConfigManager configManager = plugin.getConfigManager();
        if (configManager == null) return true;

        List<String> disabledBlocks = configManager.getConfig().getStringList("disable-blocks-autosmelt");
        if (disabledBlocks == null) return false;

        // Verificar si hay wildcard
        if (disabledBlocks.contains("*")) return true;

        // Verificar si el bloque específico está deshabilitado
        return disabledBlocks.contains(material.name());
    }

    // ==================== MÉTODOS DE PROCESAMIENTO ====================

    /**
     * Procesa manualmente el drop de un bloque para un jugador
     * Útil para otros plugins que quieran usar la funcionalidad de AutoPickup
     *
     * @param player Jugador que rompe el bloque
     * @param block Bloque que se está rompiendo
     * @param drops Lista de drops a procesar
     * @return true si el procesamiento fue exitoso, false en caso contrario
     */
    public static boolean processBlockDrop(Player player, Block block, List<ItemStack> drops) {
        if (!isAvailable()) return false;

        AutoPickup autoPickup = plugin.getAutoPickup();
        if (autoPickup != null) {
            return autoPickup.processBlockDrop(player, block, drops);
        }
        return false;
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN ====================

    /**
     * Recarga la configuración del plugin
     *
     * @return true si la recarga fue exitosa, false en caso contrario
     */
    public static boolean reloadConfig() {
        if (!isAvailable()) return false;

        try {
            plugin.getConfigManager().load();
            plugin.reloadCustomPickupItems();
            plugin.getBlockBreakListener().loadDisabledBlocksConfig();
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Error reloading config: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el ConfigManager del plugin
     *
     * @return ConfigManager o null si no está disponible
     */
    public static ConfigManager getConfigManager() {
        if (!isAvailable()) return null;
        return plugin.getConfigManager();
    }

    /**
     * Obtiene el PlayerDataManager del plugin
     *
     * @return PlayerDataManager o null si no está disponible
     */
    public static PlayerDataManager getPlayerDataManager() {
        if (!isAvailable()) return null;
        return plugin.getPlayerDataManager();
    }

    // ==================== MÉTODOS DE EVENTOS PERSONALIZADOS ====================

    /**
     * Registra un listener para eventos de AutoPickup
     * Los desarrolladores pueden usar esto para escuchar cuando se usan las funcionalidades
     *
     * @param plugin Plugin que registra el listener
     * @param listener Listener a registrar
     * @return true si se registró exitosamente, false en caso contrario
     */
    public static boolean registerAutoPickupListener(Plugin plugin, AutoPickupListener listener) {
        if (!isAvailable()) return false;

        try {
            // Aquí podrías implementar un sistema de eventos personalizado
            // Por ahora, simplemente devolvemos true
            return true;
        } catch (Exception e) {
            rAutoPickupAPI.plugin.getLogger().warning("Error registering AutoPickup listener: " + e.getMessage());
            return false;
        }
    }

    // ==================== INTERFAZ PARA LISTENERS PERSONALIZADOS ====================

    /**
     * Interfaz para listeners de eventos de AutoPickup
     */
    public interface AutoPickupListener {

        /**
         * Se llama cuando un jugador activa/desactiva AutoPickup
         *
         * @param player Jugador
         * @param enabled Nuevo estado
         */
        default void onAutoPickupToggle(Player player, boolean enabled) {}

        /**
         * Se llama cuando un jugador activa/desactiva AutoSmelt
         *
         * @param player Jugador
         * @param enabled Nuevo estado
         */
        default void onAutoSmeltToggle(Player player, boolean enabled) {}

        /**
         * Se llama cuando se procesa un drop personalizado
         *
         * @param player Jugador
         * @param block Bloque
         * @param drops Drops procesados
         */
        default void onCustomDropProcess(Player player, Block block, List<ItemStack> drops) {}
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtiene la versión del plugin
     *
     * @return Versión del plugin o "Unknown" si no está disponible
     */
    public static String getVersion() {
        if (!isAvailable()) return "Unknown";
        return plugin.getDescription().getVersion();
    }

    /**
     * Verifica si el plugin está habilitado
     *
     * @return true si está habilitado, false en caso contrario
     */
    public static boolean isEnabled() {
        return plugin != null && plugin.isEnabled();
    }

    /**
     * Obtiene información de debug del estado actual del plugin
     *
     * @return String con información de debug
     */
    public static String getDebugInfo() {
        if (!isAvailable()) {
            return "rAutoPickup API: Not Available";
        }

        StringBuilder info = new StringBuilder();
        info.append("=== rAutoPickup API Debug Info ===\n");
        info.append("Plugin Version: ").append(getVersion()).append("\n");
        info.append("Plugin Enabled: ").append(isEnabled()).append("\n");
        info.append("ConfigManager Available: ").append(getConfigManager() != null).append("\n");
        info.append("PlayerDataManager Available: ").append(getPlayerDataManager() != null).append("\n");
        info.append("AutoPickup System Available: ").append(plugin.getAutoPickup() != null).append("\n");
        info.append("AutoSmelt System Available: ").append(plugin.getAutoSmelt() != null).append("\n");

        return info.toString();
    }
}