package castle;

import mindustry.io.SaveIO;
import mindustry.maps.Map;

import static mindustry.Vars.*;

public class CastleLogic {

    public static void loadMap(Map map) {
        CastleGenerator generator = new CastleGenerator();

        world.beginMapLoad();

        world.resize(map.width, map.height * 2 + CastleRooms.size * 6);
        world.loadMap(map);
        generator.get(world.tiles);

        world.endMapLoad();
    }
}
