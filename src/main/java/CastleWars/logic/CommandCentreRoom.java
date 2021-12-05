package CastleWars.logic;

import mindustry.content.Blocks;
import mindustry.game.Team;

public class CommandCentreRoom extends TurretRoom {
    public CommandCentreRoom(Team team, int x, int y) {
        super(team, Blocks.commandCenter, x, y, 750, 3);
    }
}
