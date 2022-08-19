package castle;

import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Interval;
import castle.CastleRooms.Room;
import castle.components.Bundle;
import castle.components.CastleCosts;
import castle.components.CastleIcons;
import castle.components.PlayerData;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.type.UnitType;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.Locale;

import static castle.CastleUtils.isBreak;
import static castle.CastleUtils.timer;
import static mindustry.Vars.*;

public class Main extends Plugin {

    public static final int roundTime = 45 * 60;

    public static final Interval interval = new Interval();

    public static Locale findLocale(Player player) {
        Locale locale = Bundle.supportedLocales.find(l -> l.toString().equals(player.locale) || player.locale.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale;
    }

    @Override
    public void init() {
        for (UnitType unit : content.units()) {
            unit.payloadCapacity = 0f;
            unit.controller = u -> !unit.playerControllable ? unit.aiController.get() : new CastleCommandAI();
        }

        Bundle.load();
        CastleCosts.load();
        CastleIcons.load();

        world = new CastleWorld();

        netServer.admins.addActionFilter(action -> action.tile == null || !(action.tile.block() instanceof Turret) && !(action.tile.block() instanceof Drill));

        Events.on(PlayerJoin.class, event -> {
            PlayerData data = PlayerData.getData(event.player.uuid());
            if (data != null) data.handlePlayerJoin(event.player);
            else PlayerData.datas.add(new PlayerData(event.player));
        });

        Events.on(BlockDestroyEvent.class, event -> {
            if (event.tile.block() instanceof CoreBlock && event.tile.team().cores().size <= 1) {
                Events.fire(new GameOverEvent(event.tile.team() == Team.sharded ? Team.blue : Team.sharded));
            }
        });

        Events.on(UnitDestroyEvent.class, event -> {
            int income = CastleCosts.drop(event.unit.type);
            if (income <= 0 || event.unit.spawnedByCore) return;
            PlayerData.datas.each(data -> {
                if (data.player.team() != event.unit.team) {
                    data.money += income;
                    Call.label(data.player.con, "[lime]+[accent] " + income, 2f, event.unit.x, event.unit.y);
                }
            });
        });

        Events.on(ResetEvent.class, event -> {
            CastleRooms.rooms.clear();
            CastleRooms.spawns.clear();
            PlayerData.datas.filter(data -> data.player.con.isConnected());
            PlayerData.datas.each(PlayerData::reset);
        });

        Events.on(PlayEvent.class, event -> CastleUtils.applyRules(state.rules));

        Events.on(WorldLoadEvent.class, event -> CastleUtils.timer = roundTime);

        Events.run(Trigger.update, () -> {
            if (isBreak() || state.serverPaused) return;

            Groups.unit.each(unit -> unit.isFlying() && !unit.spawnedByCore && (unit.floorOn() == null || unit.floorOn() == Blocks.space), unit -> {
                Call.effect(Fx.unitEnvKill, unit.x, unit.y, 0f, Color.scarlet);
                Call.unitDespawn(unit);
            });

            PlayerData.datas.each(PlayerData::update);
            CastleRooms.rooms.each(Room::update);

            if (interval.get(60f)) {
                PlayerData.datas.each(PlayerData::updateMoney);
                CastleRooms.spawns.each((team, spawns) -> spawns.each(spawn -> {
                    for (int deg = 0; deg < 36; deg++) {
                        float x = spawn.worldx() + Mathf.cosDeg(deg * 10) * state.rules.dropZoneRadius;
                        float y = spawn.worldy() + Mathf.sinDeg(deg * 10) * state.rules.dropZoneRadius;
                        Call.effect(Fx.mineSmall, x, y, 0f, team.color);
                    }
                }));

                if (--timer <= 0) Events.fire(new GameOverEvent(Team.derelict));
            }
        });
    }
}
