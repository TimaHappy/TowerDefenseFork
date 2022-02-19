package castle;

import arc.Events;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Timer;
import castle.CastleRooms.Room;
import castle.components.Bundle;
import castle.components.PlayerData;
import mindustry.content.Blocks;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Flyingc;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.blocks.defense.turrets.PointDefenseTurret;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.CommandCenter;
import mindustry.world.blocks.units.RepairPoint;

import static mindustry.Vars.*;

public class CastleLogic {

    public static void update() {
        if (!world.isGenerating() && !state.serverPaused && !state.gameOver) {
            PlayerData.datas().each(PlayerData::update);
            CastleRooms.rooms.each(Room::update);

            Groups.unit.each(Flyingc::isFlying, unit -> {
                if (unit.tileX() > world.width() || unit.tileX() < 0 || unit.tileY() > world.height() || unit.tileY() < 0 || (unit.tileY() > CastleGenerator.halfHeight && unit.tileY() < world.height() - CastleGenerator.halfHeight - 1)) {
                    Call.unitDespawn(unit);
                }
            });
        }
    }

    public static void restart() {
        Seq<Player> players = new Seq<>();
        Groups.player.each(player -> {
            players.add(player);
            player.clearUnit();
        });

        logic.reset();
        CastleRooms.rooms.clear();
        PlayerData.datas().each(PlayerData::reset);

        CastleGenerator generator = new CastleGenerator();
        generator.run();
        Call.worldDataBegin();

        state.rules = getRules();
        logic.play();

        players.each(player -> netServer.sendWorldData(player));
    }

    public static void gameOver(Team team) {
        Events.fire("CastleGameOver");
        Call.updateGameOver(team);

        Log.info("Игра окончена. Генерирую карту заново...");
        Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format("events.gameover", Bundle.findLocale(p), colorizedTeam(team))));
        Timer.schedule(CastleLogic::restart, 10f);
    }

    public static Rules getRules() {
        Rules rules = new Rules();

        rules.teams.get(Team.sharded).cheat = true;
        rules.teams.get(Team.blue).cheat = true;

        rules.pvp = true;
        rules.canGameOver = false;

        rules.unitCap = 500;
        rules.unitCapVariable = false;

        rules.waves = false;
        rules.waveTimer = false;
        rules.modeName = "Wars";

        content.blocks().each(block -> {
            if (block instanceof CoreBlock || block instanceof Turret || block instanceof PointDefenseTurret || block instanceof TractorBeamTurret || block instanceof CommandCenter || block instanceof RepairPoint || block instanceof LogicBlock || block == Blocks.airFactory) {
                rules.bannedBlocks.add(block);
            }
        });

        return rules;
    }

    public static String colorizedTeam(Team team) {
        return "[#" + team.color + "]" + team.name;
    }
}
