package castle;

import arc.Events;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Timer;
import castle.components.Bundle;
import castle.components.CastleIcons;
import castle.components.CastleUnitDrops;
import castle.components.PlayerData;
import mindustry.content.Blocks;
import mindustry.game.EventType.BlockDestroyEvent;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.UnitDestroyEvent;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Flyingc;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration.ActionType;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;

public class Main extends Plugin {

    @Override
    public void init() {

        CastleLogic.load();
        CastleIcons.load();
        CastleUnitDrops.load();
        CastleRooms.load();

        netServer.admins.addActionFilter(action -> {
            if ((action.type != ActionType.placeBlock && action.type != ActionType.breakBlock) || action.tile == null) return true;

            if (CastleLogic.checkNearby(action.tile, tile -> tile.block() == Blocks.itemSource || tile.block() == Blocks.liquidSource || tile.block() == Blocks.powerSource)) return false;
            return !action.tile.getLinkedTilesAs(action.block, new Seq<>()).contains(tile -> tile.floor() == Blocks.metalFloor || tile.floor() == Blocks.metalFloor5 || tile.overlay() == Blocks.tendrils);
        });

        Events.on(PlayerJoin.class, event -> {
            PlayerData old = PlayerData.datas.get(event.player.uuid());
            if (old != null) {
                event.player.team(old.player.team());
                old.player = event.player;
            } else PlayerData.datas.put(event.player.uuid(), new PlayerData(event.player));
        });

        Events.on(BlockDestroyEvent.class, event -> {
            if (world.isGenerating() || state.gameOver) return;
            if (event.tile.block() instanceof CoreBlock && event.tile.team().cores().size <= 1)
                CastleLogic.gameOver(event.tile.team() == Team.sharded ? Team.blue : Team.sharded);
        });

        Events.on(UnitDestroyEvent.class, event -> {
            int income = CastleUnitDrops.get(event.unit.type);
            if (income > 0 && !event.unit.spawnedByCore) {
                PlayerData.datas().each(data -> data.player.team() != event.unit.team, data -> {
                    data.money += income;
                    Call.label(data.player.con, "[lime] + [accent]" + income, 0.5f, event.unit.x, event.unit.y);
                });
            }
        });

        Events.run(Trigger.update, () -> Groups.unit.each(Flyingc::isFlying, unit -> {
            if (unit.tileY() > CastleLogic.halfHeight && unit.tileY() < world.height() - CastleLogic.halfHeight || unit.tileOn() == null) Call.unitDespawn(unit);
        }));

        Timer.schedule(CastleLogic::update, 0f, 1f);

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

        handler.register("gameover", "End the game.", args -> CastleLogic.gameOver(Team.derelict));
    }
}
