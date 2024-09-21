package online.pigeonshouse.moreinterfaces.netty.message;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CommandMessage extends SimpleTokenMessage {
    public static final Integer MESSAGE_TYPE = 2;
    String command;
    Map<String, Object> data;

    public CommandMessage(String token, String sessionId, String command) {
        super(token, sessionId, MESSAGE_TYPE);
        this.command = command;
    }

    public CommandMessage(String token, String sessionId, String command, Map<String, Object> data) {
        super(token, sessionId, MESSAGE_TYPE);
        this.command = command;
        this.data = data;
    }
}
