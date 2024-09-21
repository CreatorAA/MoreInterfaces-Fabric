package online.pigeonshouse.moreinterfaces.eventhandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.handlers.commands.PlayerEventCommand;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;

import java.util.Map;

public class EntityLoadWorldEventHandler {
    public static volatile EntityLoadWorldEventHandler INSTANCE;

    public static EntityLoadWorldEventHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (EntityLoadWorldEventHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EntityLoadWorldEventHandler();
                }
            }
        }
        return INSTANCE;
    }

    public void onLoad(Entity entity, Level world) {
        if (entity instanceof ServerPlayer player) {
            MIPlayer miPlayer = MIUtil.buildMIPlayer(player);
            PlayerEventCommand.callEvent("spawn", miPlayer, Map.of());
        }
    }
}
