package CastleWars.logic;

public class DrillRoom extends TurretRoom {

    public DrillRoom(int x, int y) {

    }

    @Override
    public void update() {
        if (bought && interval.get(0, 60f) && team.core() != null) {
            team.core().items.add(ItemStack.with(Items.thorium, 8, Items.blastCompound, 4, Items.surgeAlloy, 4, Items.plastanium, 4))
        }
    }
}
