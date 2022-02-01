package castle;

import mindustry.content.Blocks;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.maps.Map;

import static mindustry.Vars.*;

public class CastleLogic {

    public static void startHosting(Map map) {
        logic.reset();

        world.loadMap(map, map.applyRules(Gamemode.pvp));
        CastleGenerator generator = new CastleGenerator();
        generator.get(world.tiles);

        state.rules = applyRules(state.map.applyRules(Gamemode.pvp));
        logic.play();

        netServer.openServer();
    }

    public static void restart() {

    }

    public static Rules applyRules(Rules rules) {
        rules.teams.get(Team.sharded).cheat = true;
        rules.teams.get(Team.blue).cheat = true;

        rules.pvp = true;
        rules.canGameOver = false;

        rules.waves = false;
        rules.waveTimer = false;
        rules.revealedBlocks.addAll(Blocks.duct, Blocks.ductRouter, Blocks.ductBridge, Blocks.thruster, Blocks.scrapWall, Blocks.scrapWallLarge, Blocks.scrapWallHuge, Blocks.scrapWallGigantic);
        rules.modeName = "Castle";

        return rules;
    }
}
