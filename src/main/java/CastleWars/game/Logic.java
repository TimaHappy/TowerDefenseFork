package CastleWars.game;

import CastleWars.Bundle;
import CastleWars.Main;
import CastleWars.data.PlayerData;
import CastleWars.rooms.Room;
import CastleWars.rooms.RoomComp;
import arc.Events;
import arc.struct.Seq;
import arc.util.Timer;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.Tile;

import static mindustry.Vars.*;

public class Logic {

    public static boolean worldLoaded = false;
    public static float x = 0, y = 0, endx = 0, endy = 0;

    public static void update() {
        if (worldLoaded) {
            PlayerData.allDatas().each(PlayerData::update);
            Room.rooms.each(RoomComp::update);

            Groups.unit.intersect(x, y, endx, endy, Logic::killUnit);

            Groups.unit.each(Flyingc::isFlying, u -> {
                if (!placeCheck(u.team(), u.tileOn())) u.damagePierce(u.maxHealth / 500f);

                if (u.tileX() > world.width() || u.tileX() < 0 || u.tileY() > world.height() || u.tileY() < 0) killUnit(u);
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
        Room.rooms.clear();
        PlayerData.allDatas().each(PlayerData::reset);

        Generator gen = new Generator();
        gen.run();
        Call.worldDataBegin();

        int half = (gen.height - (Room.ROOM_SIZE * 6)) / 2;
        y = half * tilesize;
        endy = Room.ROOM_SIZE * 6 * tilesize;
        x = -5 * tilesize;
        endx = (5 + gen.width) * tilesize;

        state.rules = Main.rules.copy();
        logic.play();

        players.each(netServer::sendWorldData);

        Timer.schedule(() -> worldLoaded = true, 6f);
    }

    public static void endGame(Team team) {
        Events.fire("CastleGameOver");

        Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format(team == Team.blue ? "events.win.blue" : "events.win.sharded", Bundle.findLocale(p))));
        Timer.schedule(Logic::restart, 6f);
        worldLoaded = false;
    }

    public static boolean placeCheck(Team team, Tile tile) {
        if (tile != null) return team == Team.blue ? tile.worldy() > y + endy : tile.worldy() < y;
        return true;
    }

    public static boolean placeCheck(Player player) {
        return placeCheck(player.team(), player.tileOn());
    }

    public static void killUnit(Unit unit) {
        if (unit.isPlayer()) unit.getPlayer().clearUnit();
        unit.kill();
    }
}
