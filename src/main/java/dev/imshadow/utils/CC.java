package dev.imshadow.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class CC {

    public static String translate (String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> translateList(List<String> lore) {
        List<String> translatedLore = new ArrayList<>();
        for (String line : lore) {
            translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return translatedLore;
    }
}