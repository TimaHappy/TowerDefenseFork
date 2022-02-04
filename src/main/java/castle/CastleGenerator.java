package castle;

import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
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

    public static final int width = 360;
    public static final int halfHeight = 80;
    public static final int height = halfHeight * 2 + CastleRooms.size * 4 + 11;

    public static final int borderLength = 3;

    public void run() {
        world.loadGenerator(width, height, this);
    }

    @Override
    public void get(Tiles tiles) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < halfHeight; y++) {
                Block block = Blocks.air;
                Block floor = Blocks.grass;

                if (y < borderLength || y > halfHeight - borderLength - 1 || x < borderLength || x > width - borderLength - 1) {
                    block = Blocks.stoneWall;
                }

                if (y > 25 && y < halfHeight - 25) {
                    floor = Blocks.sandWater;
                }

                if (y == borderLength || y == halfHeight - borderLength - 1) {
                    floor = Blocks.cryofluid;
                }

                if (block == Blocks.air && floor == Blocks.grass && Mathf.chance(0.01f)) {
                    block = Blocks.pine;
                }

                tiles.set(x, y, new Tile(x, y, floor, Blocks.air, block));
                tiles.set(x, height - y - 1, new Tile(x, height - y - 1, floor, Blocks.air, block));
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = halfHeight; y < height - halfHeight; y++) {
                tiles.set(x, y, new Tile(x, y, Blocks.space, Blocks.air, Blocks.air));
            }
        }

        // Генерируем ядра и точки появления врагов
        Tile shardedCoreTile = tiles.getc(40, halfHeight / 2);
        shardedCoreTile.setBlock(Blocks.coreShard, Team.sharded);
        CastleRooms.rooms.add(new CoreRoom(Team.sharded, shardedCoreTile.x - 2, shardedCoreTile.y - 2, 5000));
        CastleRooms.shardedSpawn = tiles.getc(width - shardedCoreTile.x, shardedCoreTile.y);

        Tile blueCoreTile = tiles.getc(40, height - halfHeight / 2 - 1);
        blueCoreTile.setBlock(Blocks.coreShard, Team.blue);
        CastleRooms.rooms.add(new CoreRoom(Team.blue, blueCoreTile.x - 2, blueCoreTile.y - 2, 5000));
        CastleRooms.blueSpawn = tiles.getc(width - blueCoreTile.x, blueCoreTile.y);

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
        Point2 first = new Point2(7, 26);
        Point2 second = new Point2(15, height - 31);
        int distance = 8;

        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.copper, 48 - Items.copper.hardness * 8), Team.sharded, first.x, first.y, 750 + Items.copper.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.copper, 48 - Items.copper.hardness * 8), Team.blue, first.x, second.y, 750 + Items.copper.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.copper, 48 - Items.copper.hardness * 8), Team.sharded, second.x, first.y, 750 + Items.copper.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.copper, 48 - Items.copper.hardness * 8), Team.blue, second.x, second.y, 750 + Items.copper.hardness * 125));

        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.lead, 48 - Items.lead.hardness * 8), Team.sharded, first.x, first.y + distance, 750 + Items.lead.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.lead, 48 - Items.lead.hardness * 8), Team.blue, first.x, second.y - distance, 750 + Items.lead.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.lead, 48 - Items.lead.hardness * 8), Team.sharded, second.x, first.y + distance, 750 + Items.lead.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.lead, 48 - Items.lead.hardness * 8), Team.blue, second.x, second.y - distance, 750 + Items.lead.hardness * 125));

        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.titanium, 48 - Items.titanium.hardness * 8), Team.sharded, first.x, first.y + distance * 2, 750 + Items.titanium.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.titanium, 48 - Items.titanium.hardness * 8), Team.blue, first.x, second.y - distance * 2, 750 + Items.titanium.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.titanium, 48 - Items.titanium.hardness * 8), Team.sharded, second.x, first.y + distance * 2, 750 + Items.titanium.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.titanium, 48 - Items.titanium.hardness * 8), Team.blue, second.x, second.y - distance * 2, 750 + Items.titanium.hardness * 125));

        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.thorium, 48 - Items.thorium.hardness * 8), Team.sharded, first.x, first.y + distance * 3, 750 + Items.thorium.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.thorium, 48 - Items.thorium.hardness * 8), Team.blue, first.x, second.y - distance * 3, 750 + Items.thorium.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.thorium, 48 - Items.thorium.hardness * 8), Team.sharded, second.x, first.y + distance * 3, 750 + Items.thorium.hardness * 125));
        CastleRooms.rooms.add(new MinerRoom(new ItemStack(Items.thorium, 48 - Items.thorium.hardness * 8), Team.blue, second.x, second.y - distance * 3, 750 + Items.thorium.hardness * 125));
    }

    public void generateTurrets() {
        Point2 first = new Point2(38, 26);
        Point2 second = new Point2(38, height - 31);
        int horizontalDistance = 24;
        int verticalDistance = 23;

        CastleRooms.rooms.add(new BlockRoom(Blocks.commandCenter, Team.sharded, first.x, first.y, 750));
        CastleRooms.rooms.add(new BlockRoom(Blocks.commandCenter, Team.blue, second.x, second.y, 750));

        CastleRooms.rooms.add(new BlockRoom(Blocks.repairTurret, Team.sharded, first.x, first.y + verticalDistance, 1200));
        CastleRooms.rooms.add(new BlockRoom(Blocks.repairTurret, Team.blue, second.x, second.y - verticalDistance, 1200));
    }


    /**
     * if (save.block() instanceof Turret turret) {
     * CastleRooms.rooms.add(new BlockRoom(turret, Team.sharded, first.x, first.y, getTurretCost(turret)));
     * CastleRooms.rooms.add(new BlockRoom(turret, Team.blue, second.x, second.y, getTurretCost(turret)));
     *
     * addEffectRoom(StatusEffects.overdrive, "Overdrive\neffect", x + distance * 12, y + 2 + distance * 2 + CastleRooms.size + 2, 2500);
     * addEffectRoom(StatusEffects.boss, "Boss\neffect", x + distance * 13, y + 2 + distance * 2 + CastleRooms.size + 2, 5000);
     * addEffectRoom(StatusEffects.shielded, "Shield\neffect", x + distance * 14, y + 2 + distance * 2 + CastleRooms.size + 2, 7500);
     */

    public void generateShop() {
        int x = 7, y = halfHeight + 2, distance = CastleRooms.size + 2;

        addUnitRoom(UnitTypes.dagger, 0, x, y + 2, 100);
        addUnitRoom(UnitTypes.mace, 1, x + distance, y + 2, 150);
        addUnitRoom(UnitTypes.fortress, 4, x + distance * 2, y + 2, 525);
        addUnitRoom(UnitTypes.scepter, 20, x + distance * 3, y + 2, 3000);
        addUnitRoom(UnitTypes.reign, 55, x + distance * 4, y + 2, 8000);

        addUnitRoom(UnitTypes.crawler, 0, x, y + 2 + distance * 2, 70);
        addUnitRoom(UnitTypes.atrax, 1, x + distance, y + 2 + distance * 2, 175);
        addUnitRoom(UnitTypes.spiroct, 4, x + distance * 2, y + 2 + distance * 2, 500);
        addUnitRoom(UnitTypes.arkyid, 24, x + distance * 3, y + 2 + distance * 2, 5000);
        addUnitRoom(UnitTypes.toxopid, 60, x + distance * 4, y + 2 + distance * 2, 9000);

        addUnitRoom(UnitTypes.nova, 0, x + distance * 5, y + 2, 100);
        addUnitRoom(UnitTypes.pulsar, 1, x + distance * 6, y + 2, 175);
        addUnitRoom(UnitTypes.quasar, 4, x + distance * 7, y + 2, 500);
        addUnitRoom(UnitTypes.vela, 22, x + distance * 8, y + 2, 3500);
        addUnitRoom(UnitTypes.corvus, 65, x + distance * 9, y + 2, 8500);

        addUnitRoom(UnitTypes.risso, 1, x + distance * 5, y + 2 + distance * 2, 150);
        addUnitRoom(UnitTypes.minke, 2, x + distance * 6, y + 2 + distance * 2, 350);
        addUnitRoom(UnitTypes.bryde, 8, x + distance * 7, y + 2 + distance * 2, 1200);
        addUnitRoom(UnitTypes.sei, 24, x + distance * 8, y + 2 + distance * 2, 3750);
        addUnitRoom(UnitTypes.omura, 65, x + distance * 9, y + 2 + distance * 2, 10000);

        addUnitRoom(UnitTypes.retusa, 1, x + distance * 10, y + 2, 200);
        addUnitRoom(UnitTypes.oxynoe, 3, x + distance * 11, y + 2, 500);
        addUnitRoom(UnitTypes.cyerce, 9, x + distance * 12, y + 2, 1400);
        addUnitRoom(UnitTypes.aegires, 25, x + distance * 13, y + 2, 5000);
        addUnitRoom(UnitTypes.navanax, 65, x + distance * 14, y + 2, 10000);

        addUnitRoom(UnitTypes.flare, 0, x + distance * 10, y + 2 + distance * 2, 100);
        addUnitRoom(UnitTypes.horizon, 1, x + distance * 11, y + 2 + distance * 2, 250);
        addUnitRoom(UnitTypes.zenith, 5, x + distance * 12, y + 2 + distance * 2, 1000);
        addUnitRoom(UnitTypes.antumbra, 25, x + distance * 13, y + 2 + distance * 2, 4000);
        addUnitRoom(UnitTypes.eclipse, 55, x + distance * 14, y + 2 + distance * 2, 10000);
    }

    private void addUnitRoom(UnitType type, int income, int x, int y, int cost) {
        CastleRooms.rooms.add(new UnitRoom(type, UnitRoom.UnitRoomType.attack, income, x, y, cost));
        CastleRooms.rooms.add(new UnitRoom(type, UnitRoom.UnitRoomType.defend, -income, x, y + CastleRooms.size + 2, cost));
    }

    //private void addEffectRoom(StatusEffect effect, String label, int x, int y, int cost) {
    //    CastleRooms.rooms.add(new EffectRoom(effect, label, x, y, cost));
    //}

    //private int getTurretCost(Turret turret) {
    //    return BlockRoom.turretCosts.get(turret, 1000);
    //}
}
