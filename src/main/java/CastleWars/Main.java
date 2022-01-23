package CastleWars;

import CastleWars.data.Icons;
import CastleWars.data.PlayerData;
import CastleWars.data.UnitDeathData;
import CastleWars.game.Logic;
import arc.Events;
import arc.math.geom.Geometry;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Timer;
import mindustry.content.Blocks;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.EnergyFieldAbility;
import mindustry.game.EventType.*;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;

public class Main extends Plugin {

    public static Rules rules;

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
        rules.modeName = "Castle";

        content.blocks().each(block -> !(block instanceof Wall), block -> {
            rules.bannedBlocks.add(block);
            if (block instanceof CoreBlock) block.unitCapModifier = 999999;
            else block.health *= 100;
        });

        Blocks.itemSource.health = Integer.MAX_VALUE;
        Blocks.liquidSource.health = Integer.MAX_VALUE;

        UnitTypes.omura.abilities.clear();
        UnitTypes.corvus.abilities.clear();
        UnitTypes.arkyid.abilities.clear();
        UnitTypes.aegires.armor = 0f;
        UnitTypes.aegires.abilities.each(ability -> {
            if (ability instanceof EnergyFieldAbility fieldAbility) {
                fieldAbility.maxTargets = 3;
                fieldAbility.status = StatusEffects.freezing;
                fieldAbility.statusDuration = 30f;
                fieldAbility.damage = 14f;
            }
        });

        netServer.admins.addActionFilter(action -> {
            if ((action.type == Administration.ActionType.placeBlock || action.type == Administration.ActionType.breakBlock) && action.tile != null) {
                if (action.tile.floor() == Blocks.metalFloor.asFloor() || action.tile.floor() == Blocks.metalFloor5.asFloor() || !Logic.placeCheck(action.player.team(), action.tile)) return false;

                boolean[] nearbyPanels = {true};
                Geometry.circle(action.tile.x, action.tile.y, 10, (x, y) -> {
                    if (world.tile(x, y) != null && world.tile(x, y).floor() == Blocks.darkPanel2.asFloor()) nearbyPanels[0] = false;
                });

                return nearbyPanels[0];
            }

            return true;
        });

        UnitDeathData.load();
        Icons.load();

        Events.on(PlayerJoin.class, event -> {
            PlayerData.datas.put(event.player.id, new PlayerData(event.player));
            netServer.assignTeam(event.player);
        });

        Events.on(PlayerLeave.class, event -> PlayerData.datas.remove(event.player.id));

        Events.on(BlockDestroyEvent.class, event -> {
            if (event.tile.block() instanceof CoreBlock && event.tile.team() != Team.derelict && event.tile.team().cores().size <= 1 && Logic.worldLoaded) {
                Logic.endGame(event.tile.team() == Team.sharded ? Team.blue : Team.sharded);
            }
        });

        Events.on(UnitDestroyEvent.class, event -> {
            int income = UnitDeathData.get(event.unit.type);
            if (income > 0 && !event.unit.spawnedByCore) {
                PlayerData.allDatas().each(data -> data.player.team() != event.unit.team, data -> {
                    data.money += income;
                    Call.label(data.player.con, "[lime] + [accent]" + income, 0.4f, event.unit.x, event.unit.y);
                });
            }
        });

        Events.on(ServerLoadEvent.class, event -> {
            Log.info("[Darkdustry] Castle Wars loaded. Hosting a server...");
            Logic.restart();
            netServer.openServer();
        });

        Timer.schedule(Logic::update, 0f, 0.01f);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("hud", "Toggle HUD.", (args, player) -> {
            PlayerData data = PlayerData.datas.get(player.id);
            data.disabledHud = !data.disabledHud;
            Bundle.bundled(player, data.disabledHud ? "commands.hud.off" : "commands.hud.on");
            if (data.disabledHud) Call.hideHudText(player.con);
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("gameover");
        handler.register("gameover", "End the CastleWars round.", args -> Logic.endGame(Team.sharded));
    }
}
