package castle;

import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Strings;
import arc.util.Time;
import castle.components.Bundle;
import castle.components.CastleCosts;
import castle.components.CastleIcons;
import castle.components.PlayerData;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.WorldLabel;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.ItemTurret.ItemTurretBuild;
import mindustry.world.blocks.defense.turrets.LiquidTurret.LiquidTurretBuild;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.storage.CoreBlock;

import static castle.CastleLogic.spawnUnit;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CastleRooms {

    public static Seq<Room> rooms = new Seq<>();

    public static final int size = 10;
    public static Tile shardedSpawn, blueSpawn;

    public static class Room implements Position {
        public int x;
        public int y;

        public int startx;
        public int starty;
        public int endx;
        public int endy;

        public int cost;
        public int size;

        public float offset;
        public Tile tile;

        public WorldLabel label = WorldLabel.create();

        public Room(int x, int y, int cost, int size) {
            this.x = x;
            this.y = y;

            this.startx = x - size / 2;
            this.starty = y - size / 2;
            this.endx = x + size / 2 + size % 2;
            this.endy = y + size / 2 + size % 2;

            this.cost = cost;
            this.size = size;
            this.offset = size % 2 == 0 ? 0f : 4f;
            this.tile = world.tile(x, y);

            this.label.set(getX(), getY());
            this.label.fontSize(1.75f);
            this.label.flags(WorldLabel.flagOutline);
            this.label.add();

            this.spawn();
            rooms.add(this);
        }

        public void update() {}

        public void buy(PlayerData data) {
            data.money -= cost;
        }

        public boolean canBuy(PlayerData data) {
            return data.money >= cost;
        }

        public boolean check(float x, float y) {
            return x > startx * tilesize && y > starty * tilesize && x < endx * tilesize && y < endy * tilesize;
        }

        public float getX() {
            return x * tilesize + offset;
        }

        public float getY() {
            return y * tilesize + offset;
        }

        public void spawn() {
            for (int x = startx; x <= endx; x++) for (int y = starty; y <= endy; y++) {
                Block floor = x == startx || y == starty || x == endx || y == endy ? Blocks.metalFloor5 : Blocks.metalFloor;
                world.tile(x, y).setFloor(floor.asFloor());
            }
        }
    }

    public static class BlockRoom extends Room {
        public Block block;
        public Team team;

        public boolean bought;

        public BlockRoom(Block block, Team team, int x, int y, int cost, int size) {
            super(x, y, cost, size);

            this.block = block;
            this.team = team;
            this.label.text(CastleIcons.get(block) + " :[white] " + cost);
        }

        public BlockRoom(Block block, Team team, int x, int y, int cost) {
            this(block, team, x, y, cost, block.size + 1);
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);
            label.hide();
            bought = true;

            tile.setNet(block, team, 0);
            if (!(block instanceof CoreBlock)) tile.build.health(Float.MAX_VALUE);

            Groups.player.each(p -> Call.label(p.con, Bundle.format("events.buy", Bundle.findLocale(p), data.player.coloredName()), 1f, getX(), getY()));
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && data.player.team() == team && !bought;
        }
    }

    public static class TurretRoom extends BlockRoom {
        public TurretRoom(Turret block, Team team, int x, int y) {
            super(block, team, x, y, CastleCosts.turrets.get(block));
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);

            Tile source = world.tile(startx, y);
            if (tile.build instanceof ItemTurretBuild build) {
                source.setNet(Blocks.itemSource, team, 0);
                source.build.configure(((ItemTurret) build.block).ammoTypes.entries().next().key);
            } else if (tile.build instanceof LiquidTurretBuild || tile.block() == Blocks.meltdown) {
                source.setNet(Blocks.liquidSource, team, 0);
                source.build.configure(Liquids.cryofluid);
            } else return;

            source.build.health(Float.MAX_VALUE);
            Time.run(60f, () -> {
                Call.effect(Fx.mineHuge, source.worldx(), source.worldy(), 0, team.color);
                source.removeNet();
            });
        }
    }

    public static class MinerRoom extends BlockRoom {
        public Item item;
        public int amount;
        public Interval interval = new Interval();

        public MinerRoom(Item item, Team team, int x, int y) {
            super(Blocks.laserDrill, team, x, y, CastleCosts.items.get(item));

            this.item = item;
            this.amount = (int) (300f - item.cost * 150f);

            this.label.text("[" + CastleIcons.get(item) + "] : " + cost);
        }

        @Override
        public void update() {
            if (bought && interval.get(300f)) {
                float randomX = getX() + Mathf.range(12f), randomY = getY() + Mathf.range(12f);
                Call.effect(Fx.mineHuge, randomX, randomY, 0f, team.color);
                Call.transferItemTo(null, item, amount, randomX, randomY, team.core());
            }
        }
    }

    public static class UnitRoom extends Room {
        public enum UnitRoomType {
            attack, defend
        }

        public UnitType unitType;
        public UnitRoomType roomType;

        public int income;

        public UnitRoom(UnitType unitType, UnitRoomType roomType, int income, int x, int y, int cost) {
            super(x, y, cost, 4);

            this.unitType = unitType;
            this.roomType = roomType;
            this.income = income;

            this.label.set(getX(), getY() + 12f);
            this.label.fontSize(2.25f);
            this.label.text(" ".repeat(Math.max(0, (String.valueOf(income).length() + String.valueOf(cost).length() + 2) / 2)) +
                    CastleIcons.get(unitType) + (roomType == UnitRoomType.attack ? " [accent]\uE865" : " [scarlet]\uE84D") +
                    "\n[gray]" + cost +
                    "\n[white]" + Iconc.blockPlastaniumCompressor + " : " + (income < 0 ? "[crimson]" : income > 0 ? "[lime]+" : "[gray]") + income);
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);
            data.income += income;

            Tile tile;

            if (roomType == UnitRoomType.attack) {
                tile = data.player.team() == Team.sharded ? blueSpawn : shardedSpawn;
                spawnUnit(unitType, data.player.team(), tile.worldx() + Mathf.range(40), tile.worldy() + Mathf.range(40));
            } else if (data.player.team().core() != null) {
                tile = data.player.team().core().tile;
                spawnUnit(unitType, data.player.team(), tile.worldx() + 40, tile.worldy() + Mathf.range(40));
            }
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && Units.getCap(data.player.team()) > data.player.team().data().unitCount;
        }
    }

    public static class EffectRoom extends Room {
        public StatusEffect effect;
        public Interval interval = new Interval();

        public EffectRoom(StatusEffect effect, int x, int y, int cost) {
            super(x, y, cost, 4);

            this.effect = effect;

            this.label.set(getX(), getY() + 12f);
            this.label.fontSize(2.25f);
            this.label.text("[accent]" + Strings.capitalize(effect.name) + "\n" + "effect" + "\n[white]" + CastleIcons.get(effect) + " : [gray]" + cost);
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);
            Groups.unit.each(unit -> unit.team == data.player.team(), unit -> unit.apply(effect, Float.POSITIVE_INFINITY));
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && interval.get(60f);
        }
    }
}
