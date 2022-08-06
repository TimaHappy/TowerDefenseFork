package castle;

import mindustry.content.Planets;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.maps.Map;
import mindustry.type.Planet;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.meta.BlockGroup;

import static castle.Main.roundTime;
import static mindustry.Vars.*;

public class CastleUtils {

    public static Planet planet;
    public static int timer = roundTime;

    public static void checkPlanet(Map map) {
        Rules rules = map.applyRules(Gamemode.pvp);
        planet = content.planets().find(planet -> planet.accessible && (planet.defaultEnv == rules.env || planet.hiddenItems.asSet().equals(rules.hiddenBuildItems)));
    }

    public static void applyRules(Rules rules) {
        //rules.pvp = true;

        rules.unitCap = 500;
        rules.unitCapVariable = false;

        rules.dropZoneRadius = 10f;
        rules.showSpawns = true;

        rules.polygonCoreProtection = true;
        rules.buildSpeedMultiplier = 0.5f;
        rules.buildCostMultiplier = 2.5f;

        rules.waves = false;
        rules.waveTimer = false;
        rules.modeName = "Castle Wars";

        rules.teams.get(Team.sharded).cheat = true;
        rules.teams.get(Team.blue).cheat = true;

        rules.bannedBlocks.addAll(content.blocks().select(block -> block instanceof CoreBlock || block instanceof UnitFactory || block.group == BlockGroup.turrets || block.group == BlockGroup.drills || block.group == BlockGroup.logic));
    }

    public static boolean isBreak() {
        return world.isGenerating() || state.gameOver;
    }

    public static boolean isSerpulo() {
        return planet == Planets.serpulo;
    }

    public static boolean isErekir() {
        return planet == Planets.erekir;
    }
}
