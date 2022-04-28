package castle.ai;

import arc.Core;
import arc.func.Prov;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.entities.units.UnitController;
import mindustry.game.Team;
import mindustry.gen.Teamc;

import static castle.CastleLogic.*;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class AIShell extends AIController {

    public UnitController parent;
    public Runnable update;
    
    public AIShell(Prov<? extends UnitController> controller) {
        this.parent = controller.get();
        this.update = this::updateMovement;
    }

    @Override
    public void init() {
        if (!isBreak()) Core.app.post(() -> {
            parent.unit(unit);
            if (onEnemySide(unit)) update = parent::updateUnit;
        }); // Это необходимо, т.к. во время вызова метода, контроллер, по сути, еще не существует, а юнит не имеет позиции.
    }

    @Override
    public void updateUnit() {
        if (!isBreak()) {
            update.run();
        }
    }

    @Override
    public void updateMovement() {
        if (invalid(target) || !onEnemySide(target)) {
            target = Units.closestEnemy(unit.team, unit.x, unit.y, 360f, AIShell::onEnemySide);
            moveTo(unit.closestCore(), unit.hitSize() * tilesize, 1f);
        } else {
            moveTo(target, unit.mounts[0].weapon.bullet.range() * .8f + (target instanceof Sized sized ? sized.hitSize() / 2f : 0f), 1f);
            updateWeapons();
        }

        faceTarget();
    }

    public static boolean onEnemySide(Teamc teamc) {
        return (teamc.team() == Team.sharded && teamc.y() > world.unitHeight() / 2f) || (teamc.team() == Team.blue && teamc.y() < world.unitHeight() / 2f);
    }
}