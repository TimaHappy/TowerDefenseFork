package CastleWars.game;

import CastleWars.Main;
import CastleWars.data.PlayerData;
import CastleWars.logic.Room;
import CastleWars.Bundle;
import arc.Events;
import arc.struct.Seq;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Nulls;
import mindustry.gen.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

public class Logic {

    public boolean worldLoaded = false;
    public float x = 0, y = 0, endx = 0, endy = 0;

    Seq<Tile> cores = new Seq<>();

    public Logic() {
        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if (!(e.tile.build instanceof CoreBlock.CoreBuild) || e.tile.build.team.cores().size > 1 || !worldLoaded) return;

            if (e.tile.build.team == Team.sharded) gameOver(Team.blue);
            else gameOver(Team.sharded);
        });
    }

    public void update() {
        if (Vars.state.isPaused() || !worldLoaded) return;
        for (PlayerData data : PlayerData.datas.values()) {
            data.update();
        }

        for (Room room : Room.rooms) {
            room.update();
        }
        // Kill all units in centre
        Groups.unit.intersect(x, y, endx, endy, u -> {
            if (u.isPlayer()) {
                u.getPlayer().unit(Nulls.unit);
            }
            u.kill();
        });
    }

    public void restart() {
        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        Vars.logic.reset();

        UnitTypes.omura.abilities.clear();
        UnitTypes.mono.weapons.add(UnitTypes.crawler.weapons.get(0));
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
        y = half * Vars.tilesize;
        endy = (Room.ROOM_SIZE * 6) * Vars.tilesize;
        x = -5 * Vars.tilesize;
        endx = (5 + gen.width) * Vars.tilesize;

        for (Player player : players) {
            Vars.netServer.assignTeam(player, players);
            Vars.netServer.sendWorldData(player);
            PlayerData.labels(player);
        }

        Call.setRules(Main.rules);
        Vars.logic.play();
        Vars.state.rules = Main.rules;
        Call.setRules(Vars.state.rules);
        // AntiInstantGameStart
        Timer.schedule(() -> worldLoaded = true, 5);
    }

    public void gameOver(Team team) {
        Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format(team == Team.blue ? "events.win.blue" : "events.win.sharded", Bundle.findLocale(p))));
        Timer.schedule(this::restart, 3);
        worldLoaded = false;
    }
}
