package castle;

import arc.Events;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Structs;
import arc.util.Time;
import castle.CastleRooms.Room;
import castle.components.Bundle;
import castle.components.CastleCosts;
import castle.components.CastleIcons;
import castle.components.PlayerData;
import mindustry.content.Blocks;
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

import java.util.Locale;

import static castle.CastleLogic.*;
import static mindustry.Vars.netServer;
import static mindustry.Vars.state;

public class Main extends Plugin {

    public static final Seq<String> votesRtv = new Seq<>();
    public static final Interval interval = new Interval();

    public static final float voteRatio = 0.6f;

    @Override
    public void init() {
        Bundle.load();
        CastleCosts.load();
        CastleIcons.load();
        CastleLogic.load();

        netServer.admins.addActionFilter(action -> {
            if (action.tile != null && (action.tile.block() instanceof Turret || action.tile.block() instanceof Drill)) return false;
            return action.tile == null || action.type != ActionType.placeBlock || (action.tile.dst(CastleRooms.shardedSpawn) > 64 && action.tile.dst(CastleRooms.blueSpawn) > 64);
        });

        netServer.assigner = (player, players) -> Groups.player.count(p -> p.team() == Team.blue) >= Groups.player.count(p -> p.team() == Team.sharded) ? Team.sharded : Team.blue;

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
            int income = CastleCosts.drop(event.unit.type);
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

            Groups.unit.each(unit -> unit.isFlying() && (unit.tileOn() == null || unit.tileOn().floor() == Blocks.space), Call::unitDespawn);

            PlayerData.datas().each(PlayerData::update);
            CastleRooms.rooms.each(Room::update);

            if (timer <= 0) gameOver(Team.derelict);
            else if (interval.get(60f)) timer--;
        });

        Events.on(ServerLoadEvent.class, event -> Time.runTask(360f, () -> {
            CastleLogic.restart();
            netServer.openServer();
        }));
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
            int current = votesRtv.size;
            int required = Mathf.ceil(voteRatio * Groups.player.size());
            sendToChat("commands.rtv.vote", player.coloredName(), current, required);

            if (current < required) return;

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

    public static Locale findLocale(Player player) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> l.toString().equals(player.locale) || player.locale.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale;
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, findLocale(player), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }
}
