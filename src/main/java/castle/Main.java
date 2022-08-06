package castle;

import arc.Events;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Structs;
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
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.production.Drill;

import java.util.Locale;

import static castle.CastleUtils.isBreak;
import static castle.CastleUtils.timer;
import static mindustry.Vars.*;

public class Main extends Plugin {

    public static final Interval interval = new Interval();

    public static final int roundTime = 45 * 60;

    public static Locale findLocale(Player player) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> l.toString().equals(player.locale) || player.locale.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale;
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, findLocale(player), values));
    }

    @Override
    public void init() {
        Bundle.load();
        CastleCosts.load();
        CastleIcons.load();

        world = new CastleWorld();

        netServer.admins.addActionFilter(action -> {
            if (action.tile == null) return true;

            return !(action.tile.block() instanceof Turret) && !(action.tile.block() instanceof Drill);
        });

        netServer.assigner = (player, players) -> {
            Seq<Player> seq = Seq.with(players);
            int sharded = seq.count(p -> p.team() == Team.sharded);
            int blue = seq.count(p -> p.team() == Team.blue);

            return sharded > blue ? Team.blue : Team.sharded;
        };

        Events.on(PlayerJoin.class, event -> {
            if (PlayerData.datas.containsKey(event.player.uuid())) {
                PlayerData.datas.get(event.player.uuid()).handlePlayerJoin(event.player);
            } else {
                PlayerData.datas.put(event.player.uuid(), new PlayerData(event.player));
            }
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

        Events.on(ResetEvent.class, event -> {
            CastleRooms.rooms.clear();
            PlayerData.datas.clear();
        });

        Events.on(PlayEvent.class, event -> {
            CastleUtils.timer = roundTime;
            Groups.player.each(player -> PlayerData.datas.put(player.uuid(), new PlayerData(player)));
        });

        Events.run(Trigger.update, () -> {
            if (isBreak() || state.serverPaused) return;

            Groups.unit.each(unit -> unit.isFlying() && !unit.spawnedByCore && (unit.tileOn() == null || unit.tileOn().floor() == Blocks.space), Call::unitDespawn);

            PlayerData.datas().each(PlayerData::update);
            CastleRooms.rooms.each(Room::update);

            if (timer <= 0) Events.fire(new GameOverEvent(Team.derelict));
            else if (interval.get(60f)) timer--;
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("hud", "Toggle HUD.", (args, player) -> {
            PlayerData data = PlayerData.datas.get(player.uuid());
            data.hideHud = !data.hideHud;
            if (data.hideHud) Call.hideHudText(player.con);
            bundled(player, data.hideHud ? "commands.hud.off" : "commands.hud.on");
        });
    }
}
