package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;

import java.util.*;

@Getter
@Setter
public class MIBlock extends MIData {
    private final String blockName;
    private final String registerName;

    public static final MIBlock EMPTY = new MIBlock("null", "null");

    public static MIBlock of(String blockName, String registerName) {
        return new MIBlock(blockName, registerName);
    }

    private MIBlock(String blockName, String registerName) {
        this.blockName = blockName;
        this.registerName = registerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MIBlock)) return false;
        MIBlock block = (MIBlock) o;
        return Objects.equals(blockName, block.blockName) && Objects.equals(registerName, block.registerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockName, registerName);
    }

    public static MIBlock.ContainerPack buildContainerPack(Container container) {
        List<MIItemStack> items = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            MIItemStack miItemStack = MIUtil.buildMIItemStack(item);
            items.add(miItemStack);
        }

        return new MIBlock.ContainerPack(container.getContainerSize(), items);
    }

    @Data
    public static class BlockStatePack {
        private final MIBlock block;
        private final MIPos pos;
        private final int lightLevel;
        private final boolean isSignalSource;
        private Map<String, Object> extraInfo = new HashMap<>();
    }

    @Data
    public static class ContainerPack {
        private final List<MIItemStack> items;
        private int containerSize;

        public ContainerPack(int containerSize) {
            this.items = new ArrayList<>();
            this.containerSize = containerSize;
        }

        public ContainerPack(int containerSize, List<MIItemStack> items) {
            this.items = items;
            this.containerSize = containerSize;
        }

        public String toJsonString() {
            return MIData.GSON.toJson(this);
        }
    }
}
