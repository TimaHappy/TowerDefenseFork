package castle.components;

import arc.math.Mathf;
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

    public static final Seq<PlayerData> data = new Seq<>();

    public Player player;

    public int money = 0;
    public int income = 15;

    public Locale locale;

    public PlayerData(Player player) {
        this.handlePlayerJoin(player);
    }

    public static PlayerData getData(String uuid) {
        return data.find(data -> data.player.uuid().equals(uuid));
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

    public void reset() {
        this.money = 0;
        this.income = 15;
    }

    public float getBonus() {
        return Mathf.clamp((float) data.size / data.count(data -> data.player.team() == player.team()) / 2f, 1f, 5f);
    }
}
