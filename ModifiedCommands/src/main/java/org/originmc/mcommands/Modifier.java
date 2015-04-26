package org.originmc.mcommands;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Modifier {

    private final double price;

    private final int cooldown;

    private final int warmup;

    private final String regex;

    private final String permission;

    private final String permissionMessage;

    private final String message;

    private final String alias;

    private List<String> factions;

    Modifier(Map<?, ?> modifier) {
        Object price = modifier.get("price");
        if (price != null && price instanceof Double) {
            this.price = (double) price;
        } else {
            this.price = 0;
        }

        Object cooldown = modifier.get("cooldown");
        if (cooldown != null && cooldown instanceof Integer) {
            this.cooldown = (int) cooldown;
        } else {
            this.cooldown = 0;
        }

        Object warmup = modifier.get("warmup");
        if (warmup != null && warmup instanceof Integer) {
            this.warmup = (int) warmup;
        } else {
            this.warmup = 0;
        }

        Object regex = modifier.get("regex");
        if (regex != null && regex instanceof String) {
            this.regex = (String) regex;
        } else {
            this.regex = null;
        }

        Object permission = modifier.get("permission");
        if (permission != null && permission instanceof String) {
            this.permission = (String) permission;
        } else {
            this.permission = null;
        }

        Object permissionMessage = modifier.get("permission-message");
        if (permissionMessage != null && permissionMessage instanceof String) {
            this.permissionMessage = (String) permissionMessage;
        } else {
            this.permissionMessage = null;
        }

        Object message = modifier.get("message");
        if (message != null && message instanceof String) {
            this.message = (String) message;
        } else {
            this.message = null;
        }

        Object alias = modifier.get("alias");
        if (alias != null && alias instanceof String) {
            this.alias = (String) alias;
        } else {
            this.alias = null;
        }

        this.factions = new ArrayList<>();
        Object factions = modifier.get("factions");
        if (factions != null && factions instanceof List) {
            try {
                this.factions = (List<String>) factions;
            } catch(ClassCastException e) {
                this.factions = new ArrayList<>();
            }
        }
    }

    public double getPrice() {
        return price;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getWarmup() {
        return warmup;
    }

    public String getRegex() {
        return regex;
    }

    public String getPermission() {
        return permission;
    }

    public String getPermissionMessage() {
        return permissionMessage != null ? ChatColor.translateAlternateColorCodes('&', permissionMessage) : null;
    }

    public String getMessage() {
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public String getAlias() {
        return alias;
    }

    public List<String> getFactions() {
        return factions;
    }

}
