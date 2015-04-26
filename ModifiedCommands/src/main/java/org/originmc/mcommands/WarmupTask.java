package org.originmc.mcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class WarmupTask implements Runnable {

    private final int id;

    private final Modifier modifier;

    private final Player player;

    private final String command;

    private final PlayerListener playerListener;

    WarmupTask(ModifiedCommands plugin, PlayerListener playerListener, Player player, String command, Modifier modifier) {
        this.id = Bukkit.getScheduler().runTaskLater(plugin, this, modifier.getWarmup() * 20).getTaskId();
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

        // Do nothing if the command is currently on cooldown
        if (!playerListener.handleCooldown(player, modifier, command)) return;

        // Do nothing if player cannot afford the command
        if (!playerListener.billPlayer(player, modifier.getPrice())) return;

        if (modifier.getMessage() != null && !modifier.getMessage().isEmpty()) {
            player.sendMessage(modifier.getMessage());
        }

        // Execute the command
        Bukkit.dispatchCommand(player, command);
    }

    public void stopTask() {
        Bukkit.getScheduler().cancelTask(id);
    }

}
