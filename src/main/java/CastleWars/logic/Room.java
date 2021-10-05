package CastleWars.logic;

import arc.struct.Seq;
import mindustry.Vars;

public abstract class Room implements RoomComp {

    public static Seq<Room> rooms = new Seq<>();
    public static int ROOM_SIZE = 8;

    public int size;
    public float drawSize;
    public String label = "";

    public int x, y, centrex, centrey, endx, endy;
    public float drawx, drawy, centreDrawx, centreDrawy, endDrawx, endDrawy;

    public boolean labelVisible = true;
    public int cost;

    public Room(int x, int y, int cost, int size) {

        this.cost = cost;
        this.size = size;
        this.drawSize = size * Vars.tilesize;

        this.x = x;
        this.y = y;
        this.centrex = x + size / 2;
        this.centrey = y + size / 2;
        this.endx = x + size;
        this.endy = y + size;
        this.drawx = x * Vars.tilesize;
        this.drawy = y * Vars.tilesize;
        this.centreDrawx = (x + size / 2f) * Vars.tilesize;
        this.centreDrawy = (y + size / 2f) * Vars.tilesize;
        this.endDrawx = drawx + drawSize;
        this.endDrawy = drawy + drawSize;
    }

    @Override
    public int cost() {
        return cost;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public float drawx() {
        return drawx;
    }

    @Override
    public float drawy() {
        return drawy;
    }

    @Override
    public float endDrawx() {
        return endDrawx;
    }

    @Override
    public float endDrawy() {
        return endDrawy;
    }
}
