package online.pigeonshouse.moreinterfaces.netty.message;

public class SucceededMessage extends SimpleSessionMessage{
    public static final int MESSAGE_TYPE = 0;

    public SucceededMessage() {
        super(null, MESSAGE_TYPE);
    }

    public SucceededMessage(String sessionId) {
        super(sessionId, MESSAGE_TYPE);
    }
}
