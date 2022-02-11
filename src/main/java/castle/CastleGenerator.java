package castle;

import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.StringMap;
import arc.util.Log;
import castle.CastleRooms.*;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.maps.Map;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

import static mindustry.Vars.*;

public class CastleGenerator implements Cons<Tiles> {

    // Все переменные для генерации
    public static final int worldWidth = 360;
    public static final int halfHeight = 80;
    public static final int worldHeight = halfHeight * 2 + CastleRooms.size * 4 + 11;

    public static final int border = 3, waterIndent = 25;

    public static final int drillX = 7, shardedDrillY = 26, blueDrillY = worldHeight - 31;

    public static final int turretX = 38, shardedTurretY = 27, blueTurretY = worldHeight - 31;

    public static final int shopX = 7, shopY = halfHeight + 2;

    public static final int coreX = 40, shardedCoreY = halfHeight / 2, blueCoreY = worldHeight - halfHeight / 2 - 1;

    public static final float pineChance = 0.01f;

    public void run() {
        world.loadGenerator(worldWidth, worldHeight, this);
    }

    @Override
    public void get(Tiles tiles) {
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < halfHeight; y++) {
                Block block = Blocks.air;
                Block floor = Blocks.grass;

                if (y < border || y > halfHeight - border - 1 || x < border || x > worldWidth - border - 1) {
                    block = Blocks.stoneWall;
                }

                if (y > waterIndent && y < halfHeight - waterIndent) {
                    floor = Blocks.sandWater;
                }

                if (y == border || y == halfHeight - border - 1) {
                    floor = Blocks.cryofluid;
                }

                if (block == Blocks.air && floor == Blocks.grass && Mathf.chance(pineChance)) {
                    block = Blocks.pine;
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

        // Генерируем магазин с юнитами
        generateShop();

        // Спавним комнаты и круги вокруг точек появления
        CastleRooms.rooms.each(room -> room.spawn(tiles));

        Geometry.circle(CastleRooms.shardedSpawn.x, CastleRooms.shardedSpawn.y, 8, (x, y) -> tiles.getc(x, y).setFloor(Blocks.darksandWater.asFloor()));
        Geometry.circle(CastleRooms.blueSpawn.x, CastleRooms.blueSpawn.y, 8, (x, y) -> tiles.getc(x, y).setFloor(Blocks.darksandWater.asFloor()));

        state.map = new Map(StringMap.of("name", "The Castle", "author", "[cyan]Darkness", "description", "A map for Castle Wars gamemode. Automatically generated."));

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
        addUnitRoom(UnitTypes.oxynoe, 3, shopX + distance * 11, shopY + 2, 500);
        addUnitRoom(UnitTypes.cyerce, 9, shopX + distance * 12, shopY + 2, 1400);
        addUnitRoom(UnitTypes.aegires, 25, shopX + distance * 13, shopY + 2, 5000);
        addUnitRoom(UnitTypes.navanax, 65, shopX + distance * 14, shopY + 2, 10000);

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
}
