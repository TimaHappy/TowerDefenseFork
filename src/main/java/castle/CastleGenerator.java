package castle;

import arc.func.Cons;
import arc.math.Mathf;
import arc.util.Log;
import castle.components.CastleUnits;
import castle.components.CastleUnits.Moneys;
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
import mindustry.world.blocks.distribution.Sorter.SorterBuild;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.TreeBlock;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.state;
import static mindustry.Vars.world;
import static castle.CastleRooms.*;

public class CastleGenerator implements Cons<Tiles> {

    public Tiles saved;

    // variables for generating shop
    private boolean top;
    private int offset;
    
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
                    get(world.tiles);
                    world.endMapLoad();
                }
            });

            state.map = map;
        } catch (Throwable e) {
            state.set(State.menu);
            Log.err(e);
        }
    }

    @Override
    public void get(Tiles tiles) {
        saved = tiles;

        tiles = world.resize(world.width(), world.height() * 2 + size * 4 + 5);

        for (int x = 0; x < tiles.width; x++) {
            for (int y = 0; y < tiles.height; y++) {
                tiles.set(x, y, new Tile(x, y, Blocks.space, Blocks.air, Blocks.air));
            }
        }

        for (int x = 0; x < tiles.width; x++) {
            for (int y = 0; y < saved.height; y++) {
                Tile save = saved.getc(x, y);
                tiles.set(x, y, new Tile(x, y, save.floor(), save.overlay() != Blocks.spawn ? save.overlay() : Blocks.air, save.block() instanceof Prop || save.block() instanceof TreeBlock ? save.block() : Blocks.air));
                tiles.set(x, tiles.height - y - 1, new Tile(x, tiles.height - y - 1, save.floor(), save.overlay() != Blocks.spawn ? save.overlay() : Blocks.air, save.block() instanceof Prop || save.block() instanceof TreeBlock ? save.block() : Blocks.air));
            }
        }

        for (Tile save : saved) {
            if (save.isCenter()) {
                if (save.block() instanceof CoreBlock block) {
                    tiles.getc(save.x, save.y).setNet(block, Team.sharded, 0);
                    tiles.getc(save.x, tiles.height - save.y - 1).setNet(block, Team.blue, 0);

                    new BlockRoom(Team.sharded, save.x, save.y, 5000);
                    new BlockRoom(Team.blue, save.x, tiles.height - save.y - 1, 5000);
                } else if (save.build instanceof SorterBuild sorterBuild) {
                    Item item = sorterBuild.config();
                    int cost = 250 + Mathf.ceil(item.hardness == 0 ? item.cost * 500 : item.hardness * 250);
                    new MinerRoom(item, Team.sharded, save.x, save.y, cost);
                    new MinerRoom(item, Team.blue, save.x, tiles.height - save.y - 1, cost);
                } else if (save.overlay() == Blocks.spawn) {
                    shardedSpawn = tiles.getc(save.x, save.y);
                    blueSpawn = tiles.getc(save.x, tiles.height - save.y - 1);
                }
            }
        }

        generateShop(8, saved.height + 7);
    }

    public void generateShop(int shopX, int shopY) {
        CastleUnits.units.each((type, money) -> {
            addUnitRoom(type, shopX + size * offset++, shopY + (top ? size * 2 : 0));
            if (offset % 5 == 0 && (top = !top)) offset -= 5; // bruh, just don't touch it
        });
    }

    public void addUnitRoom(UnitType type, int x, int y){
        Moneys money = CastleUnits.units.get(type);
        new UnitRoom(type, UnitRoom.UnitRoomType.attack, money.income(), x, y, money.cost());
        new UnitRoom(type, UnitRoom.UnitRoomType.defend, -money.income(), x, y + size, money.cost());
    }
}
