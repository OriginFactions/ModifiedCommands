package org.originmc.mcommands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerListener implements Listener {

    private static final String PERMISSION_COOLDOWN = "mcommands.bypass.cooldowns";

    private static final String PERMISSION_ECONOMY = "mcommands.bypass.economy";

    private static final String PERMISSION_TERRITORY = "mcommands.bypass.territory";

    private static final String PERMISSION_WARMUP = "mcommands.bypass.warmups";

    private final ModifiedCommands plugin;

    private final Map<UUID, WarmupTask> warmupTasks = new HashMap<>();

    PlayerListener(ModifiedCommands plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void highestCommandPreProcess(PlayerCommandPreprocessEvent event) {
        for (String regex : plugin.getSettings().getHighest().keySet()) {
            if (event.getMessage().toLowerCase().matches(regex.toLowerCase())) {
                handleCommand(event, plugin.getSettings().getHighest().get(regex));
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void highCommandPreProcess(PlayerCommandPreprocessEvent event) {
        for (String regex : plugin.getSettings().getHigh().keySet()) {
            if (event.getMessage().toLowerCase().matches(regex.toLowerCase())) {
                handleCommand(event, plugin.getSettings().getHigh().get(regex));
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void normalCommandPreProcess(PlayerCommandPreprocessEvent event) {
        for (String regex : plugin.getSettings().getNormal().keySet()) {
            if (event.getMessage().toLowerCase().matches(regex.toLowerCase())) {
                handleCommand(event, plugin.getSettings().getNormal().get(regex));
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void lowCommandPreProcess(PlayerCommandPreprocessEvent event) {
        for (String regex : plugin.getSettings().getLow().keySet()) {
            if (event.getMessage().toLowerCase().matches(regex.toLowerCase())) {
                handleCommand(event, plugin.getSettings().getLow().get(regex));
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void lowestCommandPreProcess(PlayerCommandPreprocessEvent event) {
        for (String regex : plugin.getSettings().getLowest().keySet()) {
            if (event.getMessage().toLowerCase().matches(regex.toLowerCase())) {
                handleCommand(event, plugin.getSettings().getLowest().get(regex));
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorCommandPreProcess(PlayerCommandPreprocessEvent event) {
        for (String regex : plugin.getSettings().getMonitor().keySet()) {
            if (event.getMessage().toLowerCase().matches(regex.toLowerCase())) {
                handleCommand(event, plugin.getSettings().getMonitor().get(regex));
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmup(PlayerMoveEvent event) {
        // Do nothing if player does not have a warmup
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!warmupTasks.containsKey(uuid)) return;

        // Do nothing if player has not moved a whole block
        Location t = event.getTo();
        Location f = event.getFrom();
        if (!(t.getBlockX() != f.getBlockX() ||
                t.getBlockY() != f.getBlockY() ||
                t.getBlockZ() != f.getBlockZ())) {
            return;
        }

        // Remove the players warmup
        removeWarmup(uuid);

        // Send player a message
        if (plugin.getSettings().getWarmupFailedMessage() != null &&
                !plugin.getSettings().getWarmupFailedMessage().isEmpty()) {
            player.sendMessage(plugin.getSettings().getWarmupFailedMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmup(EntityDamageEvent event) {
        // Do nothing if entity is not a player
        if (!(event.getEntity() instanceof Player)) return;

        // Do nothing if player does not have a warmup
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();
        if (!warmupTasks.containsKey(uuid)) return;

        // Remove the players warmup
        removeWarmup(uuid);

        // Send player a message
        if (plugin.getSettings().getWarmupFailedMessage() != null &&
                !plugin.getSettings().getWarmupFailedMessage().isEmpty()) {
            player.sendMessage(plugin.getSettings().getWarmupFailedMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmup(PlayerTeleportEvent event) {
        // Do nothing if player does not have a warmup
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!warmupTasks.containsKey(uuid)) return;

        // Remove the players warmup
        removeWarmup(uuid);

        // Send player a message
        if (plugin.getSettings().getWarmupFailedMessage() != null &&
                !plugin.getSettings().getWarmupFailedMessage().isEmpty()) {
            player.sendMessage(plugin.getSettings().getWarmupFailedMessage());
        }
    }

    private void handleCommand(PlayerCommandPreprocessEvent event, Modifier modifier) {
        // Attempt to alias the command
        String command = event.getMessage();
        String regex = modifier.getRegex();
        if (modifier.getAlias() != null) {
            Map<String, String> replacements = new HashMap<>();
            String[] args = command.split(" ");
            args[0] = args[0].substring(1);

            for (int c = 0; c < args.length; c++) {
                if (!modifier.getAlias().contains("%" + c)) continue;
                replacements.put("%" + c, args[c]);
                replacements.put("%" + c + "+", StringUtils.join(Arrays.copyOfRange(args, c, args.length), " "));
            }

            command = modifier.getAlias();

            for (String replacement : replacements.keySet()) {
                command = command.replace(replacement, replacements.get(replacement));
            }

            event.setMessage(command);
        }

        // Deny the command if player does not have permission
        Player player = event.getPlayer();
        if (modifier.getPermission() != null && !player.hasPermission(modifier.getPermission())) {
            event.setCancelled(true);

            // Send permission message if it exists
            String message = modifier.getPermissionMessage();
            if (message != null && !message.isEmpty()) {
                player.sendMessage(message);
            }
            return;
        }

        // Deny the command if player is within the specified territories
        if (!modifier.getFactions().isEmpty() && !player.hasPermission(PERMISSION_TERRITORY) &&
                plugin.getFactionsManager().isInTerritory(player, modifier.getFactions())) {
            event.setCancelled(true);

            String message = plugin.getSettings().getFactionMessage();
            if (message != null && !message.isEmpty()) {
                player.sendMessage(message);
            }
            return;
        }

        // Check if the command has a cooldown and if player has permission
        UUID uuid = player.getUniqueId();
        int cooldown = modifier.getCooldown();
        if (cooldown > 0 && !player.hasPermission(PERMISSION_COOLDOWN)) {
            // Deny the command if player is on a cooldown
            int remaining = (int) (cooldown - ((System.currentTimeMillis() - plugin.getCooldown(uuid, regex)) / 1000L));

            if (remaining > 0L) {
                player.sendMessage(plugin.getSettings().getCooldownMessage()
                        .replace("%t", DurationUtils.format(remaining))
                        .replace("%c", command));
                event.setCancelled(true);
                return;
            }
        }

        // Check if the command has a warmup and if player has permission
        if (!player.hasPermission(PERMISSION_WARMUP) && modifier.getWarmup() > 0) {
            if (warmupTasks.containsKey(uuid)) {
                // Send player a message as they are already on a warmup
                player.sendMessage(plugin.getSettings().getWarmupDoubleMessage());
            } else {
                // Send player a message that the warmup is starting
                player.sendMessage(plugin.getSettings().getWarmupStartMessage()
                        .replace("%t", "" + DurationUtils.format(modifier.getWarmup()))
                        .replace("%c", command));

                // Start the warmup for the player
                warmupTasks.put(uuid, new WarmupTask(plugin, this, player, command.substring(1), modifier));
            }

            event.setCancelled(true);
            return;
        }

        // Start the command cooldown
        if (cooldown > 0 && !player.hasPermission(PERMISSION_COOLDOWN)) {
            plugin.setCooldown(uuid, regex);
        }

        // Bill the player if the command has a price
        if (!billPlayer(player, modifier.getPrice())) {
            event.setCancelled(true);
            return;
        }

        if (modifier.getMessage() != null && !modifier.getMessage().isEmpty()) {
            player.sendMessage(modifier.getMessage());
        }
    }

    public boolean billPlayer(Player player, double price) {
        // Do nothing if economy is not enabled
        if (plugin.getEconomy() == null) return true;

        // Do nothing if player has permission
        if (player.hasPermission(PERMISSION_ECONOMY)) return true;

        // Do nothing if command is free
        if (price == 0) return true;

        // Deny command if player does not have enough money
        double balance = plugin.getEconomy().getBalance(player);
        if (balance < price) {
            player.sendMessage(plugin.getSettings().getPriceFailedMessage()
                    .replace("%m", String.valueOf(price)));
            return false;
        }

        // Withdraw money from player
        plugin.getEconomy().withdrawPlayer(player, price);

        // Send player a message
        player.sendMessage(plugin.getSettings().getPriceSuccessMessage()
                .replace("%m", String.valueOf(price)));

        return true;
    }

    public void removeWarmup(UUID uuid) {
        warmupTasks.remove(uuid).stopTask();
    }

}
