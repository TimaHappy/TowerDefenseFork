package CastleWars.logic;

import CastleWars.data.Icon;
import CastleWars.data.PlayerData;
import arc.util.Interval;
import mindustry.gen.Groups;
import mindustry.type.StatusEffect;

public class EffectRoom extends Room{

    StatusEffect effect;
    String name;
    Interval interval = new Interval();

    public EffectRoom(StatusEffect effect, int x, int y, int cost, String name) {
        super(x, y, cost, 4);
        this.effect = effect;
        this.name = name;
        label = "[accent]" + name + "[white]\n" + Icon.get(effect) + " [white]: [gray]" + cost;
    }

    @Override
    public void buy(PlayerData data) {
        data.money -= cost;
        Groups.unit.each(u -> u.team == data.player.team(), unit -> unit.apply(effect));
    }

    @Override
    public boolean canBuy(PlayerData data) {
        return data.money >= cost() && interval.get(60 * 15f);
    }

    @Override
    public void update() {}
}
