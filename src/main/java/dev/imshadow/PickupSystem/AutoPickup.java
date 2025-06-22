package dev.imshadow.PickupSystem;

import dev.imshadow.rAutoPickup;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AutoPickup {

    private final rAutoPickup plugin;
    private final Map<Integer, List<Material>> customPickupItems = new HashMap<>();
    private final Map<Integer, List<String>> customPickupActions = new HashMap<>();
    private final Map<UUID, Map<Integer, Integer>> playerBlockCounts = new HashMap<>();
    private final Map<Integer, Integer> requiredBlocks = new HashMap<>();

    public AutoPickup(rAutoPickup plugin) {
        this.plugin = plugin;
    }

    /**
     * Método seguro para colorear texto
     * Fallback en caso de que CC.color() no esté disponible
     */
    private String colorText(String text) {
        if (text == null) return "";

        try {
            // Intentar usar la clase CC si está disponible
            Class<?> ccClass = Class.forName("dev.imshadow.utils.CC");
            java.lang.reflect.Method colorMethod = ccClass.getMethod("color", String.class);
            return (String) colorMethod.invoke(null, text);
        } catch (Exception e) {
            // Fallback: usar ChatColor directamente
            return ChatColor.translateAlternateColorCodes('&', text);
        }
    }

    public boolean isCustomBlock(Material material) {
        if (material == null) return false;

        for (Map.Entry<Integer, List<Material>> entry : customPickupItems.entrySet()) {
            if (entry.getValue().contains(material)) {
                return true;
            }
        }
        return false;
    }

    public void loadCustomPickupItems() {
        customPickupItems.clear();
        customPickupActions.clear();
        requiredBlocks.clear();

        // Cargar items personalizados de la configuración
        ConfigurationSection rootSection = plugin.getConfigManager().getConfig().getConfigurationSection("");
        if (rootSection == null) {
            plugin.getLogger().warning("Configuration is empty or invalid!");
            return;
        }

        for (String key : rootSection.getKeys(false)) {
            if (key.matches("\\d+")) {
                int id = Integer.parseInt(key);
                ConfigurationSection customSection = plugin.getConfigManager().getConfig().getConfigurationSection(key + ".custom-pickup-items");

                if (customSection != null && customSection.getBoolean("enabled", false)) {
                    List<Material> materials = new ArrayList<>();
                    for (String materialName : customSection.getStringList("material")) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            materials.add(material);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid material name: " + materialName);
                        }
                    }

                    if (!materials.isEmpty()) {
                        customPickupItems.put(id, materials);
                        List<String> actions = customSection.getStringList("actions");
                        customPickupActions.put(id, actions);

                        // Cargar la cantidad de bloques requeridos si existe
                        if (customSection.contains("required-blocks")) {
                            int required = customSection.getInt("required-blocks", 0);
                            if (required > 0) {
                                requiredBlocks.put(id, required);
                            }
                        } else if (customSection.contains("requerid-blocks")) {
                            // Verificar la versión mal escrita que está en el config.yml
                            int required = customSection.getInt("requerid-blocks", 0);
                            if (required > 0) {
                                requiredBlocks.put(id, required);
                            }
                        }
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + customPickupItems.size() + " custom pickup item groups");
        plugin.getLogger().info("Required blocks configurations: " + requiredBlocks.toString());
    }

    public boolean processBlockDrop(Player player, Block block, List<ItemStack> drops) {
        // Verificar si el jugador tiene autopickup activado
        if (!plugin.getPlayerDataManager().hasAutoPickupEnabled(player.getUniqueId())) {
            return false;
        }

        // Verificación adicional para bloques de aire
        if (block.getType() == Material.AIR) {
            return false;
        }

        // Verificar si el bloque roto es un item personalizado
        for (Map.Entry<Integer, List<Material>> entry : customPickupItems.entrySet()) {
            int groupId = entry.getKey();
            if (entry.getValue().contains(block.getType())) {

                // SOLUCIÓN: Eliminar el bloque físicamente ANTES de procesar
                block.setType(Material.AIR);

                // Si hay requisito de bloques, incrementar contador
                if (requiredBlocks.containsKey(groupId)) {
                    int required = requiredBlocks.get(groupId);

                    // Incrementar antes de comprobar
                    incrementBlockCount(player, groupId);
                    int currentCount = getPlayerBlockCount(player, groupId);

                    // Si alcanzó la cantidad requerida
                    if (currentCount >= required) {
                        executeCustomActions(player, groupId);
                        resetBlockCount(player, groupId);
                    } else {
                        // Verificar si el mensaje está habilitado en config
                        if (plugin.getConfigManager().getConfig().getBoolean("options.Mined-Blocks-enabled-messages", true)) {
                            String message = plugin.getConfigManager().getConfig().getString("mined-blocks", "");
                            if (message != null && !message.isEmpty()) {
                                message = message.replace("%current%", String.valueOf(currentCount))
                                        .replace("%required%", String.valueOf(required));
                                player.sendMessage(colorText(message));
                            }
                        }
                    }
                    return true;
                } else {
                    // Ejecutar acciones inmediatamente si no hay requisito
                    executeCustomActions(player, groupId);
                }
                return true;
            }
        }

        // Manejar items normales usando la lógica consistente
        handleInventoryDrops(player, drops, block.getLocation());
        return true;
    }

    /**
     * Maneja los drops considerando si el inventario está lleno
     * Misma lógica que en BlockBreakListener para mantener consistencia
     */
    private void handleInventoryDrops(Player player, List<ItemStack> drops, org.bukkit.Location dropLocation) {
        if (drops == null || drops.isEmpty()) {
            return;
        }

        String fullInventoryOption = plugin.getConfigManager().getConfig().getString("options.Full-inventory", "drop");
        boolean inventoryFull = false;
        boolean hasEmptySlot = player.getInventory().firstEmpty() != -1;

        for (ItemStack item : drops) {
            if (!hasEmptySlot) {
                inventoryFull = true;

                if (fullInventoryOption.equalsIgnoreCase("drop")) {
                    // Dropear el item en la ubicación del bloque
                    player.getWorld().dropItemNaturally(dropLocation, item);
                } else if (fullInventoryOption.equalsIgnoreCase("no-received")) {
                    // No hacer nada, el item se pierde
                    continue;
                }
            } else {
                // Intentar añadir el item al inventario
                java.util.HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);

                // Si quedaron items sin añadir, el inventario se llenó
                if (!leftover.isEmpty()) {
                    hasEmptySlot = false;
                    inventoryFull = true;

                    // Manejar los items que no cupieron
                    for (ItemStack leftoverItem : leftover.values()) {
                        if (fullInventoryOption.equalsIgnoreCase("drop")) {
                            player.getWorld().dropItemNaturally(dropLocation, leftoverItem);
                        }
                        // Si es "no-received", simplemente no hacer nada (se pierden)
                    }
                }
            }
        }

        // Enviar mensaje si el inventario estaba lleno
        if (inventoryFull) {
            String message = "";
            if (fullInventoryOption.equalsIgnoreCase("drop")) {
                message = plugin.getConfigManager().getConfig().getString("Full-inventory.drop-items", "");
            } else if (fullInventoryOption.equalsIgnoreCase("no-received")) {
                message = plugin.getConfigManager().getConfig().getString("Full-inventory.no-received", "");
            }

            if (message != null && !message.isEmpty()) {
                player.sendMessage(colorText(message));
            }
        }
    }

    private void executeCustomActions(Player player, int key) {
        List<String> actions = customPickupActions.get(key);
        if (actions == null) return;

        for (String action : actions) {
            try {
                // Ejecutar comando de consola
                if (action.startsWith("[CONSOLE_COMMAND]")) {
                    String command = action.substring("[CONSOLE_COMMAND]".length()).trim();
                    command = command.replace("%player%", player.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                }
                // Enviar mensaje al jugador
                else if (action.startsWith("[MESSAGE]")) {
                    String message = action.substring("[MESSAGE]".length()).trim();
                    if (message != null && !message.isEmpty()) {
                        player.sendMessage(colorText(message));
                    }
                }
                // Dar item personalizado
                else if (action.startsWith("[GIVE]")) {
                    executeGiveAction(player, action);
                }
                // Reproducir sonido
                else if (action.startsWith("[SOUND]")) {
                    executeSoundAction(player, action);
                }
                // Mostrar partículas
                else if (action.startsWith("[PARTICLE]")) {
                    executeParticleAction(player, action);
                }
                // Mostrar título
                else if (action.startsWith("[TITLE]")) {
                    executeTitleAction(player, action);
                }
                // Definir bloques requeridos (aunque esto normalmente se configura, se deja para compatibilidad)
                else if (action.startsWith("[REQUIRED_BLOCKS]")) {
                    try {
                        int required = Integer.parseInt(action.substring("[REQUIRED_BLOCKS]".length()).trim());
                        requiredBlocks.put(key, required);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error parsing REQUIRED_BLOCKS: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing action '" + action + "': " + e.getMessage());
            }
        }
    }

    private void executeGiveAction(Player player, String action) {
        try {
            String[] parts = action.substring("[GIVE]".length()).trim().split(" ");
            if (parts.length >= 2) {
                Material material = Material.valueOf(parts[0].toUpperCase());
                int amount = Integer.parseInt(parts[1]);

                ItemStack item = new ItemStack(material, amount);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    // Nombre personalizado
                    for (int i = 2; i < parts.length; i++) {
                        if (parts[i].startsWith("name:")) {
                            String name = parts[i].substring(5).replace("_", " ");
                            meta.setDisplayName(colorText(name));
                        }
                        // Encantamientos
                        else if (parts[i].startsWith("enchant:")) {
                            String[] enchantParts = parts[i].substring(8).split(":");
                            if (enchantParts.length == 2) {
                                Enchantment enchantment = getEnchantmentSafely(enchantParts[0]);
                                if (enchantment != null) {
                                    int level = Integer.parseInt(enchantParts[1]);
                                    meta.addEnchant(enchantment, level, true);
                                }
                            }
                        }
                        // Lore
                        else if (parts[i].startsWith("lore:")) {
                            String lore = parts[i].substring(5).replace("_", " ");
                            List<String> loreList = meta.getLore();
                            if (loreList == null) {
                                loreList = new ArrayList<>();
                            }
                            loreList.add(colorText(lore));
                            meta.setLore(loreList);
                        }
                    }

                    item.setItemMeta(meta);
                }

                // Usar la lógica consistente de manejo de inventario
                List<ItemStack> giveItems = new ArrayList<>();
                giveItems.add(item);
                handleInventoryDrops(player, giveItems, player.getLocation());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error executing GIVE action: " + e.getMessage());
        }
    }

    private void executeSoundAction(Player player, String action) {
        try {
            String[] parts = action.substring("[SOUND]".length()).trim().split(" ");
            Sound sound = Sound.valueOf(parts[0].toUpperCase());
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Error executing SOUND action: " + e.getMessage());
        }
    }

    private void executeParticleAction(Player player, String action) {
        try {
            String particleName = action.substring("[PARTICLE]".length()).trim();

            // Para 1.8.8, usar Effect enum
            try {
                org.bukkit.Effect effect = org.bukkit.Effect.valueOf(particleName.toUpperCase());
                player.getWorld().playEffect(player.getLocation(), effect, 0);
            } catch (Exception e1) {
                try {
                    // Para versiones más recientes, usar Particle enum
                    Class<?> particleClass = Class.forName("org.bukkit.Particle");
                    Enum<?> particle = Enum.valueOf((Class<Enum>) particleClass, particleName.toUpperCase());

                    // Usar Reflection para ejecutar el método spawnParticle
                    player.getWorld().getClass()
                            .getMethod("spawnParticle", particleClass, org.bukkit.Location.class, int.class)
                            .invoke(player.getWorld(), particle, player.getLocation(), 10);
                } catch (Exception e2) {
                    plugin.getLogger().warning("Error executing PARTICLE action: " + e2.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error executing PARTICLE action: " + e.getMessage());
        }
    }

    private void executeTitleAction(Player player, String action) {
        try {
            String titleContent = action.substring("[TITLE]".length()).trim();
            String title = "";
            String subtitle = "";

            // Separar título y subtítulo
            if (titleContent.contains("|")) {
                String[] parts = titleContent.split("\\|");
                title = colorText(parts[0].trim());
                if (parts.length > 1) {
                    subtitle = colorText(parts[1].trim());
                }
            } else {
                title = colorText(titleContent);
            }

            // Mostrar título (compatible con varias versiones)
            try {
                // Método para 1.8.8 a 1.10
                player.getClass().getMethod("sendTitle", String.class, String.class)
                        .invoke(player, title, subtitle);
            } catch (NoSuchMethodError | NoSuchMethodException e1) {
                try {
                    // Método para 1.11+
                    player.getClass().getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class)
                            .invoke(player, title, subtitle, 10, 70, 20);
                } catch (Exception e2) {
                    plugin.getLogger().warning("Error showing title: " + e2.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error executing TITLE action: " + e.getMessage());
        }
    }

    /**
     * Obtiene un encantamiento de manera segura, probando diferentes métodos
     */
    private Enchantment getEnchantmentSafely(String name) {
        try {
            // Método principal (1.13+)
            return Enchantment.getByName(name.toUpperCase());
        } catch (Exception e1) {
            try {
                // Intentar por "key" (1.13+)
                Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
                Object bukkitKey = namespacedKeyClass.getConstructor(String.class, String.class)
                        .newInstance("minecraft", name.toLowerCase());

                return (Enchantment) Enchantment.class.getMethod("getByKey", namespacedKeyClass)
                        .invoke(null, bukkitKey);
            } catch (Exception e2) {
                try {
                    // Último intento: usar reflection para obtener por ID (1.8-1.12)
                    int enchantId = getEnchantmentId(name.toUpperCase());
                    if (enchantId != -1) {
                        return (Enchantment) Enchantment.class.getMethod("getById", int.class)
                                .invoke(null, enchantId);
                    }
                } catch (Exception e3) {
                    plugin.getLogger().warning("The enchantment could not be found: " + name);
                }
            }
        }
        return null;
    }

    // Métodos para gestionar el conteo de bloques por jugador
    private void incrementBlockCount(Player player, int groupId) {
        UUID playerId = player.getUniqueId();

        // Asegurarse que tenemos una entrada para este jugador
        if (!playerBlockCounts.containsKey(playerId)) {
            playerBlockCounts.put(playerId, new HashMap<>());
        }

        // Obtener el mapa de conteos para este jugador
        Map<Integer, Integer> counts = playerBlockCounts.get(playerId);

        // Incrementar el contador para este grupo
        int currentCount = counts.getOrDefault(groupId, 0);
        counts.put(groupId, currentCount + 1);
    }

    private int getPlayerBlockCount(Player player, int groupId) {
        UUID playerId = player.getUniqueId();
        if (!playerBlockCounts.containsKey(playerId)) {
            return 0;
        }
        Map<Integer, Integer> counts = playerBlockCounts.get(playerId);
        return counts.getOrDefault(groupId, 0);
    }

    private void resetBlockCount(Player player, int groupId) {
        UUID playerId = player.getUniqueId();
        if (playerBlockCounts.containsKey(playerId)) {
            Map<Integer, Integer> counts = playerBlockCounts.get(playerId);
            counts.put(groupId, 0);
        }
    }

    /**
     * Obtiene el ID numérico de un encantamiento por su nombre para compatibilidad con versiones antiguas
     * @param name Nombre del encantamiento
     * @return ID numérico del encantamiento o -1 si no se encuentra
     */
    private int getEnchantmentId(String name) {
        // Tabla de correspondencia entre nombres e IDs de encantamientos para MC 1.8-1.12
        switch (name) {
            case "PROTECTION_ENVIRONMENTAL": return 0;
            case "PROTECTION_FIRE": return 1;
            case "PROTECTION_FALL": return 2;
            case "PROTECTION_EXPLOSIONS": return 3;
            case "PROTECTION_PROJECTILE": return 4;
            case "OXYGEN": return 5;
            case "WATER_WORKER": return 6;
            case "THORNS": return 7;
            case "DEPTH_STRIDER": return 8;
            case "DAMAGE_ALL": return 16;
            case "DAMAGE_UNDEAD": return 17;
            case "DAMAGE_ARTHROPODS": return 18;
            case "KNOCKBACK": return 19;
            case "FIRE_ASPECT": return 20;
            case "LOOT_BONUS_MOBS": return 21;
            case "DIG_SPEED":
            case "EFFICIENCY": return 32;
            case "SILK_TOUCH": return 33;
            case "DURABILITY":
            case "UNBREAKING": return 34;
            case "LOOT_BONUS_BLOCKS":
            case "FORTUNE": return 35;
            case "ARROW_DAMAGE":
            case "POWER": return 48;
            case "ARROW_KNOCKBACK":
            case "PUNCH": return 49;
            case "ARROW_FIRE":
            case "FLAME": return 50;
            case "ARROW_INFINITE":
            case "INFINITY": return 51;
            case "LUCK":
            case "LUCK_OF_THE_SEA": return 61;
            case "LURE": return 62;
            // 1.9+ (incluido por si acaso)
            case "FROST_WALKER": return 9;
            case "BINDING_CURSE": return 10;
            case "MENDING": return 70;
            case "VANISHING_CURSE": return 71;
            case "SWEEPING_EDGE": return 22;
            default: return -1;
        }
    }
}
