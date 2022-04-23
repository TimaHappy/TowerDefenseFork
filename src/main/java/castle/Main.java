package castle;

import arc.Events;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import castle.CastleRooms.Room;
import castle.ai.AIShell;
import castle.components.*;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration.ActionType;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.storage.CoreBlock;

import static castle.CastleLogic.*;
import static castle.components.Bundle.*;
import static mindustry.Vars.*;

public class Main extends Plugin {

    public static Seq<String> votesRtv = new Seq<>();
    public static Interval interval = new Interval();

    public static float voteRatio = 0.6f;

    @Override
    public void init() {
        ((CoreBlock) Blocks.coreShard).unitType = UnitTypes.poly;
        ((CoreBlock) Blocks.coreNucleus).unitType = UnitTypes.mega;

        CastleLogic.load();
        CastleIcons.load();
        CastleUnits.load();
        CastleRooms.TurretRoom.loadCosts();

        content.units().each(unit -> {
           var parent = unit.defaultController;
           unit.defaultController = () -> new AIShell(parent);
        });

        netServer.admins.addActionFilter(action -> {
            if (action.tile != null && (action.tile.block() instanceof Turret || action.tile.block() instanceof Drill)) return false;
            return action.tile == null || action.type != ActionType.placeBlock || (action.tile.dst(CastleRooms.shardedSpawn) > 64 && action.tile.dst(CastleRooms.blueSpawn) > 64);
        });

        netServer.assigner = (player, arr) -> {
            var players = Seq.with(arr);
            int sharded = players.count(p -> p.team() == Team.sharded);
            return players.size - sharded >= sharded ? Team.sharded : Team.blue;
        };

        Events.on(PlayerJoin.class, event -> {
            if (PlayerData.datas.containsKey(event.player.uuid())) {
                PlayerData.datas.get(event.player.uuid()).handlePlayerJoin(event.player);
            } else {
                PlayerData.datas.put(event.player.uuid(), new PlayerData(event.player));
            }
        });

        Events.on(PlayerLeave.class, event -> {
            if (votesRtv.remove(event.player.uuid())) {
                sendToChat("commands.rtv.left", event.player.coloredName(), votesRtv.size, Mathf.ceil(voteRatio * Groups.player.size()));
            }
        });

        Events.on(BlockDestroyEvent.class, event -> {
            if (isBreak() || state.serverPaused) return;
            if (event.tile.block() instanceof CoreBlock && event.tile.team().cores().size <= 1)
                gameOver(event.tile.team() == Team.sharded ? Team.blue : Team.sharded);
        });

        Events.on(UnitDestroyEvent.class, event -> {
            int income = CastleUnits.drop(event.unit.type);
            if (income <= 0 || event.unit.spawnedByCore) return;
            PlayerData.datas().each(data -> {
                if (data.player.team() != event.unit.team) {
                    data.money += income;
                    Call.label(data.player.con, "[lime] + [accent]" + income, 2f, event.unit.x, event.unit.y);
                }
            });
        });

        Events.run(Trigger.update, () -> {
            if (isBreak() || state.serverPaused) return;

            Groups.unit.each(unit -> unit.isFlying() && !unit.spawnedByCore && (unit.tileOn() == null || unit.tileOn().floor() == Blocks.space), Call::unitDespawn);

            PlayerData.datas().each(PlayerData::update);
            CastleRooms.rooms.each(Room::update);

            if (timer <= 0) gameOver(Team.derelict);
            else if (interval.get(60f)) timer--;
        });

        CastleLogic.restart();
        netServer.openServer();
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("hud", "Toggle HUD.", (args, player) -> {
            PlayerData data = PlayerData.datas.get(player.uuid());
            data.hideHud = !data.hideHud;
            if (data.hideHud) Call.hideHudText(player.con);
            bundled(player, data.hideHud ? "commands.hud.off" : "commands.hud.on");
        });

        handler.<Player>register("rtv", "Vote to skip the map.", (args, player) -> {
            if (votesRtv.contains(player.uuid())) {
                bundled(player, "commands.rtv.already-voted");
                return;
            }

            if (isBreak()) {
                bundled(player, "commands.rtv.can-not-vote");
                return;
            }

            votesRtv.add(player.uuid());
            int cur = votesRtv.size;
            int req = Mathf.ceil(voteRatio * Groups.player.size());
            sendToChat("commands.rtv.vote", player.coloredName(), cur, req);

            if (cur < req) return;

            sendToChat("commands.rtv.passed");
            votesRtv.clear();
            gameOver(Team.derelict);
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("host");
        handler.removeCommand("stop");
        handler.removeCommand("gameover");

        handler.register("gameover", "End the game.", args -> gameOver(Team.derelict));
    }
}
