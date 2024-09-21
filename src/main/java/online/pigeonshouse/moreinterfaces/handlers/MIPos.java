package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MIPos extends MIData {
    private final String world;
    private final double x;
    private final double y;
    private final double z;

    public MIPos(String world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static MIPos create(String world, double x, double y, double z) {
        return new MIPos(world, x, y, z);
    }

    public int getIntX() {
        return (int) Math.round(x);
    }

    public int getIntY() {
        return (int) Math.round(y);
    }

    public int getIntZ() {
        return (int) Math.round(z);
    }
}
