package online.pigeonshouse.moreinterfaces.handlers.message;

import lombok.Getter;
import lombok.Setter;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.netty.message.SimpleSessionMessage;

/**
 * 早期测试用
 */
@Getter
@Setter
public class PlayerPosMessage extends SimpleSessionMessage {
    public static final int MESSAGE_TYPE = 3;
    private MIPlayer player;

    public PlayerPosMessage() {
        super(null, MESSAGE_TYPE);
    }

    public PlayerPosMessage(String sessionId, MIPlayer player) {
        super(sessionId, MESSAGE_TYPE);
        this.player = player;
    }
}
