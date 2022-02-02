package castle.components;

import arc.struct.ObjectMap;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.ctype.Content;
import mindustry.gen.Iconc;

public class CastleIcons {

    public static ObjectMap<Content, Character> icons;

    public static void load() {
        icons = ObjectMap.of(
                UnitTypes.dagger, Iconc.unitDagger,
                UnitTypes.mace, Iconc.unitMace,
                UnitTypes.fortress, Iconc.unitFortress,
                UnitTypes.scepter, Iconc.unitScepter,
                UnitTypes.reign, Iconc.unitReign,

                UnitTypes.nova, Iconc.unitNova,
                UnitTypes.pulsar, Iconc.unitPulsar,
                UnitTypes.quasar, Iconc.unitQuasar,
                UnitTypes.vela, Iconc.unitVela,
                UnitTypes.corvus, Iconc.unitCorvus,

                UnitTypes.crawler, Iconc.unitCrawler,
                UnitTypes.atrax, Iconc.unitAtrax,
                UnitTypes.spiroct, Iconc.unitSpiroct,
                UnitTypes.arkyid, Iconc.unitArkyid,
                UnitTypes.toxopid, Iconc.unitToxopid,

                UnitTypes.risso, Iconc.unitRisso,
                UnitTypes.minke, Iconc.unitMinke,
                UnitTypes.bryde, Iconc.unitBryde,
                UnitTypes.sei, Iconc.unitSei,
                UnitTypes.omura, Iconc.unitOmura,

                UnitTypes.retusa, Iconc.unitRetusa,
                UnitTypes.oxynoe, Iconc.unitOxynoe,
                UnitTypes.cyerce, Iconc.unitCyerce,
                UnitTypes.aegires, Iconc.unitAegires,
                UnitTypes.navanax, Iconc.unitNavanax,

                Blocks.coreNucleus, Iconc.blockCoreNucleus,
                Blocks.coreFoundation, Iconc.blockCoreFoundation,
                Blocks.coreShard, Iconc.blockCoreShard,

                Blocks.commandCenter, Iconc.blockCommandCenter,
                Blocks.laserDrill, Iconc.blockLaserDrill,

                Blocks.duo, Iconc.blockDuo,
                Blocks.scatter, Iconc.blockScatter,
                Blocks.scorch, Iconc.blockScorch,
                Blocks.hail, Iconc.blockHail,
                Blocks.wave, Iconc.blockWave,
                Blocks.lancer, Iconc.blockLancer,
                Blocks.arc, Iconc.blockArc,
                Blocks.parallax, Iconc.blockParallax,
                Blocks.swarmer, Iconc.blockSwarmer,
                Blocks.salvo, Iconc.blockSalvo,
                Blocks.segment, Iconc.blockSegment,
                Blocks.tsunami, Iconc.blockTsunami,
                Blocks.fuse, Iconc.blockFuse,
                Blocks.ripple, Iconc.blockRipple,
                Blocks.cyclone, Iconc.blockCyclone,
                Blocks.foreshadow, Iconc.blockForeshadow,
                Blocks.spectre, Iconc.blockSpectre,
                Blocks.meltdown, Iconc.blockMeltdown,

                Blocks.repairPoint, Iconc.blockRepairPoint,
                Blocks.repairTurret, Iconc.blockRepairTurret,

                Items.copper, Iconc.itemCopper,
                Items.lead, Iconc.itemLead,
                Items.metaglass, Iconc.itemMetaglass,
                Items.graphite, Iconc.itemGraphite,
                Items.sand, Iconc.itemSand,
                Items.coal, Iconc.itemCoal,
                Items.titanium, Iconc.itemTitanium,
                Items.thorium, Iconc.itemThorium,
                Items.scrap, Iconc.itemScrap,
                Items.silicon, Iconc.itemSilicon,
                Items.plastanium, Iconc.itemPlastanium,
                Items.phaseFabric, Iconc.itemPhaseFabric,
                Items.surgeAlloy, Iconc.itemSurgeAlloy,
                Items.sporePod, Iconc.itemSporePod,
                Items.blastCompound, Iconc.itemBlastCompound,
                Items.pyratite, Iconc.itemPyratite,

                StatusEffects.shielded, Iconc.statusShielded,
                StatusEffects.boss, Iconc.statusBoss,
                StatusEffects.overdrive, Iconc.statusOverdrive
        );
    }

    public static String get(Content content) {
        return String.valueOf(icons.get(content));
    }
}
