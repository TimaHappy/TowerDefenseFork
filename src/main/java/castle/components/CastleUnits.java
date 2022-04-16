package castle.components;

import arc.struct.OrderedMap;
import mindustry.content.UnitTypes;
import mindustry.type.UnitType;

public class CastleUnits {

    public static OrderedMap<UnitType, Moneys> units;

    public static void load() {
        units = OrderedMap.of(
                UnitTypes.dagger,   new Moneys(60,    0,  10),
                UnitTypes.mace,     new Moneys(150,   1,  50),
                UnitTypes.fortress, new Moneys(500,   4,  150),
                UnitTypes.scepter,  new Moneys(3000,  20, 750),
                UnitTypes.reign,    new Moneys(10000, 45, 1500),

                UnitTypes.crawler,  new Moneys(75,    0,  10),
                UnitTypes.atrax,    new Moneys(160,   1,  60),
                UnitTypes.spiroct,  new Moneys(500,   4,  150),
                UnitTypes.arkyid,   new Moneys(4600,  24, 750),
                UnitTypes.toxopid,  new Moneys(12500, 50, 1750),

                UnitTypes.nova,     new Moneys(75,    0,  15),
                UnitTypes.pulsar,   new Moneys(160,   1,  50),
                UnitTypes.quasar,   new Moneys(500,   4,  175),
                UnitTypes.vela,     new Moneys(3750,  25, 750),
                UnitTypes.corvus,   new Moneys(15000, 55, 1500),

                UnitTypes.risso,    new Moneys(150,   0,  25),
                UnitTypes.minke,    new Moneys(350,   2,  75),
                UnitTypes.bryde,    new Moneys(1200  ,6,  200),
                UnitTypes.sei,      new Moneys(3750,  22, 800),
                UnitTypes.omura,    new Moneys(15000, 50, 1750),

                UnitTypes.retusa,   new Moneys(200,   1,  25),
                UnitTypes.oxynoe,   new Moneys(750,   3,  80),
                UnitTypes.cyerce,   new Moneys(1600,  8,  200),
                UnitTypes.aegires,  new Moneys(4800,  24, 800),
                UnitTypes.navanax,  new Moneys(11000, 70, 2000));
    }

    public static int drop(UnitType type) {
        return units.containsKey(type) ? units.get(type).drop : -1;
    }

    public record Moneys(int cost, int income, int drop) {}
}
