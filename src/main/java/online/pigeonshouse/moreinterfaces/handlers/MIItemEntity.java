package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.EntityType;

@Getter
@Setter
public class MIItemEntity extends MIEntity {
    private final MIItemStack stack;

    public static MIItemEntity create(String name, String uuid, MIItemStack stack, MIPos pos) {
        return new MIItemEntity(name, uuid, stack, pos);
    }

    public MIItemEntity(String name, String uuid, MIItemStack stack, MIPos pos) {
        super(name, EntityType.ITEM.getDefaultLootTable()
                .location().toString(), uuid, pos,
                EntityType.ITEM.getDimensions().eyeHeight());
        this.stack = stack;
    }
}
