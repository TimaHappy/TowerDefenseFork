package CastleWars.logic;

import CastleWars.data.Icon;
import CastleWars.data.PlayerData;
import mindustry.gen.Groups;
import mindustry.type.StatusEffect;

public class EffectRoom extends Room{

    StatusEffect effect;

    public EffectRoom(StatusEffect effect, int x, int y, int cost) {
        super(x, y, cost, 4);
        this.effect = effect;
        label = Icon.get(effect) + " [white]: [gray]" + cost;
    }

    @Override
    public void buy(PlayerData data) {
        data.money -= cost;
        Groups.unit.each(u -> u.team == data.player.team(), unit -> unit.apply(effect, 15));
    }

    @Override
    public void update() {
    }
}
