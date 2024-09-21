package online.pigeonshouse.moreinterfaces.netty.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SimpleTokenMessage extends SimpleSessionMessage implements TokenMessage {
    String token;

    public SimpleTokenMessage(String token, String sessionId, Integer type) {
        super(sessionId, type);
        this.token = token;
    }
}
