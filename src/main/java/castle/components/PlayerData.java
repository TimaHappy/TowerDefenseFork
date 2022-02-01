package castle.components;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Strings;
import castle.CastleLogic;
import castle.CastleRooms;
import castle.CastleRooms.BlockRoom;
import mindustry.content.UnitTypes;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;

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

    public void update() {
        if (interval.get(0, 60f)) {
            bonus = Math.max((float) Groups.player.size() / Groups.player.count(p -> p.team() == player.team()) / 2f, 1f);
            money += income;
        }

        if (interval.get(1, 180f)) {
            updateLabels();
        }

        if (!player.dead()) {
            if (player.shooting) {
                CastleRooms.rooms.each(room -> !(room instanceof BlockRoom blockRoom && blockRoom.team != player.team()), room -> {
                    if (room.check(player.unit().aimX, player.unit().aimY) && room.canBuy(this)) {
                        room.buy(this);
                    }
                });
            }

            if (player.unit().spawnedByCore && player.unit().type != UnitTypes.risso && player.team().core() != null) {
                Unit unit = UnitTypes.risso.spawn(player.team(), player.team().core().x + 30, player.team().core().y + Mathf.random(-40, 40));
                player.unit(unit);
                unit.spawnedByCore(true);
            }
        }

        if (showHud) {
            StringBuilder hud = new StringBuilder(Bundle.format("commands.hud.display", Bundle.findLocale(player), money, income));
            if (bonus > 1f) hud.append(Strings.format(" [lightgray]([accent]+@%[lightgray])", String.valueOf((bonus - 1) * 100).length() > 5 ? String.valueOf((bonus - 1) * 100).substring(0, 6) : (bonus - 1) * 100));
            if (!player.dead() && player.unit().isFlying() && !CastleLogic.placeCheck(player)) hud.append(Bundle.format("commands.hud.fly-warning", Bundle.findLocale(player)));
            Call.setHudText(player.con, hud.toString());
        }
    }

    public  void updateLabels() {
        CastleRooms.rooms.each(room -> !(room instanceof BlockRoom blockRoom && blockRoom.team != player.team()) && room.showLabel, room -> Call.label(player.con, room.label, 3f, room.centrex * tilesize, room.centrey * tilesize - room.size * 2));
    }

    public void reset() {
        for (int i = 0; i < interval.getTimes().length; i++) interval.reset(i, 0f);

        money = 0;
        income = 15;
        bonus = 1f;
    }

    public static Seq<PlayerData> datas() {
        return datas.values().toSeq();
    }
}
