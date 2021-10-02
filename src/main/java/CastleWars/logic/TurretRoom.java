package CastleWars.logic;

import CastleWars.Bundle;
import CastleWars.data.Icon;
import CastleWars.data.PlayerData;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;

import static mindustry.content.Blocks.*;

public class TurretRoom extends Room {

    public boolean bought = false;
    public Team team;
    Interval interval = new Interval(1);
    float updateTime = 60f * 10f;
    Block block;

    public TurretRoom(Team team, Block block, int x, int y, int cost, int size) {
        super(x, y, cost, size);
        this.team = team;
        this.block = block;

        label = Icon.get(block) + " :[white] " + cost;
    }

    @Override
    public void buy(PlayerData data) {
        data.money -= cost;
        bought = true;
        labelVisible = false;
        Groups.player.each(p -> Call.label(p.con(), Bundle.format("events.buy", Bundle.findLocale(p), data.player.name), 5f, centreDrawx, centreDrawy));
        Vars.world.tile(centrex, centrey).setNet(block, team, 0);
        if (block instanceof ItemTurret) {
            Vars.world.tile(x, centrey).setNet(Blocks.itemSource, team, 0);
            Vars.world.tile(x, centrey).build.configure(ammo(block));
        } else if (block instanceof LaserTurret) {
            Vars.world.tile(x, centrey).setNet(Blocks.liquidSource, team, 0);
            Vars.world.tile(x, centrey).build.configure(Liquids.cryofluid);
        }
    }

    @Override
    public boolean canBuy(PlayerData data) {
        return super.canBuy(data) && !(bought = Vars.world.build(centrex, centrey) != null);
    }

    @Override
    public void update() {
        if (bought && interval.get(0, updateTime)) {
            if (Vars.world.tile(centrex, centrey).build == null) Vars.world.tile(centrex, centrey).setNet(block, team, 0);
        }
    }

    public static Item ammo(Block block) {
        if (block == foreshadow || block == swarmer) return Items.surgeAlloy;
        if (block == cyclone || block == ripple) return Items.plastanium;
        if (block == spectre || block == fuse || block == salvo) return Items.thorium;
        return Items.copper;
    }
}
