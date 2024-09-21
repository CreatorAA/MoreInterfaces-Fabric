package online.pigeonshouse.moreinterfaces.eventhandler;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.handlers.commands.PlayerEventCommand;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;

import java.util.Map;

public class PlayerChatEventHandler {
    public static volatile PlayerChatEventHandler INSTANCE;

    public static PlayerChatEventHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (PlayerChatEventHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PlayerChatEventHandler();
                }
            }
        }
        return INSTANCE;
    }

    public boolean allowChatMessage(Component message, ServerPlayer sender, ResourceKey<ChatType> params) {
        if (ChatType.CHAT == params) {
            MIPlayer miPlayer = MIUtil.buildMIPlayer(sender);
            PlayerEventCommand.callEvent("chat", miPlayer, Map.of("message", message.getString()));
        }
        return true;
    }
}
