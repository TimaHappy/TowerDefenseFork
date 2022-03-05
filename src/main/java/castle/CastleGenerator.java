package castle;

import arc.func.Cons;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Reflect;
import castle.CastleRooms.*;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.maps.Map;
import mindustry.maps.filters.GenerateFilter.GenerateInput;
import mindustry.maps.filters.NoiseFilter;
import mindustry.maps.filters.ScatterFilter;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class CastleGenerator implements Cons<Tiles> {

    public static Mode mode;

    // Все переменные для генерации
    public static final int worldWidth = 360;
    public static final int halfHeight = 80;
    public static final int worldHeight = halfHeight * 2 + CastleRooms.size * 4 + 11;

    public static final int border = 3, waterIndent = 25;

    public static final int drillX = 6, shardedDrillY = 26, blueDrillY = worldHeight - 31;

    public static final int supplyX = 26, shardedSupplyY = 26, blueSupplyY = worldHeight - 31;

    public static final int turretX = 38, shardedTurretY = 27, blueTurretY = worldHeight - 31;

    public static final int shopX = 7, shopY = halfHeight + 2;

    public static final int coreX = 40, shardedCoreY = halfHeight / 2, blueCoreY = worldHeight - halfHeight / 2 - 1;

    public void run() {
        world.loadGenerator(worldWidth, worldHeight, this);
    }

    @Override
    public void get(Tiles tiles) {
        mode = Seq.with(Mode.values()).random(mode);

        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < halfHeight; y++) {
                Block block = Blocks.air;
                Block floor = mode.floor;

                if (y < border || y > halfHeight - border - 1 || x < border || x > worldWidth - border - 1) {
                    block = mode.wall;
                }

                if (y > waterIndent && y < halfHeight - waterIndent) {
                    floor = mode.water;
                }

                tiles.set(x, y, new Tile(x, y, floor, Blocks.air, block));
                tiles.set(x, worldHeight - y - 1, new Tile(x, worldHeight - y - 1, floor, Blocks.air, block));
            }
        }

        for (int x = 0; x < worldWidth; x++) {
            for (int y = halfHeight; y < worldHeight - halfHeight; y++) {
                tiles.set(x, y, new Tile(x, y, Blocks.space, Blocks.air, Blocks.air));
            }
        }

        GenerateInput in = new GenerateInput();

        NoiseFilter noise = new NoiseFilter();
        Reflect.set(noise, "target", mode.floor);
        Reflect.set(noise, "floor", mode.floor2);
        Reflect.set(noise, "block", mode.wall2);

        noise.randomize();
        in.begin(worldWidth, worldHeight, tiles::getc);
        noise.apply(tiles, in);

        ScatterFilter scatter = new ScatterFilter(), scatter2 = new ScatterFilter();
        Reflect.set(scatter, "chance", 0.02f);
        Reflect.set(scatter, "flooronto", mode.floor);
        Reflect.set(scatter, "floor", mode.floor);
        Reflect.set(scatter, "block", mode.floor.asFloor().decoration);
        Reflect.set(scatter2, "chance", 0.02f);
        Reflect.set(scatter2, "flooronto", mode.floor2);
        Reflect.set(scatter2, "floor", mode.floor2);
        Reflect.set(scatter2, "block", mode.floor2.asFloor().decoration);

        scatter.randomize();
        scatter2.randomize();
        in.begin(worldWidth, worldHeight, tiles::getc);
        scatter.apply(tiles, in);
        scatter2.apply(tiles, in);

        if (mode == Mode.snow) {
            ScatterFilter scatter3 = new ScatterFilter();
            Reflect.set(scatter3, "chance", 0.002f);
            Reflect.set(scatter3, "flooronto", mode.floor);
            Reflect.set(scatter3, "floor", mode.floor);
            Reflect.set(scatter3, "block", Blocks.whiteTreeDead);

            scatter3.randomize();
            in.begin(worldWidth, worldHeight, tiles::getc);
            scatter3.apply(tiles, in);
        }

        // Генерируем ядра и точки появления врагов
        Tile shardedCoreTile = tiles.getc(coreX, shardedCoreY);
        shardedCoreTile.setBlock(Blocks.coreShard, Team.sharded);
        CastleRooms.rooms.add(new CoreRoom(Team.sharded, shardedCoreTile.x - 2, shardedCoreTile.y - 2, 5000));
        CastleRooms.shardedSpawn = tiles.getc(worldWidth - shardedCoreTile.x, shardedCoreTile.y);

        Tile blueCoreTile = tiles.getc(coreX, blueCoreY);
        blueCoreTile.setBlock(Blocks.coreShard, Team.blue);
        CastleRooms.rooms.add(new CoreRoom(Team.blue, blueCoreTile.x - 2, blueCoreTile.y - 2, 5000));
        CastleRooms.blueSpawn = tiles.getc(worldWidth - blueCoreTile.x, blueCoreTile.y);

        // Спавним буры для добычи ресурсов
        generateDrills();

        // Спавним комнаты с турелями
        generateTurrets();

        // Спавним комнаты с всяким мусором
        generateSupply();

        // Генерируем магазин с юнитами
        generateShop();

        // Спавним комнаты и круги вокруг точек появления
        CastleRooms.rooms.each(room -> room.spawn(tiles));
        Geometry.circle(CastleRooms.shardedSpawn.x, CastleRooms.shardedSpawn.y, 8, (x, y) -> tiles.getc(x, y).setOverlay(Blocks.tendrils));
        Geometry.circle(CastleRooms.blueSpawn.x, CastleRooms.blueSpawn.y, 8, (x, y) -> tiles.getc(x, y).setOverlay(Blocks.tendrils));

        state.map = new Map(StringMap.of("name", "The Castle", "author", "[blue]Darkness", "description", "A map for Castle Wars gamemode."));

        Log.info("Генерация завершена.");
    }

    public void generateDrills() {
        int distance = 8;

        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.copper, 48 - Items.copper.hardness * 8), Team.sharded, drillX, shardedDrillY, 250 + Items.copper.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.copper, 48 - Items.copper.hardness * 8), Team.blue, drillX, blueDrillY, 250 + Items.copper.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.copper, 48 - Items.copper.hardness * 8), Team.sharded, drillX + distance, shardedDrillY, 250 + Items.copper.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.copper, 48 - Items.copper.hardness * 8), Team.blue, drillX + distance, blueDrillY, 250 + Items.copper.hardness * 250));

        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.lead, 48 - Items.lead.hardness * 8), Team.sharded, drillX, shardedDrillY + distance, 250 + Items.lead.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.lead, 48 - Items.lead.hardness * 8), Team.blue, drillX, blueDrillY - distance, 250 + Items.lead.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.lead, 48 - Items.lead.hardness * 8), Team.sharded, drillX + distance, shardedDrillY + distance, 250 + Items.lead.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.lead, 48 - Items.lead.hardness * 8), Team.blue, drillX + distance, blueDrillY - distance, 250 + Items.lead.hardness * 250));

        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.titanium, 48 - Items.titanium.hardness * 8), Team.sharded, drillX, shardedDrillY + distance * 2, 250 + Items.titanium.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.titanium, 48 - Items.titanium.hardness * 8), Team.blue, drillX, blueDrillY - distance * 2, 250 + Items.titanium.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.titanium, 48 - Items.titanium.hardness * 8), Team.sharded, drillX + distance, shardedDrillY + distance * 2, 250 + Items.titanium.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.titanium, 48 - Items.titanium.hardness * 8), Team.blue, drillX + distance, blueDrillY - distance * 2, 250 + Items.titanium.hardness * 250));

        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.thorium, 48 - Items.thorium.hardness * 8), Team.sharded, drillX, shardedDrillY + distance * 3, 250 + Items.thorium.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.thorium, 48 - Items.thorium.hardness * 8), Team.blue, drillX, blueDrillY - distance * 3, 250 + Items.thorium.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.thorium, 48 - Items.thorium.hardness * 8), Team.sharded, drillX + distance, shardedDrillY + distance * 3, 250 + Items.thorium.hardness * 250));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.thorium, 48 - Items.thorium.hardness * 8), Team.blue, drillX + distance, blueDrillY - distance * 3, 250 + Items.thorium.hardness * 250));
    }

    public void generateTurrets() {
        int distance = 23;

        CastleRooms.rooms.add(new BlockRoom(Blocks.commandCenter, Team.sharded, turretX, shardedTurretY, 750));
        CastleRooms.rooms.add(new BlockRoom(Blocks.commandCenter, Team.blue, turretX, blueTurretY, 750));

        CastleRooms.rooms.add(new BlockRoom(Blocks.repairTurret, Team.sharded, turretX, shardedTurretY + distance, 1200));
        CastleRooms.rooms.add(new BlockRoom(Blocks.repairTurret, Team.blue, turretX, blueTurretY - distance, 1200));

        CastleRooms.rooms.add(new BlockRoom(Blocks.foreshadow, Team.sharded, turretX, shardedTurretY - 8, 4000));
        CastleRooms.rooms.add(new BlockRoom(Blocks.foreshadow, Team.blue, turretX, blueTurretY + 6, 4000));

        CastleRooms.rooms.add(new BlockRoom(Blocks.spectre, Team.sharded, turretX, shardedTurretY + distance + 6, 3000));
        CastleRooms.rooms.add(new BlockRoom(Blocks.spectre, Team.blue, turretX, blueTurretY - distance - 8, 3000));

        CastleRooms.rooms.add(new BlockRoom(Blocks.scatter, Team.sharded, turretX + 18, shardedTurretY, 250));
        CastleRooms.rooms.add(new BlockRoom(Blocks.scatter, Team.blue, turretX + 18, blueTurretY, 250));

        CastleRooms.rooms.add(new BlockRoom(Blocks.salvo, Team.sharded, turretX + 18, shardedTurretY + distance, 500));
        CastleRooms.rooms.add(new BlockRoom(Blocks.salvo, Team.blue, turretX + 18, blueTurretY - distance, 500));

        CastleRooms.rooms.add(new BlockRoom(Blocks.tsunami, Team.sharded, turretX + 18, shardedTurretY - 7, 850));
        CastleRooms.rooms.add(new BlockRoom(Blocks.tsunami, Team.blue, turretX + 18, blueTurretY + 6, 850));

        CastleRooms.rooms.add(new BlockRoom(Blocks.fuse, Team.sharded, turretX + 18, shardedTurretY + distance + 6, 1500));
        CastleRooms.rooms.add(new BlockRoom(Blocks.fuse, Team.blue, turretX + 18, blueTurretY - distance - 7, 1500));

        CastleRooms.rooms.add(new BlockRoom(Blocks.lancer, Team.sharded, turretX + 36, shardedTurretY, 350));
        CastleRooms.rooms.add(new BlockRoom(Blocks.lancer, Team.blue, turretX + 36, blueTurretY, 350));

        CastleRooms.rooms.add(new BlockRoom(Blocks.lancer, Team.sharded, turretX + 36, shardedTurretY + distance, 350));
        CastleRooms.rooms.add(new BlockRoom(Blocks.lancer, Team.blue, turretX + 36, blueTurretY - distance, 350));

        CastleRooms.rooms.add(new BlockRoom(Blocks.ripple, Team.sharded, turretX + 36, shardedTurretY - 7, 1500));
        CastleRooms.rooms.add(new BlockRoom(Blocks.ripple, Team.blue, turretX + 36, blueTurretY + 6, 1500));

        CastleRooms.rooms.add(new BlockRoom(Blocks.cyclone, Team.sharded, turretX + 36, shardedTurretY + distance + 6, 1750));
        CastleRooms.rooms.add(new BlockRoom(Blocks.cyclone, Team.blue, turretX + 36, blueTurretY - distance - 7, 1750));
    }

    public void generateSupply() {
        int distance = 24;

        CastleRooms.rooms.add(new EffectRoom(StatusEffects.overdrive, Team.sharded, supplyX, shardedSupplyY, 2500));
        CastleRooms.rooms.add(new EffectRoom(StatusEffects.overdrive, Team.blue, supplyX, blueSupplyY, 2500));

        CastleRooms.rooms.add(new EffectRoom(StatusEffects.boss, Team.sharded, supplyX, shardedSupplyY + distance, 7500));
        CastleRooms.rooms.add(new EffectRoom(StatusEffects.boss, Team.blue, supplyX, blueSupplyY - distance, 7500));
    }

    public void generateShop() {
        int distance = CastleRooms.size + 2;

        addUnitRoom(UnitTypes.dagger, 0, shopX, shopY + 2, 100);
        addUnitRoom(UnitTypes.mace, 1, shopX + distance, shopY + 2, 150);
        addUnitRoom(UnitTypes.fortress, 4, shopX + distance * 2, shopY + 2, 525);
        addUnitRoom(UnitTypes.scepter, 20, shopX + distance * 3, shopY + 2, 3000);
        addUnitRoom(UnitTypes.reign, 55, shopX + distance * 4, shopY + 2, 8000);

        addUnitRoom(UnitTypes.crawler, 0, shopX, shopY + 2 + distance * 2, 70);
        addUnitRoom(UnitTypes.atrax, 1, shopX + distance, shopY + 2 + distance * 2, 175);
        addUnitRoom(UnitTypes.spiroct, 4, shopX + distance * 2, shopY + 2 + distance * 2, 500);
        addUnitRoom(UnitTypes.arkyid, 24, shopX + distance * 3, shopY + 2 + distance * 2, 5000);
        addUnitRoom(UnitTypes.toxopid, 60, shopX + distance * 4, shopY + 2 + distance * 2, 9000);

        addUnitRoom(UnitTypes.nova, 0, shopX + distance * 5, shopY + 2, 100);
        addUnitRoom(UnitTypes.pulsar, 1, shopX + distance * 6, shopY + 2, 175);
        addUnitRoom(UnitTypes.quasar, 4, shopX + distance * 7, shopY + 2, 500);
        addUnitRoom(UnitTypes.vela, 22, shopX + distance * 8, shopY + 2, 3500);
        addUnitRoom(UnitTypes.corvus, 65, shopX + distance * 9, shopY + 2, 8500);

        addUnitRoom(UnitTypes.risso, 1, shopX + distance * 5, shopY + 2 + distance * 2, 150);
        addUnitRoom(UnitTypes.minke, 2, shopX + distance * 6, shopY + 2 + distance * 2, 350);
        addUnitRoom(UnitTypes.bryde, 8, shopX + distance * 7, shopY + 2 + distance * 2, 1200);
        addUnitRoom(UnitTypes.sei, 24, shopX + distance * 8, shopY + 2 + distance * 2, 3750);
        addUnitRoom(UnitTypes.omura, 65, shopX + distance * 9, shopY + 2 + distance * 2, 10000);

        addUnitRoom(UnitTypes.retusa, 1, shopX + distance * 10, shopY + 2, 200);
        addUnitRoom(UnitTypes.oxynoe, 5, shopX + distance * 11, shopY + 2, 800);
        addUnitRoom(UnitTypes.cyerce, 12, shopX + distance * 12, shopY + 2, 1800);
        addUnitRoom(UnitTypes.aegires, 25, shopX + distance * 13, shopY + 2, 5000);
        addUnitRoom(UnitTypes.navanax, 70, shopX + distance * 14, shopY + 2, 12000);

        addUnitRoom(UnitTypes.flare, 0, shopX + distance * 10, shopY + 2 + distance * 2, 100);
        addUnitRoom(UnitTypes.horizon, 1, shopX + distance * 11, shopY + 2 + distance * 2, 250);
        addUnitRoom(UnitTypes.zenith, 5, shopX + distance * 12, shopY + 2 + distance * 2, 1000);
        addUnitRoom(UnitTypes.antumbra, 25, shopX + distance * 13, shopY + 2 + distance * 2, 4000);
        addUnitRoom(UnitTypes.eclipse, 55, shopX + distance * 14, shopY + 2 + distance * 2, 10000);
    }

    private void addUnitRoom(UnitType type, int income, int x, int y, int cost) {
        CastleRooms.rooms.add(new UnitRoom(type, UnitRoom.UnitRoomType.attack, income, x, y, cost));
        CastleRooms.rooms.add(new UnitRoom(type, UnitRoom.UnitRoomType.defend, -income, x, y + CastleRooms.size + 2, cost));
    }

    public enum Mode {
        grass(Blocks.grass, Blocks.dacite, Blocks.water, Blocks.shrubs, Blocks.daciteWall),
        sand(Blocks.sand, Blocks.darksand, Blocks.sandWater, Blocks.sandWall, Blocks.duneWall),
        darksand(Blocks.darksand, Blocks.shale, Blocks.darksandWater, Blocks.duneWall, Blocks.darkMetal),
        snow(Blocks.snow, Blocks.ice, Blocks.sandWater, Blocks.snowPine, Blocks.iceWall);

        public Block floor, floor2, water, wall, wall2;

        Mode(Block floor, Block floor2, Block water, Block wall, Block wall2) {
            this.floor = floor;
            this.floor2 = floor2;
            this.water = water;
            this.wall = wall;
            this.wall2 = wall2;
        }
    }
}
