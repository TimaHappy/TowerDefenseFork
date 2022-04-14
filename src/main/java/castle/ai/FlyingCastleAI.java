package castle.ai;

import arc.math.Mathf;
import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.gen.Teamc;

public class FlyingCastleAI extends FlyingAI {

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
            super.updateVisuals();
        }
        else updateDefend();
    }

    public void updateDefend() {
        if (retarget()) {
            target = findTarget();
        }

        if (invalid(target)) {
            target = findTarget();
            moveTo(unit.closestCore(), unit.range());
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
