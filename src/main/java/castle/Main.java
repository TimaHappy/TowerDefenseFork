package castle;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import castle.components.CastleIcons;
import castle.components.CastleUnitDrops;
import mindustry.game.EventType.*;
import mindustry.game.Gamemode;
import mindustry.mod.Plugin;

import static mindustry.Vars.*;

public class Main extends Plugin {

    @Override
    public void init() {
        Log.info("Plugin is initialising...");

        CastleIcons.load();
        CastleUnitDrops.load();

        Events.run(Trigger.update, () -> {});

        Events.on(PlayerJoin.class, event -> {});

        Events.on(UnitDestroyEvent.class, event -> {});

        Events.on(WorldLoadEvent.class, event -> {});
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("host");
        handler.register("host", "Start hosting the server on a random map.", args -> CastleLogic.startHosting(maps.getShuffleMode().next(Gamemode.pvp, state.map)));
    }
}
