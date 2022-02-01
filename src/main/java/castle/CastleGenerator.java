package castle;

import arc.func.Cons;
import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.world;

public class CastleGenerator implements Cons<Tiles> {

    public Tiles saved;

    @Override
    public void get(Tiles tiles) {
        world.setGenerating(true);
        this.saved = tiles;

        Log.info("Resizing...");
        tiles = world.resize(tiles.width, tiles.height * 2 + CastleRooms.size * 6);

        Log.info("Filling...");
        for (int x = 0; x < tiles.width; x++) {
            for (int y = 0; y < tiles.height; y++) {
                tiles.set(x, y, new Tile(x, y, Blocks.space, Blocks.air, Blocks.air));
            }
        }

        Log.info("Copying...");
        for (int x = 0; x < tiles.width; x++) {
            for (int y = 0; y < saved.height; y++) {
                Tile save = saved.getc(x, y);

                tiles.getc(x, y).setFloor(save.floor());
                tiles.getc(x, y).setBlock(save.block(), Team.sharded);

                tiles.getc(x, tiles.height - y).setFloor(save.floor());
                tiles.getc(x, tiles.height - y).setBlock(save.block(), Team.blue);
            }
        }

        Log.info("Done...");
        world.setGenerating(false);
    }
}
