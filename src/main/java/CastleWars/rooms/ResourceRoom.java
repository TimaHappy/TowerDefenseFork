package CastleWars.rooms;

import CastleWars.data.Icon;
import CastleWars.data.PlayerData;
import mindustry.gen.Call;
import mindustry.gen.Nulls;
import mindustry.type.Item;
import mindustry.content.Items;

public class ResourceRoom extends Room {

    public Item item;
    public int amount;

    public ResourceRoom(Item item, int x, int y, int cost, int amount) {
        super(x, y, cost, 4);
        this.item = item;
        this.amount = amount;
        label = "[white]" + amount + "x" + Icon.get(item) + " [white]: [gray]" + cost;
    }

    @Override
    public void buy(PlayerData data) {
        data.money -= cost;
        if (data.player.team().core() != null) {
            Call.transferItemTo(Nulls.unit, item, amount, centreDrawx, centreDrawy, data.player.team().core());
            if (item == Items.plastanium) {
                Call.transferItemTo(Nulls.unit, Items.metaglass, amount * 2 / 5, centreDrawx, centreDrawy, data.player.team().core());
            }
        }
    }

    @Override
    public void update() {}
}
