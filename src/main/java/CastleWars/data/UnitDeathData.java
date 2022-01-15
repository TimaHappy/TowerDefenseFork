package CastleWars.data;

import arc.struct.ObjectMap;
import mindustry.content.UnitTypes;
import mindustry.type.UnitType;

public class UnitDeathData {

    public static final ObjectMap<UnitType, Integer> costs = new ObjectMap<>();

    public static void load() {
        // Ground
        costs.put(UnitTypes.dagger, 10);
        costs.put(UnitTypes.mace, 50);

        costs.put(UnitTypes.fortress, 150);
        costs.put(UnitTypes.scepter, 750);
        costs.put(UnitTypes.reign, 1500);

        // Ground Support
        costs.put(UnitTypes.nova, 15);
        costs.put(UnitTypes.pulsar, 50);
        costs.put(UnitTypes.quasar, 175);
        costs.put(UnitTypes.vela, 750);
        costs.put(UnitTypes.corvus, 1500);

        // Naval
        costs.put(UnitTypes.risso, 25);
        costs.put(UnitTypes.minke, 75);
        costs.put(UnitTypes.bryde, 200);
        costs.put(UnitTypes.sei, 800);
        costs.put(UnitTypes.omura, 1750);

        // Naval Support
        costs.put(UnitTypes.retusa, 25);
        costs.put(UnitTypes.oxynoe, 80);
        costs.put(UnitTypes.cyerce, 200);
        costs.put(UnitTypes.aegires, 800);
        costs.put(UnitTypes.navanax, 2000);

        // Spiders
        costs.put(UnitTypes.crawler, 10);
        costs.put(UnitTypes.atrax, 60);
        costs.put(UnitTypes.spiroct, 150);
        costs.put(UnitTypes.arkyid, 750);
        costs.put(UnitTypes.toxopid, 1750);

        costs.put(UnitTypes.block, 1);
    }

    public static int get(UnitType type) {
        return costs.get(type, 0);
    }
}
