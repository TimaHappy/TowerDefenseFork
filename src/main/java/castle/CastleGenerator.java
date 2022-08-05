package castle;

import arc.util.Structs;
import castle.components.CastleCosts;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Planets;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.unit.ErekirUnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.distribution.Sorter.SorterBuild;
import mindustry.world.blocks.storage.CoreBlock;

import static castle.CastleLogic.isErekir;
import static castle.CastleLogic.isSerpulo;
import static castle.CastleRooms.*;
import static mindustry.Vars.world;

public class CastleGenerator {

    public static int offsetX, offsetY;

    public static void generate() {
        Tiles saved = world.tiles;
        Tiles tiles = world.resize(world.width(), world.height() * 2 + size * 9 / 2);

        for (int x = 0; x < tiles.width; x++) {
            for (int y = saved.height; y < tiles.height - saved.height; y++) {
                tiles.set(x, y, new Tile(x, y, Blocks.space, Blocks.air, Blocks.air));
            }
        }

        for (int x = 0; x < saved.width; x++) {
            for (int y = 0; y < saved.height; y++) {
                Tile save = saved.getc(x, y);
                tiles.set(x, y, new Tile(x, y, save.floor(), save.overlay() != Blocks.spawn ? save.overlay() : Blocks.air, !save.block().breakable ? save.block() : Blocks.air));
                tiles.set(x, tiles.height - y - 1, new Tile(x, tiles.height - y - 1, save.floor(), save.overlay() != Blocks.spawn ? save.overlay() : Blocks.air, !save.block().breakable ? save.block() : Blocks.air));
            }
        }

        saved.eachTile(tile -> {
            if (!tile.isCenter()) return;

            if (tile.block() instanceof CoreBlock) {
                Block defaultCore = isSerpulo() ? Blocks.coreShard : Blocks.coreBastion;
                Block core = isSerpulo() ? Blocks.coreNucleus : Blocks.coreAcropolis;

                Tile shardedCore = tiles.getc(tile.x, tile.y), blueCore = tiles.getc(tile.x, tiles.height - tile.y - 2 + core.size % 2);

                shardedCore.setNet(defaultCore, Team.sharded, 0);
                blueCore.setNet(defaultCore, Team.blue, 0);

                new BlockRoom(core, Team.sharded, shardedCore.x, shardedCore.y, 5000);
                new BlockRoom(core, Team.blue, blueCore.x, blueCore.y, 5000);
            }

            if (tile.block() instanceof Turret turret) {
                boolean isErekirTurret = Structs.contains(turret.requirements, e -> Planets.serpulo.hiddenItems.contains(e.item));
                if (isErekir() && !isErekirTurret || isSerpulo() && isErekirTurret) return;

                new TurretRoom(turret, Team.sharded, tile.x, tile.y);
                new TurretRoom(turret, Team.blue, tile.x, tiles.height - tile.y - 2 + turret.size % 2);
            }

            if (tile.build instanceof SorterBuild sorterBuild) {
                Item item = sorterBuild.config();
                if (!CastleCosts.items.containsKey(item)) return;

                new MinerRoom(item, Team.sharded, tile.x, tile.y);
                new MinerRoom(item, Team.blue, tile.x, tiles.height - tile.y - 1);
            }

            if (tile.overlay() == Blocks.spawn) {
                shardedSpawns.add(tiles.getc(tile.x, tile.y));
                blueSpawns.add(tiles.getc(tile.x, tiles.height - tile.y - 1));
            }
        });

        generateShop(8, saved.height + 7);
    }

    public static void generateShop(int shopX, int shopY) {
        CastleCosts.units.each((type, money) -> {
            boolean isErekirUnit = type instanceof ErekirUnitType;
            if (isErekir() && !isErekirUnit || isSerpulo() && isErekirUnit) return;

            new UnitRoom(type, UnitRoom.UnitRoomType.attack, money.income(), shopX + offsetX * size, shopY + offsetY * size * 2, money.cost());
            new UnitRoom(type, UnitRoom.UnitRoomType.defend, -money.income(), shopX + offsetX * size, shopY + offsetY * size * 2 + size, money.cost());

            if (++offsetX % 5 != 0) return;
            if (offsetY == 0) {
                offsetX -= 5;
                ++offsetY;
            } else offsetY--;
        });

        CastleCosts.effects.each((effect, cost) -> {
            new EffectRoom(effect, shopX + offsetX * size, shopY + offsetY * size * 2, cost);

            if (++offsetX % 5 != 0) return;
            if (offsetY == 0) {
                offsetX -= 5;
                ++offsetY;
            } else offsetY--;
        });
    }
}
