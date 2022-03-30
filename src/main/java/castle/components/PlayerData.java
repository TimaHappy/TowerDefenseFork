package castle.components;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Strings;
import castle.CastleRooms;
import castle.CastleRooms.BlockRoom;
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
import static mindustry.Vars.tilesize;

public class PlayerData {

    public static ObjectMap<String, PlayerData> datas = new ObjectMap<>();

    public Player player;
    public Interval interval;

    public int money;
    public int income;

    public float bonus;

    public boolean showHud;

    public PlayerData(Player player) {
        this.player = player;
        this.interval = new Interval(2);
        this.money = 0;
        this.income = 15;
        this.bonus = 1f;
        this.showHud = true;
    }

    public static Seq<PlayerData> datas() {
        return datas.values().toSeq();
    }

    public void update() {
        if (interval.get(0, 60f)) {
            bonus = Math.max((float) Groups.player.size() / Groups.player.count(p -> p.team() == player.team()) / 2f, 1f);
            money += Mathf.floor(income * bonus);
        }

        if (interval.get(1, 150f)) {
            CastleRooms.rooms.each(room -> room.showLabel, room -> {
                if ((room instanceof BlockRoom blockRoom && blockRoom.team != player.team())) return;
                Call.label(player.con, room.label, 2.5f, room.x * tilesize, room.y * tilesize - room.size * 2);
            });
        }

        if (!player.dead() && player.team().core() != null) {
            if (player.shooting) {
                CastleRooms.rooms.each(room -> {
                    if (room.check(player.unit().aimX, player.unit().aimY) && room.canBuy(this)) {
                        room.buy(this);
                    }
                });
            }

            CoreBuild core = player.team().core();
            UnitType type = core.block == Blocks.coreNucleus ? UnitTypes.cyerce : UnitTypes.retusa;

            if (player.unit().type != type && player.unit().spawnedByCore) {
                Unit unit = type.spawn(player.team(), core.x + 40, core.y + Mathf.random(-40, 40));
                player.unit(unit);
                unit.spawnedByCore(true);
            }
        }

        if (showHud) {
            Locale locale = Bundle.findLocale(player);
            StringBuilder hud = new StringBuilder(Bundle.format("ui.hud.balance", locale, money, income));
            if (bonus > 1f) {
                hud.append(Strings.format(" [lightgray]([accent]+@%[lightgray])", String.valueOf((bonus - 1) * 100).length() > 5 ? String.valueOf((bonus - 1) * 100).substring(0, 6) : (bonus - 1) * 100));
            }

            if (Units.getCap(player.team()) <= player.team().data().unitCount) {
                hud.append(Bundle.format("ui.hud.unit-limit", locale, player.team().data().unitCap));
            }

            hud.append(Bundle.format("ui.hud.timer", locale, timer));
            Call.setHudText(player.con, hud.toString());
        }
    }

    public void reset() {
        for (int i = 0; i < interval.getTimes().length; i++) interval.reset(i, 0f);

        money = 0;
        income = 15;
        bonus = 1f;
    }
}
