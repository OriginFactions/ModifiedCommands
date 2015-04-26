package org.originmc.mcommands.factions.v2_6;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.entity.Player;
import org.originmc.mcommands.factions.api.FactionsHelper;

import java.util.List;

public class FactionsHelperImpl implements FactionsHelper {

    private static final String PERMISSION_TERRITORY = "mcommands.bypass.territory";

    public boolean isInTerritory(Player player, List<String> factions) {
        if (factions.isEmpty()) return true;
        if (player.hasPermission(PERMISSION_TERRITORY)) return false;

        UPlayer uplayer = UPlayer.get(player);
        PS ps = com.massivecraft.massivecore.ps.PS.valueOf(player.getLocation());
        Faction faction = BoardColls.get().getFactionAt(ps);
        Rel rel = uplayer.getRelationTo(faction);

        for (String f : factions) {
            if ((f.equals("Rel:Leader") && rel.equals(Rel.LEADER)) ||
                    (f.equals("Rel:Officer") && rel.equals(Rel.OFFICER)) ||
                    (f.equals("Rel:Member") && rel.equals(Rel.MEMBER)) ||
                    (f.equals("Rel:Recruit") && rel.equals(Rel.RECRUIT)) ||
                    (f.equals("Rel:Ally") && rel.equals(Rel.ALLY)) ||
                    (f.equals("Rel:Truce") && rel.equals(Rel.TRUCE)) ||
                    (f.equals("Rel:Neutral") && rel.equals(Rel.NEUTRAL)) ||
                    (f.equals("Rel:Enemy") && rel.equals(Rel.ENEMY)) ||
                    (f.equalsIgnoreCase(faction.getName())) ||
                    (f.equalsIgnoreCase(faction.getName().substring(2)))) {
                return false;
            }
        }

        return true;
    }

}
