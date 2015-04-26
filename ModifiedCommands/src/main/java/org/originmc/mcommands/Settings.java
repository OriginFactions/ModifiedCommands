package org.originmc.mcommands;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Settings {

    private final ModifiedCommands plugin;

    private Map<String, Modifier> allModifiers;

    private Map<String, Modifier> highest;

    private Map<String, Modifier> high;

    private Map<String, Modifier> normal;

    private Map<String, Modifier> low;

    private Map<String, Modifier> lowest;

    private Map<String, Modifier> monitor;

    Settings(ModifiedCommands plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        Configuration defaults = plugin.getConfig().getDefaults();
        defaults.set("commands", new ArrayList<>());
        allModifiers = new HashMap<>();
        highest = new HashMap<>();
        high = new HashMap<>();
        normal = new HashMap<>();
        low = new HashMap<>();
        lowest = new HashMap<>();
        monitor = new HashMap<>();

        for (Map<?, ?> modifierOptions : plugin.getConfig().getMapList("commands")) {
            // Do nothing if modifier has no regex matcher
            if (!modifierOptions.containsKey("regex")) continue;

            Modifier modifier = new Modifier(modifierOptions);
            String regex = (String) modifierOptions.get("regex");

            // Set the priority to normal if not specified
            if (!modifierOptions.containsKey("priority")) {
                normal.put(regex, modifier);
                allModifiers.put(regex, modifier);
                continue;
            }

            // Load all modifiers configuration to local cache
            switch (modifierOptions.get("priority").toString()) {
                case "highest":
                    highest.put(regex, modifier);
                    allModifiers.put(regex, modifier);
                    break;

                case "high":
                    high.put(regex, modifier);
                    allModifiers.put(regex, modifier);
                    break;

                case "low":
                    low.put(regex, modifier);
                    allModifiers.put(regex, modifier);
                    break;

                case "lowest":
                    lowest.put(regex, modifier);
                    allModifiers.put(regex, modifier);
                    break;

                case "monitor":
                    monitor.put(regex, modifier);
                    allModifiers.put(regex, modifier);
                    break;

                default:
                    normal.put(regex, modifier);
                    allModifiers.put(regex, modifier);
                    break;
            }
        }
    }

    public int getConfigVersion() {
        return plugin.getConfig().getInt("config-version", 0);
    }

    public int getLatestConfigVersion() {
        return plugin.getConfig().getDefaults().getInt("config-version", 0);
    }

    public boolean isOutdated() {
        return getConfigVersion() < getLatestConfigVersion();
    }

    public boolean useFactions() {
        return plugin.getConfig().getBoolean("factions");
    }

    public String getPriceSuccessMessage() {
        String message = plugin.getConfig().getString("price-success-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public String getPriceFailedMessage() {
        String message = plugin.getConfig().getString("price-failed-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public String getCooldownMessage() {
        String message = plugin.getConfig().getString("cooldown-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public String getWarmupStartMessage() {
        String message = plugin.getConfig().getString("warmup-start-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public String getWarmupDoubleMessage() {
        String message = plugin.getConfig().getString("warmup-double-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public String getWarmupFailedMessage() {
        String message = plugin.getConfig().getString("warmup-failed-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public String getFactionMessage() {
        String message = plugin.getConfig().getString("faction-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public Map<String, Modifier> getAllModifiers() {
        return allModifiers;
    }

    public Map<String, Modifier> getHighest() {
        return highest;
    }

    public Map<String, Modifier> getHigh() {
        return high;
    }

    public Map<String, Modifier> getNormal() {
        return normal;
    }

    public Map<String, Modifier> getLow() {
        return low;
    }

    public Map<String, Modifier> getLowest() {
        return lowest;
    }

    public Map<String, Modifier> getMonitor() {
        return monitor;
    }

}
