package online.pigeonshouse.moreinterfaces.eventhandler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.handlers.commands.PlayerEventCommand;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;
import online.pigeonshouse.moreinterfaces.utils.MapUtil;

public class PlayerConnectionEventHandler {
    public static volatile PlayerConnectionEventHandler INSTANCE;

    public static PlayerConnectionEventHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (PlayerConnectionEventHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PlayerConnectionEventHandler();
                }
            }
        }
        return INSTANCE;
    }

    public void onPlayDisconnect(ServerPlayer player, MinecraftServer server) {
        MIPlayer miPlayer = MIUtil.buildMIPlayer(player);
        PlayerEventCommand.callEvent("disconnect", miPlayer, MapUtil.of());
    }

    public void onPlayReady(ServerPlayer player, MinecraftServer server) {
        MIPlayer miPlayer = MIUtil.buildMIPlayer(player);
        PlayerEventCommand.callEvent("join", miPlayer, MapUtil.of());
    }
}
