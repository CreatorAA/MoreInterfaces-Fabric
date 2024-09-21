package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;

@Getter
@Setter
public class MIArea extends MIData {
    private final MIPos pos1, pos2;
    private String areaName;

    public MIArea(MIPos pos1, MIPos pos2, String areaName) {
        this.pos1 = pos1;
        this.pos2 = pos2;

        if (areaName == null) {
            this.areaName = String.format("area[%s, %s, %s - %s %s %s]", pos1.getIntX(), pos1.getIntY(), pos1.getIntZ(),
                    pos2.getIntX(), pos2.getIntY(), pos2.getIntZ());
        }else {
            this.areaName = areaName;
        }
    }

    public BlockPos fromBlockPos() {
        return new BlockPos(pos1.getIntX(), pos1.getIntY(), pos1.getIntZ());
    }


}
