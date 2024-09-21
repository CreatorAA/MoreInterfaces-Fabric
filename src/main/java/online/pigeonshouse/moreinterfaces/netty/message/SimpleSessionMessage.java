package online.pigeonshouse.moreinterfaces.netty.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SimpleSessionMessage implements Message, SessionMessage {
    String sessionId;
    final Integer type;

    public SimpleSessionMessage(String sessionId, Integer type) {
        this.type = type;
        this.sessionId = sessionId;
    }
}
