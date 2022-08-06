package castle.components;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import castle.CastleRooms;
import mindustry.entities.Units;
import mindustry.gen.Call;
import mindustry.gen.Player;

import java.util.Locale;

import static castle.CastleUtils.timer;
import static castle.Main.findLocale;

public class PlayerData {

    public static ObjectMap<String, PlayerData> datas = new ObjectMap<>();

    public Player player;

    public int money = 0;
    public int income = 15;

    public Locale locale;

    public PlayerData(Player player) {
        handlePlayerJoin(player);
    }

    public static Seq<PlayerData> datas() {
        return datas.values().toSeq();
    }

    public void update() {
        if (!player.con.isConnected()) return;

        if (player.shooting)
            CastleRooms.rooms.each(room -> room.check(player.mouseX, player.mouseY) && room.canBuy(this), room -> room.buy(this));

        StringBuilder hud = new StringBuilder(Bundle.format("ui.hud.balance", locale, money, income));

        if (getBonus() > 1f)
            hud.append(Strings.format(" [lightgray](x@)", Strings.autoFixed(getBonus(), 4)));

        if (Units.getCap(player.team()) <= player.team().data().unitCount)
            hud.append(Bundle.format("ui.hud.unit-limit", locale, Units.getCap(player.team())));

        hud.append(Bundle.format("ui.hud.timer", locale, timer));
        Call.setHudText(player.con, hud.toString());
    }

    public void updateMoney() {
        if (!player.con.isConnected()) return;

        money += income * getBonus();
    }

    public void handlePlayerJoin(Player player) {
        this.player = player;
        this.locale = findLocale(player);
    }

    public float getBonus() {
        return Mathf.clamp((float) datas().size / datas().count(data -> data.player.team() == player.team()) / 2f, 1f, 5f);
    }
}
