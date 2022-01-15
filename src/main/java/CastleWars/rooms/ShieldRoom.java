package CastleWars.rooms;

import CastleWars.data.PlayerData;
import mindustry.content.StatusEffects;
import mindustry.gen.Groups;

public class ShieldRoom extends EffectRoom {
    public ShieldRoom(int x, int y, int cost, String name) {
        super(StatusEffects.shielded, x, y, cost, name);
    }

    @Override
    public void buy(PlayerData data) {
        data.money -= cost;
        Groups.unit.each(u -> u.team == data.player.team(), unit -> unit.shield += unit.health);
    }
}
