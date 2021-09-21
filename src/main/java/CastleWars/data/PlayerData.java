package CastleWars.data;

import static CastleWars.Bundle.findLocale;
import static CastleWars.Bundle.format;
import CastleWars.logic.Room;
import CastleWars.logic.TurretRoom;
import arc.Events;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.util.Interval;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.gen.*;

public class PlayerData {

    public static IntMap<PlayerData> datas = new IntMap<>();
    public static float MoneyInterval = 60f;
    public static float LabelInterval = 60f * 30f;

    public Player player;
    public boolean disabledHud = false;
    public int money, income = 10;
    Interval interval;

    public PlayerData(Player player) {
        this.player = player;
        interval = new Interval(2);
    }

    public void update() {
        // Income Math
        if (interval.get(0, MoneyInterval)) money += income;
        if (interval.get(1, LabelInterval)) labels(player);
        // For Room Rect
        if (player.shooting && player.unit() != null) {
            for (Room room : Room.rooms) {
                if (room instanceof TurretRoom && !(((TurretRoom)room).team == player.team())) {
                    continue;
                }
                if (room.check(player.unit().aimX, player.unit().aimY) && room.canBuy(this)) {
                    room.buy(this);
                }
            }
        }
        // Set Unit to risso
        if (player.unit().spawnedByCore && !(player.unit() instanceof WaterMovec)) {
            if (player.team().core() != null) {
                Unit u = UnitTypes.risso.spawn(player.team(), player.team().core().x + 30, player.team().core().y + Mathf.random(-40, 40));
                u.spawnedByCore = true;
                player.unit(u);
            }
        }

        // Set Hud Text
        if (!disabledHud) Call.setHudText(player.con, format("commands.hud.display", findLocale(player), money, income));
    }

    public static void init() {
        Events.on(EventType.PlayerJoin.class, event -> {
            datas.put(event.player.id, new PlayerData(event.player));
            Vars.netServer.assignTeam(event.player, Groups.player);
            if (Groups.player.size() >= 1) {
                if (Groups.player.size() == 1) labels(Groups.player.index(0));
                Timer.schedule(() -> labels(event.player), 1);
            }
            if (event.player.getInfo().timesJoined <= 1) {
                String[][] optionsFirst = {{format("server.first-join.yes", findLocale(event.player))}, {format("server.first-join.no", findLocale(event.player))}};
                Call.menu(event.player.con, 1, format("server.first-join.header", findLocale(event.player)), format("server.first-join.content", findLocale(event.player)), optionsFirst);
            }
        });

        Events.on(EventType.PlayerLeave.class, event -> datas.remove(event.player.id));
    }

    public static void labels(Player player) {
        for (Room room : Room.rooms) {
            if (room instanceof TurretRoom && ((TurretRoom)room).team != player.team()) continue;
            if (room.labelVisible) {
                Call.label(player.con, room.label, LabelInterval / 60f, room.centreDrawx, room.centreDrawy - room.size * 8 / 4);
            }
        }
    }

    public void reset() {
        this.income = 10;
        this.money = 0;
    }
}
