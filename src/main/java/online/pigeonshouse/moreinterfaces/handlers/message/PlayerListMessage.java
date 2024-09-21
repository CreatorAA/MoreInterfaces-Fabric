package online.pigeonshouse.moreinterfaces.handlers.message;

import lombok.Getter;
import lombok.Setter;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.netty.message.SimpleSessionMessage;

@Getter
@Setter
public class PlayerListMessage extends SimpleSessionMessage {
    public static final int MESSAGE_TYPE = 4;

    private MIPlayer[] players;

    public PlayerListMessage() {
        super(null, MESSAGE_TYPE);
    }

    public PlayerListMessage(String sessionId, MIPlayer[] players) {
        super(sessionId, MESSAGE_TYPE);
        this.players = players;
    }
}
