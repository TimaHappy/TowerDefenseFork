package castle.components;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import castle.CastleRooms;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.entities.Units;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.gen.WaterMovec;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

import java.util.Locale;

import static castle.CastleLogic.spawnUnit;
import static castle.CastleLogic.timer;

public class PlayerData {

    public static ObjectMap<String, PlayerData> datas = new ObjectMap<>();

    public Player player;
    public Interval interval;

    public int money = 0;
    public int income = 15;

    public boolean hideHud = false;
    public Locale locale;

    public static Seq<PlayerData> datas() {
        return datas.values().toSeq();
    }

    public PlayerData(Player player) {
        this.handlePlayerJoin(player);
    }

    public void update() {
        if (!player.con.isConnected()) return;

        if (interval.get(60f)) money += income;

        if (player.shooting) CastleRooms.rooms.each(room -> room.check(player.mouseX, player.mouseY) && room.canBuy(this), room -> room.buy(this));

        if (player.dead() || (player.unit().spawnedByCore && !(player.unit() instanceof WaterMovec))) this.respawn();

        if (hideHud) return;
        StringBuilder hud = new StringBuilder(Bundle.format("ui.hud.balance", locale, money, income));

        if (Units.getCap(player.team()) <= player.team().data().unitCount) hud.append(Bundle.format("ui.hud.unit-limit", locale, Units.getCap(player.team())));

        hud.append(Bundle.format("ui.hud.timer", locale, timer));
        Call.setHudText(player.con, hud.toString());
    }

    public void handlePlayerJoin(Player player) {
        this.player = player;
        this.locale = Bundle.findLocale(player);
        this.interval = new Interval();
    }

    public void respawn() {
        CoreBuild core = player.core();
        if (core == null) return;

        UnitType type = core.block == Blocks.coreShard ? UnitTypes.retusa : UnitTypes.oxynoe;
        Unit unit = spawnUnit(type, player.team(), core.x + 40, core.y + Mathf.range(40));
        unit.spawnedByCore(true);
        player.unit(unit);
    }
}
