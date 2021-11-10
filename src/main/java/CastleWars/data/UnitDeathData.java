package CastleWars.data;

import arc.Events;
import arc.struct.ObjectMap;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.type.UnitType;

public class UnitDeathData {

    public static ObjectMap<UnitType, Integer> cost;

    public static void init() {
        cost = new ObjectMap<>();

        // Ground
        cost.put(UnitTypes.dagger, 10);
        cost.put(UnitTypes.mace, 50);
        cost.put(UnitTypes.fortress, 150);
        cost.put(UnitTypes.scepter, 750);
        cost.put(UnitTypes.reign, 1500);

        // Ground Support
        cost.put(UnitTypes.nova, 15);
        cost.put(UnitTypes.pulsar, 50);
        cost.put(UnitTypes.quasar, 175);
        cost.put(UnitTypes.vela, 750);
        cost.put(UnitTypes.corvus, 1500);

        // Naval
        cost.put(UnitTypes.risso, 25);
        cost.put(UnitTypes.minke, 75);
        cost.put(UnitTypes.bryde, 200);
        cost.put(UnitTypes.sei, 800);
        cost.put(UnitTypes.omura, 1750);

        // Naval Support
        cost.put(UnitTypes.retusa, 25);
        cost.put(UnitTypes.oxynoe, 80);
        cost.put(UnitTypes.cyerce, 200);
        cost.put(UnitTypes.aegires, 800);
        cost.put(UnitTypes.navanax, 2000);

        // Spiders
        cost.put(UnitTypes.crawler, 10);
        cost.put(UnitTypes.atrax, 60);
        cost.put(UnitTypes.spiroct, 150);
        cost.put(UnitTypes.arkyid, 750);
        cost.put(UnitTypes.toxopid, 1750);

        cost.put(UnitTypes.block, 1);

        Events.on(EventType.UnitDestroyEvent.class, event -> {
            if (cost.containsKey(event.unit.type)) {
                PlayerData.datas.values().forEach(data -> {
                    if (event.unit.team != data.player.team() && !event.unit.spawnedByCore) {
                        data.money += get(event.unit.type);
                        Call.label(data.player.con, "[lime]+ [accent]" + get(event.unit.type), 0.4f, event.unit.x, event.unit.y);
                    }
                });
            }
        });
    }

    public static int get(UnitType type) {
        return cost.get(type);
    }
}
