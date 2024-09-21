package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MIItem extends MIData {
    private String name;
    private String registeredName;

    public static MIItem of(String name, String registeredName) {
        MIItem item = new MIItem();
        item.setName(name);
        item.setRegisteredName(registeredName);
        return item;
    }
}
