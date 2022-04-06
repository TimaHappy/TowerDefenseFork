package castle.components;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Strings;
import castle.CastleRooms;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.entities.Units;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

import java.util.Locale;

import static castle.CastleLogic.timer;

public class PlayerData {

    public static ObjectMap<String, PlayerData> datas = new ObjectMap<>();

    public Player player;
    public Interval interval;

    public int money;
    public int income;
    public float bonus;

    public boolean hideHud;
    public Locale locale;

    public PlayerData(Player player) {
        this.player = player;
        this.interval = new Interval(2);

        this.income = 15;
        this.bonus = 1f;

        this.locale = Bundle.findLocale(player);
    }

    public static Seq<PlayerData> datas() {
        return datas.values().toSeq();
    }

    public void update() {

        // TODO странная формула для бонуса, улушчить?
        if (interval.get(0, 60f)) {
            bonus = Math.max((float) Groups.player.size() / Groups.player.count(p -> p.team() == player.team()) / 2f, 1f);
            money += Mathf.floor(income * bonus);
        }

        if (!player.con.isConnected()) return;

        if (interval.get(1, 150f)) {
            CastleRooms.rooms.each(room -> room.showLabel(this), room -> Call.label(player.con, room.label, 2.5f, room.getX(), room.getY()));
        }

        // TODO еще большая хуйня
        if (!player.dead() && player.team().core() != null) {
            if (player.shooting) CastleRooms.rooms.each(room -> room.check(player.unit().aimX, player.unit().aimY) && room.canBuy(this), room -> room.buy(this));

            CoreBuild core = player.team().core();
            UnitType type = core.block == Blocks.coreNucleus ? UnitTypes.cyerce : UnitTypes.retusa;

            // TODO самая большая хуйня
            if (player.unit().type != type && player.unit().spawnedByCore) {
                Unit unit = type.spawn(player.team(), core.x + 40, core.y + Mathf.random(-40, 40));
                player.unit(unit);
                unit.spawnedByCore(true);
            }
        }

        if (hideHud) return;

        StringBuilder hud = new StringBuilder(Bundle.format("ui.hud.balance", locale, money, income));

        if (bonus > 1f) hud.append(Strings.format(" [lightgray]([accent]+@%[])", (int) (bonus - 1) * 100));
        if (Units.getCap(player.team()) <= player.team().data().unitCount) hud.append(Bundle.format("ui.hud.unit-limit", locale, player.team().data().unitCap));

        hud.append(Bundle.format("ui.hud.timer", locale, timer));
        Call.setHudText(player.con, hud.toString());
    }
}
