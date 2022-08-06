package castle;

import arc.util.Log;
import mindustry.core.GameState.State;
import mindustry.core.World;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.io.SaveIO;
import mindustry.maps.Map;

import static mindustry.Vars.state;

public class CastleWorld extends World {

    public Map map;

    @Override
    public void loadMap(Map map, Rules rules) {
        loadMap(map);
    }

    @Override
    public void loadMap(Map map) {
        CastleUtils.checkPlanet(this.map = map);

        try {
            SaveIO.load(map.file, context);
        } catch (Exception e) {
            state.set(State.menu);
            Log.err(e);
        }
    }

    @Override
    public void endMapLoad() {
        CastleGenerator.generate();
        state.map = map;
        state.rules = CastleUtils.applyRules(map.applyRules(Gamemode.pvp));
        super.endMapLoad();
    }
}
