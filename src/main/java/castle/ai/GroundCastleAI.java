package castle.ai;

import mindustry.ai.types.GroundAI;
import mindustry.entities.Units;
import mindustry.gen.Teamc;

public class GroundCastleAI extends GroundAI {

    public boolean attacker;

    @Override
    public void init() {
        if (unit.closestCore() == null || unit.closestEnemyCore() == null) return;
        attacker = unit.closestCore().dst(unit) > unit.closestEnemyCore().dst(unit) && unit.dst(unit.closestCore()) > 64f;
    }

    @Override
    public void updateUnit() {
        if (attacker) {
            super.updateMovement();
            super.updateTargeting();
        }
        else updateDefend();
    }

    public void updateDefend() {
        if (retarget()) {
            target = findTarget();
        }

        if (invalid(target)) {
            target = findTarget();
            moveTo(unit.closestCore(), 24f * unit.hitSize);
        } else {
            moveTo(target, unit.range());

            unit.aimLook(target);
            unit.controlWeapons(unit.within(target, unit.range() * 1.25f));
        }
    }

    public Teamc findTarget() {
        return Units.closestEnemy(unit.team, unit.x, unit.y, unit.range() * 1.5f + 64f, unit -> true);
    }
}
