package dev.imshadow.utils;

import org.bukkit.ChatColor;

public class CC {
    /**
     * Colorize a string with Minecraft color codes
     *
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // Método alternativo por si hay problemas con la firma del método anterior
    public static String translateColorCodes(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}