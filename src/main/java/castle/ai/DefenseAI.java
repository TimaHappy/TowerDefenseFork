package castle.ai;

import mindustry.entities.units.AIController;

public class DefenseAI extends AIController {

    @Override
    public void updateUnit() {
        if (invalid(target)) {
            target = target(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);

            if (unit.isFlying()) circle(unit.closestCore(), 30f);
            else moveTo(unit.closestCore(), 30f);
            return;
        }

        moveTo(target, unit.range());
        unit.aimLook(target);
        unit.controlWeapons(unit.within(target, unit.range() * 1.5f));
    }
}