package castle.ai;

import castle.CastleLogic;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;

import static mindustry.Vars.tilesize;

public class DefenderAI extends AIController {

    @Override
    public void updateUnit() {
        if (invalid(target)) {
            target = Units.closestEnemy(unit.team, unit.x, unit.y, 360f, CastleLogic::onEnemySide);
            if (unit.type.flying) circle(unit.closestCore(), Math.max(unit.hitSize() * tilesize, 150f));
            else moveTo(unit.closestCore(), Math.max(unit.hitSize() * tilesize, 120f), 1f);
        } else {
            moveTo(target, unit.mounts[0].weapon.bullet.range * .8f + (target instanceof Sized sized ? sized.hitSize() / 2f : 0f), 1f);
            updateWeapons();
        }

        faceTarget();
    }
}
