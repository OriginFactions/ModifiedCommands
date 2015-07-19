package org.originmc.mcommands.factions.v1_8;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.Rel;
import org.bukkit.entity.Player;
import org.originmc.mcommands.factions.api.FactionsHelper;

import java.util.List;

public class FactionsHelperImpl implements FactionsHelper {

    @Override
    public boolean isInTerritory(Player player, List<String> factions) {
        FPlayer fplayer = FPlayers.i.get(player);
        FLocation flocation = new FLocation(player.getLocation());
        Faction faction = Board.getFactionAt(flocation);
        Rel rel = fplayer.getRelationTo(faction);

        for (String f : factions) {
            if ((f.equals("Rel:Leader") && rel.equals(Rel.LEADER)) ||
                    (f.equals("Rel:Officer") && rel.equals(Rel.OFFICER)) ||
                    (f.equals("Rel:Member") && rel.equals(Rel.MEMBER)) ||
                    (f.equals("Rel:Recruit") && rel.equals(Rel.RECRUIT)) ||
                    (f.equals("Rel:Ally") && rel.equals(Rel.ALLY)) ||
                    (f.equals("Rel:Truce") && rel.equals(Rel.TRUCE)) ||
                    (f.equals("Rel:Neutral") && rel.equals(Rel.NEUTRAL)) ||
                    (f.equals("Rel:Enemy") && rel.equals(Rel.ENEMY)) ||
                    (f.equalsIgnoreCase(faction.getTag())) ||
                    (f.equalsIgnoreCase(faction.getTag().substring(2)))) {
                return false;
            }
        }

        return true;
    }

}
