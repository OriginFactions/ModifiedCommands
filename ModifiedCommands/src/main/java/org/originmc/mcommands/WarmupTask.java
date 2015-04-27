package org.originmc.mcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class WarmupTask implements Runnable {

    private static final String PERMISSION_COOLDOWN = "mcommands.bypass.cooldowns";

    private final int id;

    private final Modifier modifier;

    private final Player player;

    private final String command;

    private final PlayerListener playerListener;

    private final ModifiedCommands plugin;

    WarmupTask(ModifiedCommands plugin, PlayerListener playerListener, Player player, String command, Modifier modifier) {
        this.id = Bukkit.getScheduler().runTaskLater(plugin, this, modifier.getWarmup() * 20).getTaskId();
        this.plugin = plugin;
        this.modifier = modifier;
        this.player = player;
        this.command = command;
        this.playerListener = playerListener;
    }

    @Override
    public void run() {
        // Warmup finished, therefore should be removed
        playerListener.removeWarmup(player.getUniqueId());

        // Do nothing if player is no longer online
        if (!player.isOnline()) return;

        // Start the command cooldown
        if (modifier.getCooldown() > 0 && !player.hasPermission(PERMISSION_COOLDOWN)) {
            plugin.setCooldown(player.getUniqueId(), modifier.getRegex());
        }

        // Do nothing if player cannot afford the command
        if (!playerListener.billPlayer(player, modifier.getPrice())) return;

        // Execute the command
        Bukkit.dispatchCommand(player, command);

        // Send player the modifiers set completion message
        if (modifier.getMessage() != null && !modifier.getMessage().isEmpty()) {
            player.sendMessage(modifier.getMessage());
        }
    }

    public void stopTask() {
        Bukkit.getScheduler().cancelTask(id);
    }

}
