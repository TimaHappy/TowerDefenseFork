package CastleWars;

import CastleWars.data.Icon;
import CastleWars.data.PlayerData;
import CastleWars.data.UnitDeathData;
import CastleWars.game.Logic;
import arc.Events;
import arc.struct.Seq;
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
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

public class Main extends Plugin {

    public static Rules rules;
    public Logic logic;

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
            if (block == Blocks.thoriumWall || block == Blocks.thoriumWallLarge || block == Blocks.plastaniumWall || block == Blocks.plastaniumWallLarge || block == Blocks.phaseWall || block == Blocks.phaseWallLarge) {
                continue;
            }
            rules.bannedBlocks.add(block);
        }

        UnitDeathData.init();
        PlayerData.init();
        Icon.load();

        logic = new Logic();

        Events.on(EventType.ServerLoadEvent.class, e -> {
            Vars.content.blocks().each(block -> {
                if (block != null && !(block instanceof CoreBlock)) block.health *= 10;
            });
            Vars.content.units().each(u -> u.canBoost = false);

            logic.restart();
            Log.info("Castle Wars loaded.");
            Vars.netServer.openServer();
        });

        Events.run(EventType.Trigger.update, () -> logic.update());
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("label", "Show shops label. Use when labels are hidden. Debug only.", (args, player) -> PlayerData.labels(player));

        handler.<Player>register("hud", "Disable/Enable hud.", (args, player) -> {
            PlayerData data = PlayerData.datas.find(d -> d.player == player);
            if (data.disabledHud) {
                data.disabledHud = false;
                Bundle.bundled(player, "commands.hud.on");
                return;
            }
            data.disabledHud = true;
            Call.setHudText(player.con, "");
            Bundle.bundled(player,"commands.hud.off");
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("restart", "Перезапустить раунд.", (args) -> logic.restart());
    }
}
