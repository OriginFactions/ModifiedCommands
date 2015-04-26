package org.originmc.mcommands.factions.api;

import org.bukkit.entity.Player;

import java.util.List;

public interface FactionsHelper {

    boolean isInTerritory(Player player, List<String> factions);

}
