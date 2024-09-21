package online.pigeonshouse.moreinterfaces.handlers.message;

import lombok.Getter;
import lombok.Setter;
import online.pigeonshouse.moreinterfaces.handlers.MIEntity;
import online.pigeonshouse.moreinterfaces.netty.message.SimpleSessionMessage;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EntityListMessage extends SimpleSessionMessage {
    public static final int MESSAGE_TYPE = 10;
    private final List<MIEntity> entities;

    public EntityListMessage() {
        super(null, MESSAGE_TYPE);
        this.entities = new ArrayList<>();
    }

    public EntityListMessage(String sessionId, List<MIEntity> entities) {
        super(sessionId, MESSAGE_TYPE);
        this.entities = entities;
    }
}
