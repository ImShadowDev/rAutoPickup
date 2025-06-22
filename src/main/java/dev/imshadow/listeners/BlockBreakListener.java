package dev.imshadow.listeners;

import dev.imshadow.PickupSystem.AutoPickup;
import dev.imshadow.PickupSystem.AutoSmelt;
import dev.imshadow.rAutoPickup;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class BlockBreakListener implements Listener {

    private final rAutoPickup plugin;
    private final Random random = new Random();
    // Compatibilidad con versiones antiguas y nuevas
    private Enchantment FORTUNE_ENCHANTMENT = null;

    // Lista de bloques que nunca deben ser afectados por Fortune
    private final Set<String> NON_FORTUNE_BLOCKS = new HashSet<>();

    // Lista de bloques configurados para Fortune
    private Set<String> FORTUNE_BLOCKS = new HashSet<>();
    private boolean FORTUNE_ALL_BLOCKS = false;

    // Nuevas listas para bloques deshabilitados
    private Set<String> DISABLED_AUTOPICKUP_BLOCKS = new HashSet<>();
    private Set<String> DISABLED_AUTOSMELT_BLOCKS = new HashSet<>();

    public BlockBreakListener(rAutoPickup plugin) {
        this.plugin = plugin;
        // Inicializar el encantamiento Fortune una vez
        initFortuneEnchantment();
        // Inicializar lista de bloques NO afectados por Fortune
        initNonFortuneBlocks();
        // Cargar configuración de bloques afectados por Fortune
        loadFortuneBlocksConfig();
        // Cargar configuración de bloques deshabilitados
        loadDisabledBlocksConfig();
    }

    /**
     * Método seguro para colorear texto
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
            return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
        }
    }

    /**
     * Carga la configuración de bloques deshabilitados desde config.yml
     */
    public void loadDisabledBlocksConfig() {
        DISABLED_AUTOPICKUP_BLOCKS.clear();
        DISABLED_AUTOSMELT_BLOCKS.clear();

        // Cargar bloques deshabilitados para autopickup
        List<String> disabledAutoPickupBlocks = plugin.getConfigManager().getConfig().getStringList("disable-blocks-autopickup");
        if (disabledAutoPickupBlocks != null) {
            for (String block : disabledAutoPickupBlocks) {
                if (block.equals("*")) {
                    // Si se especifica *, se deshabilitará el autopickup para todos los bloques
                    DISABLED_AUTOPICKUP_BLOCKS.add("*");
                    plugin.getLogger().info("AutoPickup disabled for all blocks (wildcard * found)");
                    break;
                } else {
                    try {
                        // Verificar si el material existe
                        Material.valueOf(block.toUpperCase());
                        DISABLED_AUTOPICKUP_BLOCKS.add(block.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material name in disable-blocks-autopickup: " + block);
                    }
                }
            }
        }

        // Cargar bloques deshabilitados para autosmelt
        List<String> disabledAutoSmeltBlocks = plugin.getConfigManager().getConfig().getStringList("disable-blocks-autosmelt");
        if (disabledAutoSmeltBlocks != null) {
            for (String block : disabledAutoSmeltBlocks) {
                if (block.equals("*")) {
                    // Si se especifica *, se deshabilitará el autosmelt para todos los bloques
                    DISABLED_AUTOSMELT_BLOCKS.add("*");
                    plugin.getLogger().info("AutoSmelt disabled for all blocks (wildcard * found)");
                    break;
                } else {
                    try {
                        // Verificar si el material existe
                        Material.valueOf(block.toUpperCase());
                        DISABLED_AUTOSMELT_BLOCKS.add(block.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material name in disable-blocks-autosmelt: " + block);
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + DISABLED_AUTOPICKUP_BLOCKS.size() + " disabled autopickup blocks");
        plugin.getLogger().info("Loaded " + DISABLED_AUTOSMELT_BLOCKS.size() + " disabled autosmelt blocks");
    }

    /**
     * Verifica si un bloque está deshabilitado para autopickup
     */
    private boolean isAutoPickupDisabled(Material blockType) {
        String blockName = blockType.name();

        // Si hay wildcard, todos los bloques están deshabilitados
        if (DISABLED_AUTOPICKUP_BLOCKS.contains("*")) {
            return true;
        }

        // Verificar si el bloque específico está deshabilitado
        return DISABLED_AUTOPICKUP_BLOCKS.contains(blockName);
    }

    /**
     * Verifica si un bloque está deshabilitado para autosmelt
     */
    private boolean isAutoSmeltDisabled(Material blockType) {
        String blockName = blockType.name();

        // Si hay wildcard, todos los bloques están deshabilitados
        if (DISABLED_AUTOSMELT_BLOCKS.contains("*")) {
            return true;
        }

        // Verificar si el bloque específico está deshabilitado
        return DISABLED_AUTOSMELT_BLOCKS.contains(blockName);
    }

    /**
     * Carga la configuración de bloques afectados por Fortune desde config.yml
     */
    public void loadFortuneBlocksConfig() {
        FORTUNE_BLOCKS.clear();
        FORTUNE_ALL_BLOCKS = false;

        List<String> configBlocks = plugin.getConfigManager().getConfig().getStringList("FORTUNE-BLOCKS");

        // Si la lista está vacía, no aplicar Fortune a ningún bloque
        if (configBlocks == null || configBlocks.isEmpty()) {
            plugin.getLogger().info("No blocks configured for Fortune effect.");
            return;
        }

        // Procesar la lista de bloques
        for (String block : configBlocks) {
            if (block.equals("*")) {
                FORTUNE_ALL_BLOCKS = true;
                plugin.getLogger().info("Fortune will be applied to all blocks (wildcard * found)");
                break;
            } else {
                try {
                    // Verificar si el material existe
                    Material.valueOf(block.toUpperCase());
                    FORTUNE_BLOCKS.add(block.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material name in FORTUNE-BLOCKS: " + block);
                }
            }
        }

        if (!FORTUNE_ALL_BLOCKS) {
            plugin.getLogger().info("Fortune will be applied to " + FORTUNE_BLOCKS.size() + " configured blocks");
        }
    }

    /**
     * Inicializa la lista de bloques que nunca deberían ser afectados por Fortune
     */
    private void initNonFortuneBlocks() {
        // Bloques que no deberían multiplicar sus drops
        NON_FORTUNE_BLOCKS.add("BEDROCK");
        NON_FORTUNE_BLOCKS.add("AIR");
        NON_FORTUNE_BLOCKS.add("WATER");
        NON_FORTUNE_BLOCKS.add("LAVA");
        NON_FORTUNE_BLOCKS.add("FIRE");
        NON_FORTUNE_BLOCKS.add("BARRIER");
        NON_FORTUNE_BLOCKS.add("COMMAND_BLOCK");
        NON_FORTUNE_BLOCKS.add("STRUCTURE_BLOCK");
        NON_FORTUNE_BLOCKS.add("JIGSAW");

        // Bloques decorativos o que se rompen sin herramientas
        NON_FORTUNE_BLOCKS.add("GLASS");
        NON_FORTUNE_BLOCKS.add("ICE");
        NON_FORTUNE_BLOCKS.add("THIN_GLASS");
        NON_FORTUNE_BLOCKS.add("GLASS_PANE");

        // Bloques que explotan
        NON_FORTUNE_BLOCKS.add("TNT");

        // Bloques con inventarios
        NON_FORTUNE_BLOCKS.add("CHEST");
        NON_FORTUNE_BLOCKS.add("TRAPPED_CHEST");
        NON_FORTUNE_BLOCKS.add("ENDER_CHEST");
        NON_FORTUNE_BLOCKS.add("FURNACE");
        NON_FORTUNE_BLOCKS.add("DISPENSER");
        NON_FORTUNE_BLOCKS.add("DROPPER");
        NON_FORTUNE_BLOCKS.add("HOPPER");
        NON_FORTUNE_BLOCKS.add("BREWING_STAND");
        NON_FORTUNE_BLOCKS.add("BEACON");

        // Camas
        NON_FORTUNE_BLOCKS.add("BED");
        NON_FORTUNE_BLOCKS.add("BED_BLOCK");
    }

    /**
     * Inicializa la referencia al encantamiento Fortune según la versión
     */
    private void initFortuneEnchantment() {
        try {
            // Primero intentar con nombres estándar de Bukkit
            FORTUNE_ENCHANTMENT = Enchantment.LOOT_BONUS_BLOCKS;
        } catch (NoSuchFieldError e1) {
            try {
                // obtener por nombre en caso de fallar el primero
                FORTUNE_ENCHANTMENT = Enchantment.getByName("FORTUNE");
            } catch (Exception e2) {
                try {
                    // Último intento: obtener por ID (para 1.8.8)
                    FORTUNE_ENCHANTMENT = (Enchantment) Enchantment.class.getMethod("getById", int.class)
                            .invoke(null, 35); // 35 es el ID de Fortune en 1.8.8
                } catch (Exception e3) {
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // Verificación adicional al inicio para asegurar que el evento no está cancelado
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        // Verificar si el jugador está en modo creativo
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Verificar si el jugador tiene autopickup activado
        if (!plugin.getPlayerDataManager().hasAutoPickupEnabled(player.getUniqueId())) {
            return;
        }

        Block block = event.getBlock();

        // Verificar que el tipo de bloque no sea AIR antes de procesarlo
        if (block.getType() == Material.AIR) {
            return;
        }

        Material blockType = block.getType();

        // Si el bloque está deshabilitado para autopickup, no procesarlo
        if (isAutoPickupDisabled(blockType)) {
            return;
        }

        // Obtener el ítem en la mano del jugador para verificar Fortune
        ItemStack handItem = getItemInHand(player);

        // Verificar si el ítem tiene Fortune y obtener su nivel
        int fortuneLevel = getFortuneLevel(handItem);

        // Lista para almacenar los drops que procesaremos
        List<ItemStack> processedDrops = new ArrayList<>();

        // Primero verificamos si es un bloque personalizado
        AutoPickup autoPickup = plugin.getAutoPickup();
        boolean isCustomBlock = false;
        if (autoPickup != null) {
            isCustomBlock = autoPickup.isCustomBlock(blockType);
        }

        try {
            // Si no es un bloque personalizado, procesamos los drops normalmente
            if (!isCustomBlock) {
                // Obtenemos los drops originales del bloque con compatibilidad de versiones
                Collection<ItemStack> originalDrops = getBlockDrops(block, handItem);

                // Si no hay drops y no es un bloque personalizado, dejamos que el evento siga su curso
                if (originalDrops.isEmpty()) {
                    return; // No cancelamos el evento, dejamos que Minecraft lo maneje
                }

                // Aplicamos Fortune manualmente
                for (ItemStack drop : originalDrops) {
                    // Aplicar Fortune si corresponde
                    if (shouldApplyFortune(block.getType(), drop.getType()) && fortuneLevel > 0) {
                        // Crear una nueva instancia para cada drop procesado
                        int originalAmount = drop.getAmount();
                        int newAmount = calculateFortuneDrops(block.getType(), drop.getType(), fortuneLevel, originalAmount);

                        // Asegurarnos de que el nuevo monto es mayor o igual al original
                        newAmount = Math.max(originalAmount, newAmount);

                        // Crear un nuevo ItemStack con la cantidad correcta
                        ItemStack fortuneDrop = new ItemStack(drop.getType(), newAmount);
                        // Copiar metadatos si existen
                        if (drop.hasItemMeta()) {
                            fortuneDrop.setItemMeta(drop.getItemMeta().clone());
                        }
                        processedDrops.add(fortuneDrop);
                    } else {
                        // Si no aplica fortune, añadir el drop original
                        processedDrops.add(drop.clone());
                    }
                }

                // Procesar autosmelt solo si no está deshabilitado para este bloque
                if (plugin.getPlayerDataManager().hasAutoSmeltEnabled(player.getUniqueId()) &&
                        player.hasPermission(plugin.getConfigManager().getConfig().getString("permissions.AutoSmelt")) &&
                        !isAutoSmeltDisabled(blockType)) {
                    AutoSmelt autoSmelt = plugin.getAutoSmelt();
                    if (autoSmelt != null) {
                        List<ItemStack> smeltedDrops = new ArrayList<>();
                        for (ItemStack item : processedDrops) {
                            if (autoSmelt.isSmeltable(item.getType())) {
                                smeltedDrops.add(autoSmelt.smeltItem(item));
                            } else {
                                smeltedDrops.add(item);
                            }
                        }
                        processedDrops = smeltedDrops;
                    }
                }

                // Manejar inventario lleno correctamente
                handleInventoryDrops(player, processedDrops, block.getLocation());

                // Establecer el bloque a aire después de procesarlo
                block.setType(Material.AIR);

                // Cancelamos el evento para evitar drops duplicados
                event.setCancelled(true);
            }
            else {
                // Es un bloque personalizado, ahora lo procesamos
                // Cancelamos el evento para evitar drops duplicados
                event.setCancelled(true);

                // Procesamos los drops usando AutoPickup con manejo de errores
                try {
                    autoPickup.processBlockDrop(player, block, processedDrops);
                } catch (Exception e) {
                    // Si hay un error en el procesamiento de AutoPickup, manejarlo graciosamente
                    plugin.getLogger().severe("Error processing custom block drop for player " + player.getName() +
                            " and block " + blockType.name() + ": " + e.getMessage());

                    // Fallback: procesar como bloque normal
                    try {
                        Collection<ItemStack> fallbackDrops = getBlockDrops(block, handItem);
                        handleInventoryDrops(player, new ArrayList<>(fallbackDrops), block.getLocation());
                        block.setType(Material.AIR);
                    } catch (Exception fallbackError) {
                        plugin.getLogger().severe("Critical error in fallback processing: " + fallbackError.getMessage());
                        event.setCancelled(false);
                    }
                }
            }
        } catch (Exception generalError) {
            // Manejo de errores general
            plugin.getLogger().severe("Critical error in BlockBreakListener for player " + player.getName() +
                    " and block " + blockType.name() + ": " + generalError.getMessage());
            generalError.printStackTrace();

            // Si hay un error crítico, asegurarnos de que el evento no quede en un estado inconsistente
            if (event.isCancelled()) {
                // Si cancelamos el evento pero algo falló, intentar un procesamiento básico
                try {
                    Collection<ItemStack> emergencyDrops = getBlockDrops(block, handItem);
                    handleInventoryDrops(player, new ArrayList<>(emergencyDrops), block.getLocation());
                    block.setType(Material.AIR);
                } catch (Exception emergencyError) {
                    // Si incluso el procesamiento de emergencia falla, deshocer la cancelación
                    event.setCancelled(false);
                    plugin.getLogger().severe("Emergency processing failed, allowing normal Minecraft behavior");
                }
            }
        }
    }

    /**
     * Maneja los drops considerando si el inventario está lleno
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

    /**
     * Obtiene el ítem en la mano del jugador con compatibilidad entre versiones
     */
    private ItemStack getItemInHand(Player player) {
        try {
            // Para 1.9+
            return player.getInventory().getItemInHand();
        } catch (NoSuchMethodError e) {
            try {
                // Para 1.8.8
                return player.getInventory().getItemInHand();
            } catch (Exception e2) {
                try {
                    // Último intento con el método antiguo directo
                    return player.getItemInHand();
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    /**
     * Obtiene los drops de un bloque con compatibilidad entre versiones
     */
    private Collection<ItemStack> getBlockDrops(Block block, ItemStack tool) {
        try {
            // Para 1.9+
            return block.getDrops(tool);
        } catch (NoSuchMethodError e) {
            try {
                // Para versiones que no soportan getDrops con tool
                return block.getDrops();
            } catch (Exception e2) {
                // Si todo falla, devolvemos una lista vacía
                return new ArrayList<>();
            }
        }
    }

    /**
     * Determina si un bloque debe ser afectado por Fortune basado en la configuración
     */
    private boolean shouldApplyFortune(Material blockType, Material dropType) {
        String blockName = blockType.name();
        String dropName = dropType.name();

        // Primero verificar si está en la lista de bloques que nunca deben ser afectados
        if (isNonFortuneBlock(blockName)) {
            return false;
        }

        // Si se configuró para aplicar Fortune a todos los bloques
        if (FORTUNE_ALL_BLOCKS) {
            return true;
        }

        // Verificar si el bloque está en la lista configurada
        return FORTUNE_BLOCKS.contains(blockName);
    }

    /**
     * Verifica si un bloque está en la lista de bloques que no se afectan por Fortune
     */
    private boolean isNonFortuneBlock(String blockName) {
        // Verificar coincidencia exacta primero
        if (NON_FORTUNE_BLOCKS.contains(blockName)) {
            return true;
        }

        // Verificar si el nombre contiene alguno de los términos en la lista
        for (String nonFortuneBlock : NON_FORTUNE_BLOCKS) {
            if (blockName.contains(nonFortuneBlock)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calcula la cantidad de drops basado en el nivel de Fortune
     */
    private int calculateFortuneDrops(Material blockType, Material dropType, int fortuneLevel, int baseAmount) {
        if (fortuneLevel <= 0) return baseAmount;

        String blockName = blockType.name();
        String dropName = dropType.name();
        int result = baseAmount;

        // Para bloques típicos de minería (diamante, esmeralda, etc.) usar la fórmula estándar
        if (blockName.contains("_ORE")) {
            // Fórmula estándar de Minecraft para Fortune en minerales:
            result = baseAmount * (1 + random.nextInt(fortuneLevel + 1));
        }
        // Redstone, Lapis, y otros que dan múltiples items
        else if (blockName.contains("REDSTONE_ORE") || blockName.contains("LAPIS_ORE") ||
                blockName.equals("GLOWSTONE") || blockName.equals("SEA_LANTERN")) {
            // Fortune incrementa los drops base en un porcentaje
            int bonus = fortuneLevel + random.nextInt(fortuneLevel + 2) - 1;
            bonus = Math.max(0, bonus); // No permitir valores negativos
            result = baseAmount + bonus;
        }
        // Cultivos y semillas
        else if (blockName.contains("CROPS") || blockName.contains("WHEAT") ||
                blockName.contains("POTATO") || blockName.contains("CARROT") ||
                blockName.contains("BEETROOT") || blockName.contains("NETHER_WART") ||
                blockName.contains("COCOA") || blockName.contains("MELON")) {
            // Para cultivos, aumentar basado en nivel de Fortune
            int bonus = random.nextInt(fortuneLevel + 2);
            result = baseAmount + bonus;
        }
        // Hojas (manzanas, palos y brotes)
        else if (blockName.contains("LEAVES")) {
            // Aumentar la posibilidad de drops de hojas
            double chance = random.nextDouble();
            // Fortune aumenta la posibilidad significativamente
            if (chance < 0.05 * fortuneLevel) {
                result = baseAmount + fortuneLevel;
            }
        }
        // Grava -> sílex
        else if (blockName.equals("GRAVEL") && dropName.equals("FLINT")) {
            // Aumentar probabilidad basada en Fortune
            result = baseAmount + fortuneLevel;
        }
        // Bloques genéricos - fórmula general para Fortune
        else {
            // Probabilidad de 30% * nivel de Fortune de obtener drops adicionales
            if (random.nextDouble() < 0.3 * fortuneLevel) {
                result = baseAmount + random.nextInt(fortuneLevel + 1);
            }
        }

        return result;
    }

    /**
     * Obtiene el nivel de Fortune de un item
     */
    private int getFortuneLevel(ItemStack item) {
        if (item == null) return 0;

        // Si no se pudo inicializar el encantamiento, intentar de nuevo
        if (FORTUNE_ENCHANTMENT == null) {
            initFortuneEnchantment();
            if (FORTUNE_ENCHANTMENT == null) return 0;
        }

        try {
            // Método directo usando el encantamiento
            if (item.containsEnchantment(FORTUNE_ENCHANTMENT)) {
                return item.getEnchantmentLevel(FORTUNE_ENCHANTMENT);
            }

            // Método alternativo para versiones antiguas
            if (item.getEnchantments() != null) {
                for (Enchantment ench : item.getEnchantments().keySet()) {
                    if (ench.getName().contains("FORTUNE") || ench.getName().contains("LOOT_BONUS_BLOCKS")) {
                        return item.getEnchantmentLevel(ench);
                    }
                }
            }
        } catch (Exception e) {
            // Silenciar la excepción y devolver 0
        }

        return 0;
    }
}
