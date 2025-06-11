package dev.imshadow.PickupSystem;

import dev.imshadow.rAutoPickup;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AutoSmelt {

    private final rAutoPickup plugin;
    private final Map<Material, Material> smeltableItems = new HashMap<>();
    private final int serverVersion;

    public AutoSmelt(rAutoPickup plugin) {
        this.plugin = plugin;

        // Determinar la versión del servidor
        String version = plugin.getServer().getBukkitVersion();
        String[] parts = version.split("\\.");
        serverVersion = Integer.parseInt(parts[1].split("-")[0]);

        registerSmeltableItems();
    }

    private void registerSmeltableItems() {
        // Elementos básicos que existen en todas las versiones
        addSmeltablePair("IRON_ORE", "IRON_INGOT");
        addSmeltablePair("GOLD_ORE", "GOLD_INGOT");
        addSmeltablePair("SAND", "GLASS");
        addSmeltablePair("COBBLESTONE", "STONE");
        addSmeltablePair("CLAY_BALL", "BRICK");
        addSmeltablePair("NETHERRACK", "NETHER_BRICK");

        // Elementos que existen en versiones específicas
        if (serverVersion >= 13) {
            // Añadir elementos de 1.13+
            addSmeltablePair("CLAY", "TERRACOTTA");
            addSmeltablePair("CACTUS", "GREEN_DYE");
            addSmeltablePair("OAK_LOG", "CHARCOAL");
            addSmeltablePair("SPRUCE_LOG", "CHARCOAL");
            addSmeltablePair("BIRCH_LOG", "CHARCOAL");
            addSmeltablePair("JUNGLE_LOG", "CHARCOAL");
            addSmeltablePair("ACACIA_LOG", "CHARCOAL");
            addSmeltablePair("DARK_OAK_LOG", "CHARCOAL");
            addSmeltablePair("NETHER_QUARTZ_ORE", "QUARTZ");
        } else {
            // Versiones anteriores a 1.13 (nombres antiguos)
            addSmeltablePair("CLAY", "HARD_CLAY");
            addSmeltablePair("CACTUS", "INK_SACK", (short) 2); // Verde oscuro en 1.8-1.12
            addSmeltablePair("LOG", "COAL", (short) 1); // Carbón vegetal en 1.8-1.12
            addSmeltablePair("LOG_2", "COAL", (short) 1); // Para los otros tipos de madera
            addSmeltablePair("QUARTZ_ORE", "QUARTZ");
        }

        // Versión 1.16+ (Nether Update)
        if (serverVersion >= 16) {
            addSmeltablePair("ANCIENT_DEBRIS", "NETHERITE_SCRAP");
        }

        // Versión 1.17+ (Caves & Cliffs)
        if (serverVersion >= 17) {
            addSmeltablePair("COPPER_ORE", "COPPER_INGOT");
            addSmeltablePair("DEEPSLATE_IRON_ORE", "IRON_INGOT");
            addSmeltablePair("DEEPSLATE_GOLD_ORE", "GOLD_INGOT");
            addSmeltablePair("DEEPSLATE_COPPER_ORE", "COPPER_INGOT");
            addSmeltablePair("RAW_IRON", "IRON_INGOT");
            addSmeltablePair("RAW_GOLD", "GOLD_INGOT");
            addSmeltablePair("RAW_COPPER", "COPPER_INGOT");
        }
    }

    private void addSmeltablePair(String oreName, String resultName) {
        try {
            Material ore = Material.valueOf(oreName);
            Material result = Material.valueOf(resultName);
            smeltableItems.put(ore, result);
        } catch (IllegalArgumentException e) {
            // Material no disponible en esta versión, ignorar silenciosamente
        }
    }

    private void addSmeltablePair(String oreName, String resultName, short data) {
        try {
            Material ore = Material.valueOf(oreName);
            Material result = Material.valueOf(resultName);
            smeltableItems.put(ore, result);
            // Nota: La información de la data se manejará en métodos específicos para versiones antiguas
        } catch (IllegalArgumentException e) {
            // Material no disponible en esta versión, ignorar silenciosamente
        }
    }

    public ItemStack smeltItem(ItemStack item) {
        Material result = smeltableItems.get(item.getType());

        if (result != null) {
            if (serverVersion < 13 && result == Material.valueOf("COAL") && item.getType().toString().contains("LOG")) {
                // Manejar carbón vegetal en versiones antiguas
                return new ItemStack(result, item.getAmount(), (short) 1);
            } else if (serverVersion < 13 && result == Material.valueOf("INK_SACK") && item.getType() == Material.valueOf("CACTUS")) {
                // Manejar tinte verde en versiones antiguas
                return new ItemStack(result, item.getAmount(), (short) 2);
            } else {
                return new ItemStack(result, item.getAmount());
            }
        }
        return item;
    }

    public boolean isSmeltable(Material material) {
        return smeltableItems.containsKey(material);
    }
}