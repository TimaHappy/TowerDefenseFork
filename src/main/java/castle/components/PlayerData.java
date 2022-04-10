package castle.components;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Strings;
import castle.CastleRooms;
import castle.CastleRooms.UnitRoom;
import mindustry.entities.Units;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import java.util.Locale;

import static castle.CastleLogic.timer;

public class PlayerData {

    public static ObjectMap<String, PlayerData> datas = new ObjectMap<>();

    public Player player;
    public Interval interval;

    public int money = 0;
    public int income = 15;
    public float bonus = 1f;

    public boolean hideHud;
    public Locale locale;

    public static Seq<PlayerData> datas() {
        return Seq.with(datas.values());
    }

    public PlayerData(Player player) {
        this.player = player;
        this.interval = new Interval(2);

        this.locale = Bundle.findLocale(player);
    }

    public void update() {
        if (!player.con.isConnected() || !player.team().active()) return;

        if (interval.get(0, 60f)) {
            bonus = Math.max((float) Groups.player.count(p -> p.team() == player.team()) / Groups.player.size(), 1f);
            money += Mathf.floor(income * bonus);

            CastleRooms.rooms.each(room -> !(room instanceof UnitRoom) && room.showLabel(this), room -> Call.label(player.con, room.label, 1f, room.getX(), room.getY()));
        }

        if (interval.get(1, 300f)) {
            CastleRooms.rooms.each(room -> room instanceof UnitRoom && room.showLabel(this), room -> Call.label(player.con, room.label, 5f, room.getX(), room.getY()));
        }

        if (player.shooting) CastleRooms.rooms.each(room -> room.check(player.mouseX, player.mouseY) && room.canBuy(this), room -> room.buy(this));

        if (hideHud) return;

        StringBuilder hud = new StringBuilder(Bundle.format("ui.hud.balance", locale, money, income));

        if (bonus > 1f) hud.append(Strings.format(" [lightgray]([accent]+@%[])", (int) (bonus - 1) * 100));
        if (Units.getCap(player.team()) <= player.team().data().unitCount) hud.append(Bundle.format("ui.hud.unit-limit", locale, player.team().data().unitCap));

        hud.append(Bundle.format("ui.hud.timer", locale, timer));
        Call.setHudText(player.con, hud.toString());
    }
}
