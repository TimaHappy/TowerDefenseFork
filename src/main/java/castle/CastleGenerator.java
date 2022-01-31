package castle;

import arc.func.Cons;
import mindustry.world.Tiles;

public class CastleGenerator implements Cons<Tiles> {

    @Override
    public void get(Tiles tiles) {
        tiles.eachTile(tile -> {

        });
    }
}
