package castle;

import arc.Events;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Timer;
import castle.components.Bundle;
import castle.components.PlayerData;
import mindustry.content.Planets;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.maps.Map;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.meta.BlockGroup;

import static castle.Main.findLocale;
import static mindustry.Vars.*;

public class CastleLogic {

    public static AllowedContent allowedContent;
    public static int timer = 45 * 60;

    public static void applyRules(Rules rules) {
        rules.pvp = true;
        rules.canGameOver = false;

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

    public static void restart() {
        Seq<Player> players = new Seq<>();
        Groups.player.each(player -> {
            players.add(player);
            player.clearUnit();
        });

        logic.reset();
        CastleRooms.rooms.clear();
        PlayerData.datas.clear();

        Map map = maps.getNextMap(Gamemode.pvp, state.map);

        state.rules = map.rules(new Rules());

        var erekirOnlyItems = Planets.erekir.hiddenItems.asSet();
        var serpuloOnlyItems = Planets.serpulo.hiddenItems.asSet();

        if (state.rules.env == Planets.erekir.defaultEnv || state.rules.hiddenBuildItems.equals(erekirOnlyItems)) {
            allowedContent = AllowedContent.erekir;
        } else if (state.rules.env == Planets.serpulo.defaultEnv || state.rules.hiddenBuildItems.equals(serpuloOnlyItems)) {
            allowedContent = AllowedContent.serpulo;
        } else {
            allowedContent = AllowedContent.any;
        }

        CastleGenerator generator = new CastleGenerator();
        generator.loadMap(map);

        applyRules(state.rules);

        Call.worldDataBegin();

        timer = 45 * 60;
        logic.play();

        players.each(player -> {
            netServer.sendWorldData(player);
            PlayerData.datas.put(player.uuid(), new PlayerData(player));
        });
    }

    public static void gameOver(Team team) {
        Events.fire("CastleGameOver");
        Call.updateGameOver(team);

        Log.info("Игра окончена. Загружаю новую карту...");
        Groups.player.each(p -> Call.infoMessage(p.con, Bundle.format(team == Team.derelict ? "events.draw" : "events.gameover", findLocale(p), colorizedTeam(team))));
        Call.hideHudText();

        Timer.schedule(CastleLogic::restart, 10f);
    }

    public static String colorizedTeam(Team team) {
        return "[#" + team.color + "]" + team.name;
    }

    public static Unit spawnUnit(UnitType type, Team team, float x, float y) {
        if (world.tileWorld(x, y) != null && world.tileWorld(x, y).solid()) world.tileWorld(x, y).removeNet();
        return type.spawn(team, x, y);
    }

    public static boolean isBreak() {
        return world.isGenerating() || state.gameOver;
    }

    public static boolean isSerpulo() {
        return allowedContent == AllowedContent.serpulo;
    }

    public static boolean isErekir() {
        return allowedContent == AllowedContent.erekir;
    }

    public enum AllowedContent {
        erekir, serpulo, any
    }
}
