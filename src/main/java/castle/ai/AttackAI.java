package castle.ai;

import mindustry.ai.Pathfinder;
import mindustry.entities.units.AIController;

public class AttackAI extends AIController {

    @Override
    public void updateUnit() {
        pathfind(Pathfinder.fieldCore);

        if (invalid(target)) {
            target = target(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);
            unit.lookAt(unit.vel().angle());
        } else {
            unit.aimLook(target);
            unit.controlWeapons(unit.within(target, unit.range() * 1.25f));
        }
    }
}
