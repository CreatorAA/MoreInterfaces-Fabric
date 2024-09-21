package online.pigeonshouse.moreinterfaces.handlers.message;

import lombok.Getter;
import lombok.Setter;
import online.pigeonshouse.moreinterfaces.netty.message.SimpleSessionMessage;

@Getter
@Setter
public class CommandResultMessage extends SimpleSessionMessage {
    public static final int MESSAGE_TYPE = 9;

    private String result;

    public CommandResultMessage() {
        super(null, MESSAGE_TYPE);
    }

    public CommandResultMessage(String sessionId, String result) {
        super(sessionId, MESSAGE_TYPE);
        this.result = result;
    }
}
