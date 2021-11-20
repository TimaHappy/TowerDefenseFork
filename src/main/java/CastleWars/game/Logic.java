package CastleWars.game;

import CastleWars.Main;
import CastleWars.data.PlayerData;
import CastleWars.logic.Room;
import CastleWars.Bundle;
import CastleWars.logic.RoomComp;
import arc.Events;
import arc.struct.Seq;
import arc.util.Timer;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.logic;
import static mindustry.Vars.netServer;
import static mindustry.Vars.world;

public class Logic {

    public boolean worldLoaded = false;
    public float x = 0, y = 0, endx = 0, endy = 0;

    public Logic() {
        Events.on(EventType.BlockDestroyEvent.class, event -> {
            if (event.tile.build instanceof CoreBlock.CoreBuild && event.tile.build.team.cores().size <= 1 && worldLoaded) {
                endGame(event.tile.build.team() == Team.sharded ? Team.blue : Team.sharded);
            }
        });
    }

    public void update() {
        if (worldLoaded) {
            PlayerData.datas.values().forEach(PlayerData::update);
            Room.rooms.each(RoomComp::update);

            Groups.unit.intersect(x, y, endx, endy, this::killUnit);

            Groups.unit.each(Flyingc::isFlying, u -> {
                if (!Main.logic.placeCheck(u.team(), u.tileOn())) u.damagePierce(u.maxHealth / 1000);

                if ((u.x / 8f - 5) > world.width() || u.x / 8f < -5 || (u.y / 8f - 5) > world.height() || u.y / 8f < -5) killUnit(u);
            });
        }
    }

    public void restart() {
        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        logic.reset();
        Room.rooms.clear();
        PlayerData.datas.values().forEach(PlayerData::reset);

        Generator gen = new Generator();
        gen.run();
        Call.worldDataBegin();

        int half = (gen.height - (Room.ROOM_SIZE * 6)) / 2;
        y = half * tilesize;
        endy = (Room.ROOM_SIZE * 6) * tilesize;
        x = -5 * tilesize;
        endx = (5 + gen.width) * tilesize;

        players.each(player -> {
            netServer.assignTeam(player, players);
            netServer.sendWorldData(player);
        });

        logic.play();
        state.rules = Main.rules;
        Call.setRules(state.rules);
        Timer.schedule(() -> worldLoaded = true, 6f);
    }

    public void endGame(Team team) {
        Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format(team == Team.blue ? "events.win.blue" : "events.win.sharded", Bundle.findLocale(p))));
        Timer.schedule(this::restart, 6f);
        worldLoaded = false;
    }

    public boolean placeCheck(Team team, Tile tile) {
        if (tile != null) {
            if (team == Team.blue) {
                return (tile.y * tilesize) > (y + endy);
            } else if (team == Team.sharded) {
                return (tile.y * tilesize) < y;
            }
        }
        return true;
    }

    public boolean placeCheck(Player player) {
        return placeCheck(player.team(), player.tileOn());
    }

    public void killUnit(Unit unit) {
        if (unit.isPlayer()) unit.getPlayer().unit(Nulls.unit);
        unit.kill();
    }
}
