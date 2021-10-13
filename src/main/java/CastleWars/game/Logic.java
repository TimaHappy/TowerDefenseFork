package CastleWars.game;

import CastleWars.Main;
import CastleWars.data.PlayerData;
import CastleWars.logic.Room;
import CastleWars.Bundle;
import CastleWars.logic.RoomComp;
import arc.Events;
import arc.struct.Seq;
import arc.util.Timer;
import arc.util.Interval;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.logic;
import static mindustry.Vars.netServer;

public class Logic {

    public boolean worldLoaded = false;
    public float x = 0, y = 0, endx = 0, endy = 0;

    Seq<Tile> cores = new Seq<>();

    Interval interval = new Interval();

    public Logic() {
        Events.on(EventType.BlockDestroyEvent.class, event -> {
            if (!(event.tile.build instanceof CoreBlock.CoreBuild) || event.tile.build.team.cores().size > 1 || !worldLoaded) return;

            endGame(event.tile.build.team() == Team.sharded ? Team.blue : Team.sharded);
        });
    }

    public void update() {
        if (state.isPaused() || !worldLoaded) return;

        PlayerData.datas.values().forEach(PlayerData::update);

        Room.rooms.each(RoomComp::update);

        // Kill all units in centre
        Groups.unit.intersect(x, y, endx, endy, u -> {
            if (u.isPlayer()) u.getPlayer().unit(Nulls.unit);
            u.kill();
        });

        if (interval.get(6f)) {
            Groups.unit.each(Flyingc::isFlying, unit -> {
                if (!Main.logic.placeCheck(unit.team(), unit.tileOn())) unit.damagePierce(unit.maxHealth / 100);
            });
        }
    }

    public void restart() {
        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        logic.reset();

        UnitTypes.omura.abilities.clear();
        UnitTypes.corvus.abilities.clear();
        Blocks.itemSource.health = 999999;
        Blocks.liquidSource.health = 999999;
        Blocks.coreNucleus.unitCapModifier = 999;
        Blocks.coreShard.unitCapModifier = 999;
        Room.rooms.clear();

        PlayerData.datas.values().forEach(PlayerData::reset);

        Generator gen = new Generator();
        gen.run();
        Timer.schedule(() -> cores = gen.cores.copy(), 2);
        Call.worldDataBegin();

        int half = gen.height - (Room.ROOM_SIZE * 6);
        half = half / 2;
        y = half * tilesize;
        endy = (Room.ROOM_SIZE * 6) * tilesize;
        x = -5 * tilesize;
        endx = (5 + gen.width) * tilesize;

        players.each(player -> {
            netServer.assignTeam(player, players);
            netServer.sendWorldData(player);
            PlayerData.labels(player);
        });

        Call.setRules(Main.rules);
        logic.play();
        state.rules = Main.rules;
        Call.setRules(state.rules);
        // 7.5 секунд после загрузки мира, чтобы надписи и магазин точно успели загрузиться
        Timer.schedule(() -> worldLoaded = true, 7.5f);
    }

    public void endGame(Team team) {
        // Не самая лучшая проверка, но конкретно здесь сойдет
        Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format(team == Team.blue ? "events.win.blue" : "events.win.sharded", Bundle.findLocale(p))));
        Timer.schedule(this::restart, 7.5f);
        worldLoaded = false;
    }

    /**
     * @param team - Команда, для которой надо произвести проверку
     * @param tile - Тайл, для которого надо произвести проверку
     * @return - Находится ли данный тайл на территории данной команды
     */
    public boolean placeCheck(Team team, Tile tile) {
        if (tile == null) return true;
        if (team == Team.blue) {
            return (tile.y * tilesize) > (y + endy);
        } else if (team == Team.sharded) {
            return (tile.y * tilesize) < y;
        }
        return true;
    }
}
