package online.pigeonshouse.moreinterfaces.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.config.RemoteToken;
import online.pigeonshouse.moreinterfaces.netty.message.Message;
import online.pigeonshouse.moreinterfaces.netty.message.SucceededMessage;
import online.pigeonshouse.moreinterfaces.utils.ChannelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ChatSession {
    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);

    private final Channel channel;
    private final String sessionId;
    private final RemoteToken token;

    @Getter
    public final Map<String, Object> tempObjects = new HashMap<>();

    public ChatSession(Channel channel, String sessionId, RemoteToken token) {
        this.channel = channel;
        this.sessionId = sessionId;
        this.token = token;
    }

    public Channel channel() {
        return channel;
    }

    public String sessionId() {
        return sessionId;
    }

    public RemoteToken token() {
        return token;
    }

    public Object getOrAdd(String key, Object defaultValue) {
        return tempObjects.computeIfAbsent(key, k -> defaultValue);
    }

    public void sendErrorMsg(int errorCode, String errorMsg) {
        ChannelUtil.sendErrorMessage(channel, sessionId, errorCode, errorMsg);
    }

    public void sendMsg(Message message, Function<ChatSession, Void> callback) {
        channel().writeAndFlush(message).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                callback.apply(ChatSession.this);
            } else {
                log.error("Failed to send message to session {}: {}", sessionId, future.cause());
            }
        });
    }

    public void sendSucceededMessage() {
        sendMsg(new SucceededMessage(sessionId), session -> null);
    }

    public void sendMsg(Message message) {
        sendMsg(message, session -> null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatSession)) return false;
        ChatSession session = (ChatSession) o;
        return Objects.equals(sessionId, session.sessionId) && Objects.equals(token, session.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, token);
    }
}
