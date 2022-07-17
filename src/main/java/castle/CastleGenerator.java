package castle;

import arc.util.Log;
import castle.components.CastleCosts;
import castle.components.CastleCosts.Moneys;
import mindustry.content.Blocks;
import mindustry.core.GameState.State;
import mindustry.game.Team;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.WorldContext;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.distribution.Sorter.SorterBuild;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.TreeBlock;
import mindustry.world.blocks.storage.CoreBlock;

import static castle.CastleRooms.*;
import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class CastleGenerator {

    public Tiles saved;
    public int offsetX, offsetY;

    public void loadMap(Map map) {
        try {
            SaveIO.load(map.file, new WorldContext() {
                @Override
                public Tile tile(int index) {
                    return world.tiles.geti(index);
                }

                @Override
                public void resize(int width, int height) {
                    world.resize(width, height);
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
                    Tile tile = new Tile(x, y, floorID, overlayID, wallID);
                    world.tiles.set(x, y, tile);
                    return tile;
                }

                @Override
                public boolean isGenerating() {
                    return world.isGenerating();
                }

                @Override
                public void begin() {
                    world.beginMapLoad();
                }

                @Override
                public void end() {
                    saved = world.tiles;
                    generate();
                    world.endMapLoad();
                }
            });

            state.map = map;
        } catch (Throwable e) {
            state.set(State.menu);
            Log.err(e);
        }
    }

    public void generate() {
        Tiles tiles  = world.resize(world.width(), world.height() * 2 + size * 4 + 5);

        for (int x = 0; x < tiles.width; x++) {
            for (int y = saved.height; y < tiles.height - saved.height; y++) {
                tiles.set(x, y, new Tile(x, y, Blocks.space, Blocks.air, Blocks.air));
            }
        }

        for (int x = 0; x < saved.width; x++) {
            for (int y = 0; y < saved.height; y++) {
                Tile save = saved.getc(x, y);
                tiles.set(x, y, new Tile(x, y, save.floor(), save.overlay() != Blocks.spawn ? save.overlay() : Blocks.air, save.block() instanceof Prop || save.block() instanceof TreeBlock ? save.block() : Blocks.air));
                tiles.set(x, tiles.height - y - 1, new Tile(x, tiles.height - y - 1, save.floor(), save.overlay() != Blocks.spawn ? save.overlay() : Blocks.air, save.block() instanceof Prop || save.block() instanceof TreeBlock ? save.block() : Blocks.air));
            }
        }

        for (Tile save : saved) {
            if (!save.isCenter()) continue;

            if (save.block() instanceof CoreBlock block) {
                tiles.getc(save.x, save.y).setNet(block, Team.sharded, 0);
                tiles.getc(save.x, tiles.height - save.y - 1).setNet(block, Team.blue, 0);

                new BlockRoom(Blocks.coreNucleus, Team.sharded, save.x, save.y, 5000);
                new BlockRoom(Blocks.coreNucleus, Team.blue, save.x, tiles.height - save.y - 1, 5000);
            }

            if (save.block() instanceof Turret turret) {
                new TurretRoom(turret, Team.sharded, save.x, save.y);
                new TurretRoom(turret, Team.blue, save.x, tiles.height - save.y - 2 + turret.size % 2);
            }

            if (save.build instanceof SorterBuild sorterBuild) {
                Item item = sorterBuild.config();
                new MinerRoom(item, Team.sharded, save.x, save.y);
                new MinerRoom(item, Team.blue, save.x, tiles.height - save.y - 1);
            }

            if (save.overlay() == Blocks.spawn) {
                shardedSpawn = tiles.getc(save.x, save.y);
                blueSpawn = tiles.getc(save.x, tiles.height - save.y - 1);
            }
        }

        generateShop(8, saved.height + 7);
    }

    public void generateShop(int shopX, int shopY) {
        CastleCosts.units.each((type, money) -> {
            addUnitRoom(type, money, shopX + offsetX * size, shopY + offsetY * size * 2);
            if (++offsetX % 5 != 0) return;
            if (offsetY == 0) {
                offsetX -= 5;
                ++offsetY;
            } else offsetY--;
        });

        CastleCosts.effects.each((effect, cost) -> {
            new EffectRoom(effect, shopX + offsetX * size, shopY + offsetY * size, cost);
            if (++offsetY != 4) return;
            ++offsetX;
            offsetY = 0;
        });
    }

    public void addUnitRoom(UnitType type, Moneys money, int x, int y) {
        new UnitRoom(type, UnitRoom.UnitRoomType.attack, money.income(), x, y, money.cost());
        new UnitRoom(type, UnitRoom.UnitRoomType.defend, -money.income(), x, y + size, money.cost());
    }
}
