package online.pigeonshouse.moreinterfaces.netty;

import lombok.Getter;
import online.pigeonshouse.moreinterfaces.handlers.message.*;
import online.pigeonshouse.moreinterfaces.netty.message.*;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MessageFactory {
    private static final Map<Integer,Class<? extends Message>> MAP = new HashMap<>();

    static  {
        register(ErrorMessage.MESSAGE_TYPE, ErrorMessage.class);

        register(PingMessage.MESSAGE_TYPE, PingMessage.class);
        register(CommandMessage.MESSAGE_TYPE, CommandMessage.class);
        register(NotSessionErrorMessage.MESSAGE_TYPE, NotSessionErrorMessage.class);
        register(SucceededMessage.MESSAGE_TYPE, SucceededMessage.class);

        register(PlayerPosMessage.MESSAGE_TYPE, PlayerPosMessage.class);
        register(PlayerListMessage.MESSAGE_TYPE, PlayerListMessage.class);
        register(PlayerEventMessage.MESSAGE_TYPE, PlayerEventMessage.class);
        register(BlocksMessage.MESSAGE_TYPE, BlocksMessage.class);
        register(BlockEventMessage.MESSAGE_TYPE, BlockEventMessage.class);
        register(CommandResultMessage.MESSAGE_TYPE, CommandResultMessage.class);
        register(EntityListMessage.MESSAGE_TYPE, EntityListMessage.class);
        register(RaycastHitResultMessage.MESSAGE_TYPE, RaycastHitResultMessage.class);
    }

    public static void register(Integer type, Class<? extends Message> clazz) {
        MAP.put(type, clazz);
    }

    public static void register(Message message) {
        MAP.put(message.getType(), message.getClass());
    }

    public static Class<? extends Message> valueOf(Integer type) {
        return MAP.get(type);
    }
}