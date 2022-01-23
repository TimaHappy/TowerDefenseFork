package CastleWars.rooms;

import CastleWars.data.Icons;
import CastleWars.data.PlayerData;
import arc.math.Mathf;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.UnitSpawnAbility;
import mindustry.game.Team;
import mindustry.gen.Iconc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.Tile;

public class UnitRoom extends Room {

    public static Tile blueSpawn, shardedSpawn;

    public enum Type {
        Attacker, Defender
    }

    public UnitType unit;
    public Type type;
    public int income;

    public UnitRoom(UnitType unit, int x, int y, int cost, int income, Type type) {
        super(x, y, cost, 4);
        this.unit = unit;
        this.type = type;
        this.income = income;

        StringBuilder str = new StringBuilder();
        int len = String.valueOf(income).length() + 2 + String.valueOf(cost).length();

        str.append(" ".repeat(Math.max(0, len / 2)));
        str.append(Icons.get(unit));

        if (type == Type.Attacker) str.append(" [accent]").append(Iconc.modeAttack);
        else str.append(" [scarlet]").append(Iconc.defense);

        str.append("\n[gray]").append(cost).append("\n[white]").append(Iconc.blockPlastaniumCompressor).append(" : ");

        if (income < 0) str.append("[crimson]");
        else if (income > 0) str.append("[lime]+");
        else str.append("[gray]");
        str.append(income);
        label = str.toString();
    }

    @Override
    public void buy(PlayerData data) {
        data.money -= cost;
        data.income += income;

        if (type == Type.Attacker) {
            Unit u = unit.spawn(data.player.team(), (data.player.team() == Team.sharded ? blueSpawn.drawx() : shardedSpawn.drawx()) + Mathf.random(-40, 40), (data.player.team() == Team.sharded ? blueSpawn.drawy() : shardedSpawn.drawy()) + Mathf.random(-40, 40));
            if (u.type == UnitTypes.crawler) {
                u.maxHealth = 400f;
                u.health = u.maxHealth;
                u.armor = 100f;
                u.abilities.add(new UnitSpawnAbility(UnitTypes.crawler, 150f, 0f, -8f));
                u.apply(StatusEffects.boss);
            }
            u.team(data.player.team());
        } else if (data.player.team().core() != null) {
            Unit u = unit.spawn(data.player.team(), data.player.team().core().x + 30, data.player.team().core().y + Mathf.random(-40, 40));
            u.team(data.player.team());
        }
    }

    @Override
    public boolean canBuy(PlayerData data) {
        return super.canBuy(data) && (income > 0 || data.income - income >= 0);
    }

    @Override
    public void update() {}
}
