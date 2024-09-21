package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.EntityType;

@Getter
@Setter
public class MIPlayer extends MIEntity {
    public static MIPlayer create(String name, String uuid, MIPos pos) {
        return new MIPlayer(name, uuid, pos);
    }

    private MIPlayer(String name, String uuid, MIPos pos) {
        super(name, EntityType.PLAYER.getDefaultLootTable()
                .location().toString(), uuid, pos, EntityType.PLAYER
                .getDimensions().eyeHeight());
    }
}
