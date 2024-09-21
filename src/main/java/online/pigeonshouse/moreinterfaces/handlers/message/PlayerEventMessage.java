package online.pigeonshouse.moreinterfaces.handlers.message;

import lombok.Getter;
import lombok.Setter;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.netty.message.SimpleSessionMessage;

import java.util.Map;

@Getter
@Setter
public class PlayerEventMessage extends SimpleSessionMessage {
    public static final int MESSAGE_TYPE = 5;

    private String event;
    private MIPlayer player;
    private Map<String, Object> data;

    public PlayerEventMessage() {
        super(null, MESSAGE_TYPE);
    }

    public PlayerEventMessage(String sessionId, String event, MIPlayer player, Map<String, Object> data) {
        super(sessionId, MESSAGE_TYPE);
        this.event = event;
        this.player = player;
        this.data = data;
    }
}
