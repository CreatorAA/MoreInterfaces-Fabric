package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MIEntityInfo extends MIData {
    private final String name;
    private final String registerName;

    public static MIEntityInfo of(String name, String registerName) {
        return new MIEntityInfo(name, registerName);
    }

    public MIEntityInfo(String name, String registerName) {
        this.name = name;
        this.registerName = registerName;
    }
}
