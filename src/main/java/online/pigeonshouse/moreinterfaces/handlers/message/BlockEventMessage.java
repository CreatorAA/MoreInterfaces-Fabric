package online.pigeonshouse.moreinterfaces.handlers.message;

import lombok.Getter;
import lombok.Setter;
import online.pigeonshouse.moreinterfaces.handlers.MIBlock;
import online.pigeonshouse.moreinterfaces.netty.message.SimpleSessionMessage;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class BlockEventMessage extends SimpleSessionMessage {
    public static final int MESSAGE_TYPE = 8;
    private String event;
    private MIBlock block;
    private Map<String, Object> data;

    public BlockEventMessage() {
        super(null, MESSAGE_TYPE);
        this.data = new HashMap<>();
    }

    public BlockEventMessage(String sessionId, String event, MIBlock block, Map<String, Object> data) {
        super(sessionId, MESSAGE_TYPE);
        this.block = block;
        this.event = event;
        this.data = data;
    }
}
