package online.pigeonshouse.moreinterfaces.netty.message;

public class PingMessage implements Message {
    public static final Integer MESSAGE_TYPE = 1;

    @Override
    public Integer getType() {
        return MESSAGE_TYPE;
    }
}
