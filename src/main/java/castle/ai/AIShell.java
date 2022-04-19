package castle.ai;

import arc.Core;
import arc.func.Prov;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.entities.units.UnitController;
import mindustry.gen.Teamc;

import static castle.CastleLogic.*;

public class AIShell extends AIController {

    public UnitController parent;
    public Runnable update;
    
    public AIShell(Prov<? extends UnitController> parent) {
        this.parent = parent.get();
        this.update = this::updateMovement;
    }

    @Override
    public void init() {
        if (!isBreak()) Core.app.post(() -> {
            if (onEnemySide(unit)) update = parent::updateUnit;
            parent.unit(unit);
        }); // during the init() call, the unit is at (0, 0)
    }

    @Override
    public void updateUnit() {
        if (!isBreak()) update.run();
    }

    @Override
    public void updateMovement() {
        if (invalid(target) || !onEnemySide(target)) {
            target = Units.closestEnemy(unit.team, unit.x, unit.y, 360f, AIShell::onEnemySide);
            moveTo(unit.closestCore(), 160f, 1f);
        } else {
            moveTo(target, unit.mounts[0].weapon.bullet.range() * .8f + (target instanceof Sized s ? s.hitSize() / 2f : 0f), 1f);
            updateWeapons();
        }

        faceTarget();
    }

    public static boolean onEnemySide(Teamc unit) {
        return unit.closestCore().dst(unit) > unit.closestEnemyCore().dst(unit);
    }
}