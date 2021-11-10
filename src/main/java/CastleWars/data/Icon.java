package CastleWars.data;

import arc.struct.ObjectMap;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.ctype.Content;
import mindustry.gen.Iconc;

public class Icon {

    public static ObjectMap<Content, Character> icons = new ObjectMap<>();

    public static void load() {
        // Ground
        icons.put(UnitTypes.dagger, Iconc.unitDagger);
        icons.put(UnitTypes.mace, Iconc.unitMace);
        icons.put(UnitTypes.fortress, Iconc.unitFortress);
        icons.put(UnitTypes.scepter, Iconc.unitScepter);
        icons.put(UnitTypes.reign, Iconc.unitReign);
        // Ground Support
        icons.put(UnitTypes.nova, Iconc.unitNova);
        icons.put(UnitTypes.pulsar, Iconc.unitPulsar);
        icons.put(UnitTypes.quasar, Iconc.unitQuasar);
        icons.put(UnitTypes.vela, Iconc.unitVela);
        icons.put(UnitTypes.corvus, Iconc.unitCorvus);
        // Spiders
        icons.put(UnitTypes.crawler, Iconc.unitCrawler);
        icons.put(UnitTypes.atrax, Iconc.unitAtrax);
        icons.put(UnitTypes.spiroct, Iconc.unitSpiroct);
        icons.put(UnitTypes.arkyid, Iconc.unitArkyid);
        icons.put(UnitTypes.toxopid, Iconc.unitToxopid);
        // Naval
        icons.put(UnitTypes.risso, Iconc.unitRisso);
        icons.put(UnitTypes.minke, Iconc.unitMinke);
        icons.put(UnitTypes.bryde, Iconc.unitBryde);
        icons.put(UnitTypes.sei, Iconc.unitSei);
        icons.put(UnitTypes.omura, Iconc.unitOmura);
        // Naval Support
        icons.put(UnitTypes.retusa, Iconc.unitRetusa);
        icons.put(UnitTypes.oxynoe, Iconc.unitOxynoe);
        icons.put(UnitTypes.cyerce, Iconc.unitCyerce);
        icons.put(UnitTypes.aegires, Iconc.unitAegires);
        icons.put(UnitTypes.navanax, Iconc.unitNavanax);

        icons.put(Blocks.coreNucleus, Iconc.blockCoreNucleus);
        icons.put(Blocks.coreFoundation, Iconc.blockCoreFoundation);
        icons.put(Blocks.coreShard, Iconc.blockCoreShard);

        icons.put(Blocks.foreshadow, Iconc.blockForeshadow);
        icons.put(Blocks.meltdown, Iconc.blockMeltdown);
        icons.put(Blocks.spectre, Iconc.blockSpectre);
        icons.put(Blocks.cyclone, Iconc.blockCyclone);
        icons.put(Blocks.ripple, Iconc.blockRipple);
        icons.put(Blocks.fuse, Iconc.blockFuse);
        icons.put(Blocks.segment, Iconc.blockSegment);
        icons.put(Blocks.lancer, Iconc.blockLancer);
        icons.put(Blocks.swarmer, Iconc.blockSwarmer);
        icons.put(Blocks.salvo, Iconc.blockSalvo);

        icons.put(Items.surgeAlloy, Iconc.itemSurgeAlloy);
        icons.put(Items.plastanium, Iconc.itemPlastanium);
        icons.put(Items.phaseFabric, Iconc.itemPhaseFabric);
        icons.put(Items.copper, Iconc.itemCopper);
        icons.put(Items.titanium, Iconc.itemTitanium);
        icons.put(Items.silicon, Iconc.itemSilicon);
        icons.put(Items.pyratite, Iconc.itemPyratite);
        icons.put(Items.blastCompound, Iconc.itemBlastCompound);

        icons.put(Blocks.duo, Iconc.defense);
        icons.put(Blocks.commandCenter, Iconc.units);
        icons.put(Blocks.plastaniumCompressor, Iconc.blockPlastaniumCompressor);
        icons.put(Blocks.laserDrill, Iconc.blockLaserDrill);
        icons.put(StatusEffects.boss, Iconc.statusBoss);
        icons.put(StatusEffects.overdrive, Iconc.statusOverdrive);
    }

    public static String get(Content item) {
        if (icons.containsKey(item)) {
            return icons.get(item).toString();
        }
        return "[scarlet]oh no";
    }
}
