package CastleWars.logic;

import CastleWars.data.PlayerData;
import mindustry.content.Blocks;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;

public interface RoomComp {
    int cost();
    
    int size();
    
    int x();
    int y();

    float drawx();
    float drawy();

    float endDrawx();
    float endDrawy();
    
    void buy(PlayerData data);
    
    void update();
    
    default boolean canBuy(PlayerData data) {
        return data.money >= cost();
    }
    
    default boolean check(float x, float y) {
        return (x > drawx() && y > drawy() && x < endDrawx() && y < endDrawy());
    }
    
    default void spawn(Tiles t) {
        for (int x = 0; x <= size(); x++) {
            for (int y = 0; y <= size(); y++) {
                if (t.getn(x + x(), y + y()).floor().equals(Blocks.metalFloor) || t.getn(x + x(), y + y()).floor().equals(Blocks.metalFloor5)) break;
                if (x == 0 || y == 0 || x == size() || y == size()) t.getn(x + x(), y + y()).setFloor((Floor) Blocks.metalFloor5);
                else t.getn(x + x(), y + y()).setFloor((Floor) Blocks.metalFloor);
            }
        }
    }
}
