package castle.ai;

import castle.CastleRooms.BlockRoom;
import mindustry.entities.units.AIController;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

public class DeliverAI extends AIController {

    public static final float range = 40f;

    public BlockRoom room;
    public Item item;

    @Override
    public void updateUnit() {
        CoreBuild core = unit.closestCore();

        if (core == null)
            despawn();
        else if (unit.stack.amount == 0) {
            if (unit.within(core, range)) Call.transferItemToUnit(item, core.x, core.y, unit);
            else moveTo(core, range);
        } else {
            if (unit.within(room, range)) return; // TODO перенос предмета в турель + смерц
            else moveTo(room, range);
        }
    }

    public void despawn() {
        Call.unitDespawn(unit);
    }
}