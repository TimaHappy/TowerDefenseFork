package castle;

import arc.Events;
import arc.func.Boolf;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Timer;
import castle.CastleRooms.Room;
import castle.components.Bundle;
import castle.components.PlayerData;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Flyingc;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.meta.BlockGroup;

import java.util.Objects;

import static mindustry.Vars.*;

public class CastleLogic {

    /** creating a new one every match is masochism */
    public static Rules rules = new Rules();

    public static Interval interval = new Interval();
    public static int timer = 45 * 60;

    public static int halfHeight;

    public static void init() {
        rules.pvp = true;
        rules.canGameOver = false;

        rules.unitCap = 500;
        rules.unitCapVariable = false;

        rules.waves = false;
        rules.waveTimer = false;
        rules.modeName = "Wars";

        rules.bannedBlocks.addAll(content.blocks().select(b -> b.group == BlockGroup.turrets || b.group == BlockGroup.logic || b instanceof StorageBlock));
    }

    public static void update() {
        if (!world.isGenerating() && !state.serverPaused && !state.gameOver) {
            if (interval.get(60f)) timer--;

            if (timer <= 0) {
                gameOver(Team.derelict);
                return;
            }

            PlayerData.datas().each(PlayerData::update);
            CastleRooms.rooms.each(Room::update);

            Groups.unit.each(Flyingc::isFlying, unit -> {
                if (unit.tileX() > world.width() || unit.tileX() < 0 || unit.tileY() > world.height() || unit.tileY() < 0 || (unit.tileY() > halfHeight && unit.tileY() < world.height() - halfHeight - 1))
                    Call.unitDespawn(unit);
            });
        }
    }

    public static void restart() {
        Seq<Player> players = new Seq<>();
        Groups.player.each(player -> {
            players.add(player);
            player.clearUnit();
        });

        logic.reset();
        CastleRooms.rooms.clear();
        PlayerData.datas().each(PlayerData::reset);

        CastleGenerator gen = new CastleGenerator();
        gen.loadMap(maps.getNextMap(Gamemode.pvp, state.map));
        Call.worldDataBegin();

        state.rules = rules;
        logic.play();

        timer = 45 * 60;

        players.each(player -> netServer.sendWorldData(player));
    }

    public static void gameOver(Team team) {
        Events.fire("CastleGameOver");
        Call.updateGameOver(team);

        Log.info("Игра окончена. Генерирую карту заново...");
        if (team != Team.derelict) {
            Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format("events.gameover", Bundle.findLocale(p), colorizedTeam(team))));
        } else {
            Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format("events.draw", Bundle.findLocale(p))));
        }
        Timer.schedule(CastleLogic::restart, 10f);
    }

    public static String colorizedTeam(Team team) {
        return "[#" + team.color + "]" + team.name;
    }

    public static boolean checkNearby(Tile tile, Boolf<Tile> boolf) {
        Seq<Tile> nearby = Seq.with(tile, tile.nearby(0), tile.nearby(1), tile.nearby(2), tile.nearby(3), tile.nearby(-1, -1), tile.nearby(1, -1), tile.nearby(-1, 1), tile.nearby(1, 1)).filter(Objects::nonNull);
        return nearby.contains(boolf);
    }
}
