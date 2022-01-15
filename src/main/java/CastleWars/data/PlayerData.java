package CastleWars.data;

import CastleWars.game.Logic;
import CastleWars.rooms.Room;
import CastleWars.rooms.TurretRoom;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Strings;
import mindustry.content.UnitTypes;
import mindustry.gen.*;

import static CastleWars.Bundle.findLocale;
import static CastleWars.Bundle.format;

public class PlayerData {

    public static final int defaultIncome = 15;

    public static final IntMap<PlayerData> datas = new IntMap<>();
    public static final float MoneyInterval = 60f;
    public static final float LabelInterval = 3 * 60f;

    public Player player;
    public Interval interval;
    public boolean disabledHud = false;
    public int money = 0, income = defaultIncome;
    public float bonus = 1f;

    public PlayerData(Player player) {
        this.player = player;
        this.interval = new Interval(2);
    }

    public void update() {
        bonus = Math.max((float) Groups.player.size() / Groups.player.count(p -> p.team() == player.team()) / 2f, 1f);
        if (interval.get(0, MoneyInterval)) money += income * bonus;
        if (interval.get(1, LabelInterval)) labels();

        if (player.shooting && player.unit() != null) {
            Room.rooms.each(room -> !(room instanceof TurretRoom turretRoom && turretRoom.team != player.team()), room -> {
                if (room.check(player.unit().aimX, player.unit().aimY) && room.canBuy(this)) room.buy(this);
            });
        }

        if (player.unit().spawnedByCore && !(player.unit() instanceof WaterMovec) && player.team().core() != null) {
            Unit unit = UnitTypes.risso.spawn(player.team(), player.team().core().x + 30, player.team().core().y + Mathf.random(-40, 40));
            player.unit(unit);
            unit.spawnedByCore = true;
        }

        if (!disabledHud) {
            StringBuilder hud = new StringBuilder(format("commands.hud.display", findLocale(player), money, income));
            if (bonus > 1f) hud.append(Strings.format(" [lightgray]([accent]+@%[lightgray])", String.valueOf((bonus - 1) * 100).length() > 5 ? String.valueOf((bonus - 1) * 100).substring(0, 6) : (bonus - 1) * 100));
            if (!player.dead() && player.unit().isFlying() && !Logic.placeCheck(player)) hud.append(format("commands.hud.fly-warning", findLocale(player)));
            Call.setHudText(player.con, hud.toString());
        }
    }

    public void labels() {
        Room.rooms.each(room -> !(room instanceof TurretRoom turretRoom && turretRoom.team != player.team()) && room.labelVisible, room -> Call.label(player.con, room.label, LabelInterval / 60f, room.centreDrawx, room.centreDrawy - room.size * 2));
    }

    public void reset() {
        income = defaultIncome;
        money = 0;
        bonus = 1f;
    }

    public static Seq<PlayerData> allDatas() {
        return datas.values().toArray();
    }
}
