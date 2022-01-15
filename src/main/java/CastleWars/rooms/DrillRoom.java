package CastleWars.rooms;

import arc.util.Interval;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.Team;

import static mindustry.Vars.world;

public class DrillRoom extends TurretRoom {

    public float updateTime = 60 * 4f;
    public int amount = 24;
    public Interval interval = new Interval();

    public DrillRoom(Team team, int x, int y) { super(team, Blocks.laserDrill, x, y, 1000, 4); }

    @Override
    public void update() {
        if (bought && world.tile(centrex, centrey).build == null) {
            world.tile(centrex, centrey).setNet(Blocks.laserDrill, team, 0);
        }

        if (bought && interval.get(0, updateTime) && team.core() != null) {
            team.core().items.add(Items.thorium, amount);
        }
    }
}