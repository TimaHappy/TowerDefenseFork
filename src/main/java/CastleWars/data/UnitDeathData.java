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
        cost.put(UnitTypes.dagger, 20);
        cost.put(UnitTypes.mace, 50);
        cost.put(UnitTypes.fortress, 150);
        cost.put(UnitTypes.scepter, 750);
        cost.put(UnitTypes.reign, 2500);

        // Ground Support
        cost.put(UnitTypes.nova, 50);
        cost.put(UnitTypes.pulsar, 75);
        cost.put(UnitTypes.quasar, 200);
        cost.put(UnitTypes.vela, 800);
        cost.put(UnitTypes.corvus, 2500);

        // Naval
        cost.put(UnitTypes.risso, 50);
        cost.put(UnitTypes.minke, 100);
        cost.put(UnitTypes.bryde, 300);
        cost.put(UnitTypes.sei, 1000);
        cost.put(UnitTypes.omura, 3000);

        // Spiders
        cost.put(UnitTypes.crawler, 20);
        cost.put(UnitTypes.atrax, 60);
        cost.put(UnitTypes.spiroct, 150);
        cost.put(UnitTypes.arkyid, 900);
        cost.put(UnitTypes.toxopid, 2500);

        // Block. Just a block.
        cost.put(UnitTypes.block, 1);

        Events.on(EventType.UnitDestroyEvent.class, event -> {
            if (cost.containsKey(event.unit.type)) {
                PlayerData.datas.values().forEach(data -> {
                    if (event.unit.team != data.player.team() && !event.unit.spawnedByCore) {
                        data.increaseMoney(get(event.unit.type));
                        Call.label(data.player.con, "[accent]+ [lime]" + get(event.unit.type), 0.5f, event.unit.x, event.unit.y);
                    }
                });
            }
        });
    }

    public static int get(UnitType type) {
        return cost.get(type);
    }
}
