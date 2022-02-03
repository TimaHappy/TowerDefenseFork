package castle;

import arc.Events;
import arc.struct.Seq;
import arc.util.Timer;
import castle.CastleRooms.Room;
import castle.components.Bundle;
import castle.components.PlayerData;
import mindustry.content.Blocks;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Flyingc;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.CommandCenter;
import mindustry.world.blocks.units.RepairPoint;

import static mindustry.Vars.*;

public class CastleLogic {

    public static float x = 0, y = 0, endx = 0, endy = 0;

    public static void update() {
        if (!world.isGenerating() && !state.serverPaused && !state.gameOver) {
            PlayerData.datas().each(PlayerData::update);
            CastleRooms.rooms.each(Room::update);

            Groups.unit.intersect(x, y, endx, endy, Call::unitDespawn);

            Groups.unit.each(Flyingc::isFlying, unit -> {
                if (!placeCheck(unit.team, unit.tileOn())) {
                    unit.damagePierce(unit.maxHealth / 250f);
                }

                if (unit.tileX() > world.width() || unit.tileX() < 0 || unit.tileY() > world.height() || unit.tileY() < 0) {
                    Call.unitDespawn(unit);
                }
            });
        }
    }

    public static void restart(Map map) {
        Seq<Player> players = new Seq<>();
        Groups.player.each(player -> {
            players.add(player);
            player.clearUnit();
        });

        logic.reset();
        Call.worldDataBegin();

        CastleRooms.rooms.clear();
        PlayerData.datas().each(PlayerData::reset);

        CastleGenerator generator = new CastleGenerator(map);
        generator.run();

        y = (world.height() - CastleRooms.size * 6) / 2f * tilesize;
        endy = CastleRooms.size * 6 * tilesize;
        x = -5 * tilesize;
        endx = (5 + world.width()) * tilesize;

        state.rules = applyRules(map.applyRules(Gamemode.pvp));
        logic.play();

        players.each(player -> netServer.sendWorldData(player));
    }

    public static void gameOver(Team team) {
        Events.fire("CastleGameOver");
        Call.updateGameOver(team);

        Map map = maps.getShuffleMode().next(Gamemode.pvp, state.map);

        Groups.player.each(p -> Call.infoMessage(p.con(), Bundle.format("events.gameover", Bundle.findLocale(p), colorizedTeam(team), map.name())));
        Timer.schedule(() -> restart(map), 10f);
    }

    public static Rules applyRules(Rules rules) {
        rules.teams.get(Team.sharded).cheat = true;
        rules.teams.get(Team.blue).cheat = true;

        rules.pvp = true;
        rules.canGameOver = false;

        rules.unitCap = 1000;
        rules.unitCapVariable = false;

        rules.waves = false;
        rules.waveTimer = false;
        rules.modeName = "Castle";

        content.blocks().each(block -> {
            if (block instanceof CoreBlock || block instanceof Turret || block instanceof CommandCenter || block instanceof RepairPoint || block instanceof LogicBlock || block == Blocks.airFactory) {
                rules.bannedBlocks.add(block);
            }
        });

        return rules;
    }

    public static boolean placeCheck(Team team, Tile tile) {
        return tile == null || (team == Team.sharded ? tile.worldy() < y : tile.worldy() > y + endy);
    }

    public static boolean placeCheck(Player player) {
        return placeCheck(player.team(), player.tileOn());
    }

    public static String colorizedTeam(Team team) {
        return "[#" + team.color + "]" + team.name;
    }
}
