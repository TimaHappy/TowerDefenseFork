package CastleWars;

import CastleWars.data.Icon;
import CastleWars.data.PlayerData;
import CastleWars.data.UnitDeathData;
import CastleWars.game.Logic;
import arc.Events;
import arc.math.geom.Geometry;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.netServer;

public class Main extends Plugin {
    public static Rules rules;
    public static Logic logic;

    @Override
    public void init() {
        rules = new Rules();
        rules.pvp = true;
        rules.canGameOver = false;
        rules.teams.get(Team.sharded).cheat = true;
        rules.teams.get(Team.blue).cheat = true;
        rules.waves = false;
        rules.waveTimer = false;
        rules.revealedBlocks.addAll(Blocks.duct, Blocks.ductRouter, Blocks.ductBridge, Blocks.thruster, Blocks.scrapWall, Blocks.scrapWallLarge, Blocks.scrapWallHuge, Blocks.scrapWallGigantic);

        Vars.content.blocks().each(block -> !(block instanceof Wall), block -> {
            rules.bannedBlocks.add(block);
            if (block instanceof CoreBlock) block.unitCapModifier = 999999;
            else block.health *= 100;
        });

        UnitTypes.omura.abilities.clear();
        UnitTypes.corvus.abilities.clear();
        UnitTypes.arkyid.abilities.clear();
        UnitTypes.aegires.abilities.clear();
        Blocks.itemSource.health = Integer.MAX_VALUE;
        Blocks.liquidSource.health = Integer.MAX_VALUE;

        netServer.admins.addActionFilter(action -> {
            if ((action.type != Administration.ActionType.placeBlock && action.type != Administration.ActionType.breakBlock) || action.tile == null) return true;
            if (action.tile.floor() == Blocks.metalFloor.asFloor() || action.tile.floor() == Blocks.metalFloor5.asFloor() || action.tile.floor() == Blocks.darkPanel2.asFloor()) return false;

            if (!logic.placeCheck(action.player.team(), action.tile)) return false;

            boolean[] nearbyPanels = {true};
            Geometry.circle(action.tile.x, action.tile.y, 10, (x, y) -> {
                Tile t = Vars.world.tile(x, y);
                if (t != null && t.floor() == Blocks.darkPanel2.asFloor()) nearbyPanels[0] = false;
            });

            return nearbyPanels[0];
        });

        UnitDeathData.init();
        PlayerData.init();
        Icon.load();

        logic = new Logic();

        Events.on(EventType.ServerLoadEvent.class, e -> {
            logic.restart();
            Log.info("[Darkdustry] Castle Wars loaded. Hosting a server...");
            netServer.openServer();
        });

        Timer.schedule(() -> logic.update(), 0f, 0.01f);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("hud", "Disable/Enable hud.", (args, player) -> {
            PlayerData data = PlayerData.datas.get(player.id);
            data.disabledHud = !data.disabledHud;
            Bundle.bundled(player, data.disabledHud ? "commands.hud.off" : "commands.hud.on");
            if (data.disabledHud) Call.hideHudText(player.con);
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("gameover");
        handler.removeCommand("rules");
        handler.register("gameover", "End the game.", args -> logic.endGame(Team.sharded));
    }
}
