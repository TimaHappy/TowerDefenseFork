package castle.ai;

import mindustry.ai.Pathfinder;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;

public class CastleAI extends AIController {

    public boolean attacker;
    public boolean withdrawn;

    @Override
    public void init() {
        if (unit.closestCore() == null || unit.closestEnemyCore() == null) withdrawn = true;
        else attacker = unit.closestCore().dst(unit) > unit.closestEnemyCore().dst(unit);
    }

    @Override
    public void updateUnit() {
        if (withdrawn) return;
        if (attacker) updateAttack();
        else updateDefend();
    }

    public void updateAttack() {
        if (!unit.isFlying()) pathfind(Pathfinder.fieldCore);
        else moveTo(unit.closestEnemyCore(), unit.range() / 1.5f);

        if (invalid(target)) {
            target = target(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);
            unit.lookAt(unit.vel().angle());
        } else {
            unit.aimLook(target);
            unit.controlWeapons(unit.within(target, unit.range() * 1.25f));
        }
    }

    public void updateDefend() {
        if (invalid(target)) {
            target = Units.closestEnemy(unit.team, unit.x, unit.y, 360f, unit -> true);

            if (unit.isFlying()) circle(unit.closestCore(), 60f);
            else moveTo(unit.closestCore(), 60f);
        } else {
            moveTo(target, unit.range());

            unit.aimLook(target);
            unit.controlWeapons(unit.within(target, unit.range() * 1.25f));
        }
    }
}
