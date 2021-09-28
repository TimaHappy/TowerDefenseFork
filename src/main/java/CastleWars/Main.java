package CastleWars;

import CastleWars.data.Icon;
import CastleWars.data.PlayerData;
import CastleWars.data.UnitDeathData;
import CastleWars.game.Logic;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.Objects;

import static arc.util.Time.toMinutes;
import static mindustry.Vars.content;
import static mindustry.Vars.netServer;
import static mindustry.game.Team.blue;
import static mindustry.game.Team.sharded;

public class Main extends Plugin {

    public static Rules rules;
    public Logic logic;
    public static int unitTeamLimit = 300;
    public static int moneyPlayerLimit = 250000;

    @Override
    public void init() {
        rules = new Rules();
        rules.pvp = true;
        rules.canGameOver = false;
        rules.teams.get(sharded).cheat = true;
        rules.teams.get(blue).cheat = true;
        rules.waves = true;
        rules.waveTimer = false;
        rules.waveSpacing = 30 * toMinutes;

        content.blocks().each(block -> !(block instanceof Wall), block -> rules.bannedBlocks.add(block));

        netServer.admins.addActionFilter(action -> (action.type != Administration.ActionType.breakBlock && action.type != Administration.ActionType.placeBlock) || action.tile == null || (action.tile.floor() != Blocks.metalFloor.asFloor() && action.tile.floor() != Blocks.metalFloor5.asFloor()));


        UnitDeathData.init();
        PlayerData.init();
        Icon.load();

        logic = new Logic();

        Events.on(EventType.ServerLoadEvent.class, e -> {
            Vars.content.blocks().each(Objects::nonNull, block ->{
                if (block instanceof CoreBlock) block.health *= 2;
                else if (block instanceof Wall) block.health *= 5;
                else block.health *= 25;
            });

            logic.restart();
            Log.info("Castle Wars loaded. Hosting a server...");
            netServer.openServer();
        });

        Events.run(EventType.Trigger.update, () -> logic.update());
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("label", "Show shops label. Use when labels are hidden.", (args, player) -> PlayerData.labels(player));

        handler.<Player>register("hud", "Disable/Enable hud.", (args, player) -> {
            PlayerData data = PlayerData.datas.get(player.id);
            if (data.disabledHud) {
                data.disabledHud = false;
                Bundle.bundled(player, "commands.hud.on");
                return;
            }
            data.disabledHud = true;
            Call.hideHudText(player.con);
            Bundle.bundled(player,"commands.hud.off");
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        // Breaks the game
        handler.removeCommand("gameover");
    }
}
