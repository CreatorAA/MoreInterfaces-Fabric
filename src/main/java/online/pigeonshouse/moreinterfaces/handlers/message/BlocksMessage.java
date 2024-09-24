package online.pigeonshouse.moreinterfaces.handlers.message;

import lombok.Getter;
import lombok.Setter;
import online.pigeonshouse.moreinterfaces.handlers.MIBlock;
import online.pigeonshouse.moreinterfaces.netty.message.SimpleSessionMessage;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BlocksMessage extends SimpleSessionMessage {
    public static final int MESSAGE_TYPE = 7;
    private List<MIBlock.BlockStatePack> blocks;

    public BlocksMessage() {
        super(null, MESSAGE_TYPE);
        this.blocks = new ArrayList<>();
    }

    public BlocksMessage(String sessionId, List<MIBlock.BlockStatePack> blocks) {
        super(sessionId, MESSAGE_TYPE);
        this.blocks = blocks;
    }
}
