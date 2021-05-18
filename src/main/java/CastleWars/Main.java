package CastleWars;

import CastleWars.data.Icon;
import arc.util.CommandHandler;
import arc.util.Time;
import mindustry.mod.Plugin;
import mindustry.gen.Player;
import CastleWars.data.PlayerData;
import CastleWars.data.UnitDeathData;
import CastleWars.game.Logic;
import arc.Events;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.game.Team;
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
            Vars.netServer.openServer();
        });

        Events.run(EventType.Trigger.update, () -> logic.update());
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("label", "Show shops label | use when labels hidden", (args, player) -> PlayerData.labels(player));
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("restart", "start new game", (args) -> logic.restart());
    }
}
