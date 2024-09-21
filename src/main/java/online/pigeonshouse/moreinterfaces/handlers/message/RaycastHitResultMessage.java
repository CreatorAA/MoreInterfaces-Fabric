package online.pigeonshouse.moreinterfaces.handlers.message;

import lombok.Getter;
import lombok.Setter;
import online.pigeonshouse.moreinterfaces.handlers.MIBlock;
import online.pigeonshouse.moreinterfaces.handlers.MIEntity;
import online.pigeonshouse.moreinterfaces.netty.message.SimpleSessionMessage;

@Getter
@Setter
public class RaycastHitResultMessage extends SimpleSessionMessage {
    public static final int MESSAGE_TYPE = 11;

    private String rType;
    private MIBlock.BlockStatePack miBlock;
    private MIEntity miEntity;

    public RaycastHitResultMessage() {
        super(null, MESSAGE_TYPE);
    }

    public RaycastHitResultMessage(String sessionId) {
        super(sessionId, MESSAGE_TYPE);
        this.rType = "null";
    }

    public RaycastHitResultMessage(String sessionId, MIBlock.BlockStatePack miBlock) {
        super(sessionId, MESSAGE_TYPE);
        this.miBlock = miBlock;
        this.rType = "block";
    }

    public RaycastHitResultMessage(String sessionId, MIEntity miEntity) {
        super(sessionId, MESSAGE_TYPE);
        this.miEntity = miEntity;
        this.rType = "entity";
    }
}
