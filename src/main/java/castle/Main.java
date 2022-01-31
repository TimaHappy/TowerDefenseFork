package castle;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import castle.components.CastleIcons;
import castle.components.CastleUnitDrops;
import mindustry.core.World;
import mindustry.game.EventType.*;
import mindustry.game.Gamemode;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.mod.Plugin;

import static mindustry.Vars.*;

public class Main extends Plugin {

    @Override
    public void init() {
        CastleIcons.load();
        CastleUnitDrops.load();

        Events.run(Trigger.update, () -> {});

        Events.on(PlayerJoin.class, event -> {});

        Events.on(UnitDestroyEvent.class, event -> {});

        Events.on(WorldLoadEvent.class, Log::info);

        CastleLogic.loadMap(maps.getShuffleMode().next(Gamemode.survival, null));
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {

    }
}
