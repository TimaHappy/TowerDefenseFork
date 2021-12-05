package CastleWars.logic;

import CastleWars.data.PlayerData;
import mindustry.content.Blocks;
import mindustry.game.Team;

public class CoreRoom extends TurretRoom{

    public CoreRoom(Team team, int x, int y, int cost) {
        super(team, Blocks.coreNucleus, x, y, cost, 4);
    }
    
    @Override
    public void update() {}

    @Override
    public boolean canBuy(PlayerData data) {
        return data.money >= cost() && !bought;
    }
}
