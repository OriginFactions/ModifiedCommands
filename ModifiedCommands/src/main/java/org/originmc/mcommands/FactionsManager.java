package org.originmc.mcommands;

import org.bukkit.entity.Player;
import org.originmc.mcommands.factions.api.FactionsHelper;

import java.util.List;

public final class FactionsManager {

    private final FactionsHelper helper;

    FactionsManager(FactionsHelper helper) {
        this.helper = helper;
    }

    public boolean isInTerritory(Player player, List<String> factions) {
        return helper.isInTerritory(player, factions);
    }

}
