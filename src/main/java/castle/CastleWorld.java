package castle;

import arc.util.Log;
import mindustry.core.GameState.State;
import mindustry.core.World;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.io.SaveIO;
import mindustry.maps.Map;

import static mindustry.Vars.content;
import static mindustry.Vars.state;

public class CastleWorld extends World {

    @Override
    public void loadMap(Map map, Rules rules) {
        loadMap(map);
    }

    @Override
    public void loadMap(Map map) {
        Rules rules = map.applyRules(Gamemode.pvp);
        CastleUtils.planet = content.planets().find(planet -> planet.accessible && (planet.defaultEnv == rules.env || planet.hiddenItems.asSet().equals(rules.hiddenBuildItems)));

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
        CastleUtils.applyRules(state.rules);
        super.endMapLoad();
    }
}
