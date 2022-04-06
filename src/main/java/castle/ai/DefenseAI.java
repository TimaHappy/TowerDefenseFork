package castle.ai;

import mindustry.entities.units.AIController;

public class DefenseAI extends AIController {

    @Override
    public void updateUnit() {
        if (target == null) {
            target = target(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);
            if (invalid(target)) target = null;

            if (unit.isFlying()) circle(unit.closestCore(), 30f);
            else moveTo(unit.closestCore(), 30f);
        } else {
            moveTo(target, unit.range());
            unit.aimLook(target);
            unit.controlWeapons(unit.within(target, unit.range()));
        }
    }
}