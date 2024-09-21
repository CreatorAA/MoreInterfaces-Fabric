package online.pigeonshouse.moreinterfaces.utils;

import io.netty.channel.Channel;
import online.pigeonshouse.moreinterfaces.netty.message.ErrorMessage;

public class ChannelUtil {
    public static void sendErrorMessage(Channel channel, String sessionId, int code, String message) {
        channel.writeAndFlush(new ErrorMessage(sessionId, code, message));
    }
}
