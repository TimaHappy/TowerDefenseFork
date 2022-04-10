package castle;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Interval;
import castle.CastleRooms.Room;
import castle.ai.CastleAI;
import castle.components.Bundle;
import castle.components.CastleIcons;
import castle.components.CastleUnitDrops;
import castle.components.PlayerData;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType.BlockDestroyEvent;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.UnitDestroyEvent;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration.ActionType;
import mindustry.world.blocks.storage.CoreBlock;

import static castle.CastleLogic.gameOver;
import static castle.CastleLogic.timer;
import static mindustry.Vars.*;

public class Main extends Plugin {

    public static Interval interval = new Interval();

    @Override
    public void init() {
        ((CoreBlock) Blocks.coreShard).unitType = UnitTypes.poly;
        ((CoreBlock) Blocks.coreNucleus).unitType = UnitTypes.mega;

        content.units().each(type -> type.defaultController = CastleAI::new);

        CastleLogic.load();
        CastleIcons.load();
        CastleUnitDrops.load();
        CastleRooms.load();

        netServer.admins.addActionFilter(action -> action.tile == null || action.type != ActionType.placeBlock || (action.tile.dst(CastleRooms.shardedSpawn) > 64 && action.tile.dst(CastleRooms.blueSpawn) > 64));

        Events.on(PlayerJoin.class, event -> {
            PlayerData old = PlayerData.datas.get(event.player.uuid());
            if (old != null) old.handlePlayer(event.player);
            else PlayerData.datas.put(event.player.uuid(), new PlayerData(event.player));
        });

        Events.on(BlockDestroyEvent.class, event -> {
            if (world.isGenerating() || state.gameOver) return;
            if (event.tile.block() instanceof CoreBlock && event.tile.team().cores().size <= 1)
                gameOver(event.tile.team() == Team.sharded ? Team.blue : Team.sharded);
        });

        Events.on(UnitDestroyEvent.class, event -> {
            int income = CastleUnitDrops.get(event.unit.type);
            if (income <= 0 || event.unit.spawnedByCore) return;
            PlayerData.datas().each(data -> data.player.team() != event.unit.team, data -> {
                data.money += income;
                Call.label(data.player.con, "[lime] + [accent]" + income, .5f, event.unit.x, event.unit.y);
            });
        });

        Events.run(Trigger.update, () -> {
            if (world.isGenerating() || state.serverPaused || state.gameOver) return;

            Groups.unit.each(unit -> unit.isFlying() && (unit.tileOn() == null || unit.tileOn().floor() == Blocks.space), Call::unitDespawn);

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
            Bundle.bundled(player, data.hideHud ? "commands.hud.off" : "commands.hud.on");
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
