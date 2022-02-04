package castle;

import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Log;
import castle.CastleRooms.*;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.defense.turrets.Turret;

import static mindustry.Vars.world;

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

        tiles.getc(40, halfHeight / 2).setBlock(Blocks.coreShard, Team.sharded);
        CastleRooms.rooms.add(new CoreRoom(Team.sharded, 38, halfHeight / 2 - 2, 5000));
        tiles.getc(40, height - halfHeight / 2 - 1).setBlock(Blocks.coreShard, Team.blue);
        CastleRooms.rooms.add(new CoreRoom(Team.blue, 38, height - halfHeight / 2 - 3, 5000));

        // TODO чета тут не чисто
        CastleRooms.shardedSpawn = tiles.getc(width - 40, halfHeight / 2);
        CastleRooms.blueSpawn = tiles.getc(width - 40, height - halfHeight / 2 - 1);

        Geometry.circle(CastleRooms.shardedSpawn.x, CastleRooms.shardedSpawn.y, 8, (x, y) -> tiles.getc(x, y).setFloor(Blocks.darksandWater.asFloor()));

        Geometry.circle(CastleRooms.blueSpawn.x, CastleRooms.blueSpawn.y, 8, (x, y) -> tiles.getc(x, y).setFloor(Blocks.darksandWater.asFloor()));

        // Генерируем остальные комнаты

        generateShop();

        CastleRooms.rooms.each(room -> room.spawn(tiles));

        Log.info("Генерация завершена.");
    }

    /**
     * public void generateRooms(Tiles tiles) {
     *
     * if (save.block() == Blocks.laserDrill && (save.overlay() == Blocks.oreCopper || save.overlay() == Blocks.oreTitanium || save.overlay() == Blocks.oreThorium)) {
     * CastleRooms.rooms.add(new MinerRoom(new ItemStack(save.overlay().itemDrop, 48 - save.overlay().itemDrop.hardness * 8), Team.sharded, first.x, first.y, 1000 + save.overlay().itemDrop.hardness * 125));
     * CastleRooms.rooms.add(new MinerRoom(new ItemStack(save.overlay().itemDrop, 48 - save.overlay().itemDrop.hardness * 8), Team.blue, second.x, second.y, 1000 + save.overlay().itemDrop.hardness * 125));
     * }
     * <p>
     * if (save.block() instanceof Turret turret) {
     * CastleRooms.rooms.add(new BlockRoom(turret, Team.sharded, first.x, first.y, getTurretCost(turret)));
     * CastleRooms.rooms.add(new BlockRoom(turret, Team.blue, second.x, second.y, getTurretCost(turret)));
     * }
     * <p>
     * if (save.block() instanceof CommandCenter center) {
     * CastleRooms.rooms.add(new BlockRoom(center, Team.sharded, first.x, first.y, 750));
     * CastleRooms.rooms.add(new BlockRoom(center, Team.blue, second.x, second.y, 750));
     * }
     * <p>
     * if (save.block() instanceof RepairPoint point) {
     * CastleRooms.rooms.add(new BlockRoom(point, Team.sharded, first.x, first.y, point.size * point.size * 300));
     * CastleRooms.rooms.add(new BlockRoom(point, Team.blue, second.x, second.y, point.size * point.size * 300));
     * }
     * <p>
     * if (save.floor() == Blocks.darkPanel2) {
     * CastleRooms.shardedSpawn = first;
     * CastleRooms.blueSpawn = second;
     * }
     * }
     * }
     * }
     * <p>
     * generateShop();
     * <p>
     * CastleRooms.rooms.each(room -> room.spawn(tiles));
     * }
     */

    public void generateShop() {
        int x = 7, y = halfHeight + 2;
        int distance = CastleRooms.size + 2;

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
        addUnitRoom(UnitTypes.oxynoe, 3, x + distance * 11, y + 2, 525);
        addUnitRoom(UnitTypes.cyerce, 9, x + distance * 12, y + 2, 1450);
        addUnitRoom(UnitTypes.aegires, 25, x + distance * 13, y + 2, 5000);
        addUnitRoom(UnitTypes.navanax, 65, x + distance * 14, y + 2, 10000);

        addItemRoom(new ItemStack(Items.copper, 240), x + distance * 10, y + 2 + distance * 2, 100);
        addItemRoom(new ItemStack(Items.silicon, 240), x + distance * 10, y + 2 + distance * 2 + CastleRooms.size + 2, 150);
        addItemRoom(new ItemStack(Items.titanium, 240), x + distance * 11, y + 2 + distance * 2, 200);
        addItemRoom(new ItemStack(Items.pyratite, 120), x + distance * 11, y + 2 + distance * 2 + CastleRooms.size + 2, 200);
        addItemRoom(new ItemStack(Items.plastanium, 120), x + distance * 12, y + 2 + distance * 2, 300);
        addEffectRoom(StatusEffects.overdrive, "Overdrive\neffect", x + distance * 12, y + 2 + distance * 2 + CastleRooms.size + 2, 2500);
        addItemRoom(new ItemStack(Items.phaseFabric, 120), x + distance * 13, y + 2 + distance * 2, 400);
        addEffectRoom(StatusEffects.boss, "Boss\neffect", x + distance * 13, y + 2 + distance * 2 + CastleRooms.size + 2, 5000);
        addItemRoom(new ItemStack(Items.surgeAlloy, 240), x + distance * 14, y + 2 + distance * 2, 500);
        addEffectRoom(StatusEffects.shielded, "Shield\neffect", x + distance * 14, y + 2 + distance * 2 + CastleRooms.size + 2, 7500);
    }

    private void addUnitRoom(UnitType type, int income, int x, int y, int cost) {
        CastleRooms.rooms.add(new UnitRoom(type, UnitRoom.UnitRoomType.attack, income, x, y, cost));
        CastleRooms.rooms.add(new UnitRoom(type, UnitRoom.UnitRoomType.defend, -income, x, y + CastleRooms.size + 2, cost));
    }

    private void addItemRoom(ItemStack stack, int x, int y, int cost) {
        CastleRooms.rooms.add(new ItemRoom(stack, x, y, cost));
    }

    private void addEffectRoom(StatusEffect effect, String label, int x, int y, int cost) {
        CastleRooms.rooms.add(new EffectRoom(effect, label, x, y, cost));
    }

    // ?
    private int getTurretCost(Turret turret) {
        return BlockRoom.turretCosts.get(turret, 1000);
    }
}
