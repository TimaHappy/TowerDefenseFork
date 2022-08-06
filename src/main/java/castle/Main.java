package castle;

import arc.Events;
import arc.util.Structs;
import arc.util.Timer;
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

import static arc.Core.*;
import static castle.CastleUtils.isBreak;
import static castle.CastleUtils.timer;
import static mindustry.Vars.*;

public class Main extends Plugin {

    public static final int roundTime = 45 * 60;

    public static Locale findLocale(Player player) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> l.toString().equals(player.locale) || player.locale.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale;
    }

    @Override
    public void init() {
        Bundle.load();
        CastleCosts.load();
        CastleIcons.load();

        world = new CastleWorld();

        netServer.admins.addActionFilter(action -> action.tile == null || !(action.tile.block() instanceof Turret) && !(action.tile.block() instanceof Drill));

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
                    Call.label(data.player.con, "[lime]+[accent] " + income, 2f, event.unit.x, event.unit.y);
                }
            });
        });

        Events.on(ResetEvent.class, event -> {
            CastleRooms.rooms.clear();
            // PlayerData.datas.clear();
        });

        Events.on(WorldLoadEvent.class, event -> app.post(() -> {
            CastleUtils.timer = roundTime;
            CastleUtils.applyRules(state.rules);
        }));

        Events.run(Trigger.update, () -> {
            if (isBreak() || state.serverPaused) return;

            Groups.unit.each(unit -> unit.isFlying() && !unit.spawnedByCore && (unit.floorOn() == null || unit.floorOn() == Blocks.space), Call::unitDespawn);

            PlayerData.datas().each(PlayerData::update);
            CastleRooms.rooms.each(Room::update);
        });

        Timer.schedule(() -> {
            if (isBreak() || state.serverPaused) return;

            PlayerData.datas().each(PlayerData::updateMoney);

            if (--timer <= 0) Events.fire(new GameOverEvent(Team.derelict));
        }, 0f, 1f);
    }
}
