package org.originmc.mcommands.factions.v1_6;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import org.bukkit.entity.Player;
import org.originmc.mcommands.factions.api.FactionsHelper;

import java.util.List;

public class FactionsHelperImpl implements FactionsHelper {

    @Override
    public boolean isInTerritory(Player player, List<String> factions) {
        FLocation flocation = new FLocation(player.getLocation());
        FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Board.getInstance().getFactionAt(flocation);
        Relation rel = fplayer.getRelationTo(faction);

        for (String f : factions) {
            if ((f.equals("Rel:Leader") && faction.getFPlayerAdmin().equals(fplayer)) ||
                    (f.equals("Rel:Officer") && faction.getFPlayersWhereRole(Role.MODERATOR).contains(fplayer)) ||
                    (f.equals("Rel:Member") && rel.equals(Relation.MEMBER)) ||
                    (f.equals("Rel:Recruit") && rel.equals(Relation.MEMBER)) ||
                    (f.equals("Rel:Ally") && rel.equals(Relation.ALLY)) ||
                    (f.equals("Rel:Truce") && rel.equals(Relation.ALLY)) ||
                    (f.equals("Rel:Neutral") && rel.equals(Relation.NEUTRAL)) ||
                    (f.equals("Rel:Enemy") && rel.equals(Relation.ENEMY)) ||
                    (f.equalsIgnoreCase(faction.getTag())) ||
                    (f.equalsIgnoreCase(faction.getTag().substring(2)))) {
                return false;
            }
        }

        return true;
    }

}
