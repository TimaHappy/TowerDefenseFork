package castle.ai;

import arc.Core;
import arc.func.Prov;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.entities.units.UnitController;
import mindustry.gen.Call;

public class AIShell extends AIController {

    public boolean withdrawn;

    public UnitController parent;
    public Runnable update;
    
    public AIShell(Prov<? extends UnitController> parent) {
        this.parent = parent.get();
        this.update = this::updateMovement;
    }

    @Override
    public void init() {
        if (unit.closestCore() == null || unit.closestEnemyCore() == null) withdrawn = true;
        else Core.app.post(() -> { // during the init() call, the unit is at (0, 0)
            if (unit.closestCore().dst(unit) > unit.closestEnemyCore().dst(unit)) update = parent::updateUnit;
            parent.unit(unit);
        });
    }

    @Override
    public void updateUnit() {
        if (withdrawn) Call.unitDespawn(unit);
        else update.run();
    }

    @Override
    public void updateMovement() {
        if (invalid(target)) {
            target = Units.closestEnemy(unit.team, unit.x, unit.y, 360f, unit -> true);

            circle(unit.closestCore(), 120f);
            unit.movePref(unit.vel);
        } else {
            moveTo(target, unit.range());

            unit.aimLook(target);
            unit.controlWeapons(unit.within(target, unit.range() * 1.25f));
        }
    }
}