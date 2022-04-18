package castle;

import arc.Events;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Timer;
import castle.components.Bundle;
import castle.components.PlayerData;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.meta.BlockGroup;

import static mindustry.Vars.*;

public class CastleLogic {

    public static Rules rules = new Rules();
    public static int timer = 45 * 60;

    public static void load() {
        rules.pvp = true;
        rules.canGameOver = false;

        rules.unitCap = 500;
        rules.unitCapVariable = false;

        rules.polygonCoreProtection = true;
        rules.buildSpeedMultiplier = 0.5f;
        rules.buildCostMultiplier = 2.5f;

        rules.waves = false;
        rules.waveTimer = false;
        rules.modeName = "Castle Wars";

        rules.teams.get(Team.sharded).cheat = true;
        rules.teams.get(Team.blue).cheat = true;

        rules.bannedBlocks.addAll(content.blocks().select(b -> b.group != BlockGroup.power && b.group != BlockGroup.walls && b.group != BlockGroup.projectors && b.group != BlockGroup.none && b.group != BlockGroup.transportation));
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

        CastleGenerator gen = new CastleGenerator();
        gen.loadMap(maps.getNextMap(Gamemode.pvp, state.map));
        Call.worldDataBegin();

        timer = 45 * 60;
        state.rules = rules;
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
        Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format(team == Team.derelict ? "events.draw" : "events.gameover", Bundle.findLocale(p), colorizedTeam(team))));
        Call.hideHudText();

        Timer.schedule(CastleLogic::restart, 10f);
    }

    public static String colorizedTeam(Team team) {
        return "[#" + team.color + "]" + team.name;
    }
}
