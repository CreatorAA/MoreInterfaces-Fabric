package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MIEntity extends MIData {
    private final MIEntityInfo type;
    private final String uuid;
    private final MIPos pos;
    private double eyeHeight;
    private float yaw, pitch;

    public static MIEntity create(String name, String registerName, String uuid, MIPos pos, double eyeHeight) {
        return new MIEntity(name, registerName, uuid, pos, eyeHeight);
    }

    public static MIEntity create(MIEntityInfo type, String uuid, MIPos pos, double eyeHeight) {
        return new MIEntity(type, uuid, pos, eyeHeight);
    }

    protected MIEntity(MIEntityInfo type, String uuid, MIPos pos, double eyeHeight) {
        this.type = type;
        this.uuid = uuid;
        this.pos = pos;
        this.eyeHeight = eyeHeight;
    }

    protected MIEntity(String name, String registerName, String uuid, MIPos pos, double eyeHeight) {
        this.type = MIEntityInfo.of(name, registerName);
        this.uuid = uuid;
        this.pos = pos;
        this.eyeHeight = eyeHeight;
    }
}
