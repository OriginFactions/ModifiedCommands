package org.originmc.mcommands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.mcommands.factions.api.FactionsHelper;
import org.originmc.mcommands.factions.api.FactionsHelperImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ModifiedCommands extends JavaPlugin {

    private Map<String, Map<UUID, Long>> cooldowns;

    private final File dataFile = new File(getDataFolder(), "data.yml");

    private Economy economy;

    private FactionsManager factionsManager;

    private FileConfiguration dataConfig;

    private Settings settings;

    public Long getCooldown(UUID player, String regex) {
        if (cooldowns.containsKey(regex) && cooldowns.get(regex).containsKey(player)) {
            return cooldowns.get(regex).get(player);
        }
        return 0L;
    }

    public void setCooldown(UUID player, String regex) {
        Map<UUID, Long> cooldown = new HashMap<>();
        cooldown.put(player, System.currentTimeMillis());
        cooldowns.put(regex, cooldown);
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public FactionsManager getFactionsManager() {
        return factionsManager;
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public Settings getSettings() {
        return settings;
    }

    @Override
    public void onEnable() {
        // Load settings
        saveDefaultConfig();

        settings = new Settings(this);
        if (settings.isOutdated()) {
            getLogger().warning("**WARNING**");
            getLogger().warning("Your configuration file is outdated.");
            getLogger().warning("Backup your old file and then delete it to generate a new copy.");
        }

        // Load database
        if (!dataFile.exists()) {
            saveResource("data.yml", false);
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadData();

        // Initialize plugin state
        integrateVault();
        integrateFactions();
        new PlayerListener(this);

        getLogger().info(getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        saveData();
        getLogger().info(getName() + " has been disabled!");
    }

    private void integrateFactions() {
        // Use a dummy implementation if Factions is disabled
        if (!getSettings().useFactions()) {
            factionsManager = new FactionsManager(new FactionsHelperImpl());
            return;
        }

        // Determine if Factions is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
        if (plugin == null) {
            getLogger().info("Factions integration is disabled because it is not loaded.");

            // Use the dummy helper implementation if Factions isn't loaded
            factionsManager = new FactionsManager(new FactionsHelperImpl());
            return;
        }

        // Determine which helper class implementation to use
        FactionsHelper helper;
        String[] v = plugin.getDescription().getVersion().split("\\.");
        String version = v[0] + "_" + v[1];
        String className = "org.originmc.mcommands.factions.v" + version + ".FactionsHelperImpl";

        try {
            // Try to create a new helper instance
            helper = (FactionsHelper) Class.forName(className).newInstance();

            // Create the manager which is what the plugin will interact with
            factionsManager = new FactionsManager(helper);
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible WorldGuard
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Factions integration due to errors.");
            getLogger().warning("This is most likely due to a newer Factions.");

            // Use the dummy helper implementation since WG isn't supported
            factionsManager = new FactionsManager(new FactionsHelperImpl());

            // Let's leave a stack trace in console for reporting
            e.printStackTrace();
        }
    }

    private void integrateVault() {
        // Determine if Vault is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (plugin == null) {
            getLogger().info("Vault integration is disabled as it is not loaded");
            return;
        }

        ServicesManager sm = getServer().getServicesManager();
        RegisteredServiceProvider<Economy> economyProvider = sm.getRegistration(Economy.class);

        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        } else {
            // No economy provider has been found
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Economy integration!");
            getLogger().warning("This is most likely due no economy plugin being installed");
            getLogger().warning("Certain features of this plugin may not function correctly");
        }
    }

    private void loadData() {
        // Reset cooldowns to prevent errors when reloading entire plugin
        this.cooldowns = new HashMap<>();

        for (Map<?,?> modifierOptions : getDataConfig().getMapList("commands")) {
            // Do nothing if regex is not defined within this mapList
            Object regexObject = modifierOptions.get("regex");
            if (regexObject == null || !(regexObject instanceof String)) continue;

            // Do nothing if this regex command is no longer included in the config
            String regex = (String) regexObject;
            if (!getSettings().getAllModifiers().containsKey(regex)) continue;

            // Do nothing if "cooldowns" configuration section is invalid
            Object cooldownsObject = modifierOptions.get("cooldowns");
            if (cooldownsObject == null || !(cooldownsObject instanceof Map<?,?>)) continue;

            // Iterate through all active cooldowns
            Map<?,?> cooldowns = (Map<?,?>) cooldownsObject;
            int cooldown = getSettings().getAllModifiers().get(regex).getCooldown();

            for (Object uuidObject : cooldowns.keySet()) {
                // Do nothing if uuid is not valid
                if (uuidObject == null || !(uuidObject instanceof String)) continue;

                // Do nothing if time is not valid
                Object timeObject = cooldowns.get(uuidObject);
                if (timeObject == null || !(timeObject instanceof Long)) continue;

                // Do nothing if this cooldown has expired
                long time = (Long) timeObject;
                long remaining = cooldown - ((System.currentTimeMillis() - time) / 1000L);
                if (remaining <= 0) continue;

                // Add this cooldown to the active cooldowns map
                UUID uuid = UUID.fromString((String) uuidObject);
                Map<UUID, Long> cooldownMap = new HashMap<>();
                cooldownMap.put(uuid, time);
                this.cooldowns.put(regex, cooldownMap);
            }
        }
    }

    private void saveData() {
        // Create commands map list
        List<Map<Object, Object>> commands = new ArrayList<>();

        // Iterate through all commands with cooldowns
        for (String regex : this.cooldowns.keySet()) {
            // Do nothing if modifier is no longer in effect
            if (!getSettings().getAllModifiers().containsKey(regex)) continue;

            // Create the new command and cooldown sections
            Map<Object, Object> command = new HashMap<>();
            Map<Object, Object> cooldowns = new HashMap<>();

            int cooldown = getSettings().getAllModifiers().get(regex).getCooldown();

            // Iterate through all player cooldowns
            for (UUID player : this.cooldowns.get(regex).keySet()) {
                // Do nothing if cooldown is no longer in effect
                if (cooldown - ((System.currentTimeMillis() - this.cooldowns.get(regex).get(player)) / 1000L) <= 0) continue;

                // Add player to the active cooldowns list
                cooldowns.put(player.toString(), this.cooldowns.get(regex).get(player));
            }

            // Add all information to the command then add to commands list
            command.put("regex", regex);
            command.put("cooldowns", cooldowns);
            commands.add(command);
        }

        // Overwrite commands config section
        dataConfig.set("commands", commands);

        // Save to file
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        long time = System.currentTimeMillis();
        reloadConfig();
        getSettings().load();
        time = System.currentTimeMillis() - time;
        sender.sendMessage(ChatColor.GREEN + getName() + " config reloaded. (Took " + time + "ms)");
        return true;
    }

}
