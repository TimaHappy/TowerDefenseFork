package CastleWars;

import CastleWars.data.Icon;
import CastleWars.data.PlayerData;
import CastleWars.data.UnitDeathData;
import CastleWars.game.Logic;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.ui.Menus;
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import static CastleWars.Bundle.findLocale;

public class Main extends Plugin {

    public static Rules rules;
    public Logic logic;
    public static int unitTeamLimit = 250;

    @Override
    public void init() {
        rules = new Rules();
        rules.pvp = true;
        rules.canGameOver = false;
        rules.teams.get(Team.sharded).cheat = true;
        rules.teams.get(Team.blue).cheat = true;
        rules.waves = true;
        rules.waveTimer = false;
        rules.waveSpacing = 30 * Time.toMinutes;

        for (Block block : Vars.content.blocks()) {
            if (block == Blocks.thoriumWall || block == Blocks.thoriumWallLarge || block == Blocks.plastaniumWall || block == Blocks.plastaniumWallLarge || block == Blocks.phaseWall || block == Blocks.phaseWallLarge) continue;
            rules.bannedBlocks.add(block);
        }

        UnitDeathData.init();
        PlayerData.init();
        Icon.load();

        logic = new Logic();

        Events.on(EventType.ServerLoadEvent.class, e -> {
            Vars.content.blocks().each(block -> {
                if (block != null && !(block instanceof CoreBlock)) block.health *= 25;
            });

            logic.restart();
            Log.info("Castle Wars loaded.");
            Vars.netServer.openServer();
        });

        Events.run(EventType.Trigger.update, () -> logic.update());

        Menus.registerMenu(1, (player, selection) -> {
            if (selection == 0) {
                String[][] options = {{Bundle.format("server.tutorial.yes", findLocale(player))}, {Bundle.format("server.tutorial.no", findLocale(player))}};
                Call.menu(player.con, 2, Bundle.format("server.tutorial-1.header", findLocale(player)), Bundle.format("server.tutorial-1.content", findLocale(player)), options);
            }
        });

        Menus.registerMenu(2, (player, selection) -> {
            if (selection == 0) {
                String[][] options = {{Bundle.format("server.tutorial.yes", findLocale(player))}, {Bundle.format("server.tutorial.no", findLocale(player))}};
                Call.menu(player.con, 3, Bundle.format("server.tutorial-2.header", findLocale(player)), Bundle.format("server.tutorial-2.content", findLocale(player)), options);
            }
        });

        Menus.registerMenu(3, (player, selection) -> {
            if (selection == 0) {
                String[][] options = {{Bundle.format("server.tutorial.yes", findLocale(player))}, {Bundle.format("server.tutorial.no", findLocale(player))}};
                Call.menu(player.con, 4, Bundle.format("server.tutorial-3.header", findLocale(player)), Bundle.format("server.tutorial-3.content", findLocale(player)), options);
            }
        });

        Menus.registerMenu(4, (player, selection) -> {
            if (selection == 0) {
                String[][] options = {{Bundle.format("server.tutorial.yes", findLocale(player))}, {Bundle.format("server.tutorial.no", findLocale(player))}};
                Call.menu(player.con, 5, Bundle.format("server.tutorial-4.header", findLocale(player)), Bundle.format("server.tutorial-4.content", findLocale(player)), options);
            }
        });

        Menus.registerMenu(5, (player, selection) -> {
            if (selection == 0) {
                String[][] options = {{Bundle.format("server.tutorial.yes", findLocale(player))}, {Bundle.format("server.tutorial.no", findLocale(player))}};
                Call.menu(player.con, 6, Bundle.format("server.tutorial-5.header", findLocale(player)), Bundle.format("server.tutorial-5.content", findLocale(player)), options);
            }
        });

        Menus.registerMenu(6, (player, selection) -> {
            if (selection == 0) {
                String[][] optionFinal = {{Bundle.format("server.tutorial-final", findLocale(player))}};
                Call.menu(player.con, 7, Bundle.format("server.tutorial-6.header", findLocale(player)), Bundle.format("server.tutorial-6.content", findLocale(player)), optionFinal);
            }
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("label", "Show shops label. Use when labels are hidden. Debug only.", (args, player) -> PlayerData.labels(player));

        handler.<Player>register("hud", "Disable/Enable hud.", (args, player) -> {
            PlayerData data = PlayerData.datas.get(player.id);
            if (data.disabledHud) {
                data.disabledHud = false;
                Bundle.bundled(player, "commands.hud.on");
                return;
            }
            data.disabledHud = true;
            Call.setHudText(player.con, "");
            Bundle.bundled(player,"commands.hud.off");
        });

        handler.<Player>register("info", "Information about gamemode.", (args, player) -> Call.menuChoose(player, 1, 0));
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("restart", "Перезапустить раунд.", (args) -> logic.restart());
    }
}
