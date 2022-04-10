package castle.ai;

import mindustry.ai.Pathfinder;
import mindustry.entities.units.AIController;

public class CastleAI extends AIController {

    public boolean attacker = true;

    @Override
    public void updateUnit() {
        // TODO сделать определение, является ли юнит атакующим (по его местоположению)

        if (attacker) {
            pathfind(Pathfinder.fieldCore);

            if (invalid(target)) {
                target = target(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);
                unit.lookAt(unit.vel().angle());
            } else {
                unit.aimLook(target);
                unit.controlWeapons(unit.within(target, unit.range() * 1.25f));
            }
        } else {
            if (invalid(target)) {
                target = target(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);

                if (unit.isFlying()) circle(unit.closestCore(), 30f);
                else moveTo(unit.closestCore(), 30f);
            } else {
                moveTo(target, unit.range());
                unit.aimLook(target);
                unit.controlWeapons(unit.within(target, unit.range() * 1.5f));
            }
        }
    }
}
