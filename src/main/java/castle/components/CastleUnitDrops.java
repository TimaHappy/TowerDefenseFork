package castle.components;

import arc.struct.ObjectMap;
import mindustry.content.UnitTypes;
import mindustry.type.UnitType;

public class CastleUnitDrops {

    public static ObjectMap<UnitType, Integer> costs;

    public static void load() {
        costs = ObjectMap.of(
                UnitTypes.dagger, 10,
                UnitTypes.mace, 50,
                UnitTypes.fortress, 150,
                UnitTypes.scepter, 750,
                UnitTypes.reign, 1500,

                UnitTypes.nova, 15,
                UnitTypes.pulsar, 50,
                UnitTypes.quasar, 175,
                UnitTypes.vela, 750,
                UnitTypes.corvus, 1500,

                UnitTypes.risso, 25,
                UnitTypes.minke, 75,
                UnitTypes.bryde, 200,
                UnitTypes.sei, 800,
                UnitTypes.omura, 1750,

                UnitTypes.retusa, 25,
                UnitTypes.oxynoe, 80,
                UnitTypes.cyerce, 200,
                UnitTypes.aegires, 800,
                UnitTypes.navanax, 2000,

                UnitTypes.crawler, 10,
                UnitTypes.atrax, 60,
                UnitTypes.spiroct, 150,
                UnitTypes.arkyid, 750,
                UnitTypes.toxopid, 1750
        );
    }

    public static int get(UnitType type) {
        return costs.get(type, 0);
    }
}
