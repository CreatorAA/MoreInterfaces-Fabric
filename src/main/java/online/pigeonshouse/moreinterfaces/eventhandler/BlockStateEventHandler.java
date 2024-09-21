package online.pigeonshouse.moreinterfaces.eventhandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import online.pigeonshouse.moreinterfaces.handlers.MIBlock;
import online.pigeonshouse.moreinterfaces.handlers.MIPos;
import online.pigeonshouse.moreinterfaces.handlers.commands.BlockEventCommand;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;

import java.util.Map;

public class BlockStateEventHandler extends ServerLifecycleEventHandler.SimpleTickTask {
    private BlockPos pos;
    private final MIPos miPos;
    private final ServerLevel level;
    private final ChatSession session;

    private volatile MIBlock.BlockStatePack pack;

    public BlockStateEventHandler(ChatSession session, MIPos pos, ServerLevel level) {
        super(session);
        this.miPos = pos;
        this.pos = new BlockPos(pos.getIntX(), pos.getIntY(), pos.getIntZ());
        this.level = level;
        this.session = session;
    }

    @Override
    public Object run() {
        if (!level.isLoaded(pos)) {
            return null;
        }

        BlockState blockState = level.getBlockState(pos);
        MIBlock.BlockStatePack blockStatePack = createBlockStatePack(level, pos, miPos, blockState);

        if (blockStatePack.equals(pack)) {
            return null;
        }

        pack = blockStatePack;

        if (!BlockEventCommand.callEvent(session, "block_state", blockStatePack.getBlock(), Map.of("block_state", blockStatePack))) {
            ServerLifecycleEventHandler.INSTANCE.removeTickTask(this);
        }
        return blockStatePack;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void runAsync(Object o) {
        if (!(o instanceof MIBlock.BlockStatePack)) return;
        MIBlock.BlockStatePack blockStatePack = (MIBlock.BlockStatePack) o;

        if (!BlockEventCommand.callEvent(session, "block_state", blockStatePack.getBlock(), Map.of("block_state", blockStatePack))) {
            ServerLifecycleEventHandler.INSTANCE.removeTickTask(this);
        }
    }

    public static MIBlock.BlockStatePack createBlockStatePack(ServerLevel level, BlockPos pos, MIPos miPos,
                                                              BlockState blockState) {
        int lightLevel = blockState.getLightEmission();
        Block block = blockState.getBlock();
        MIBlock miBlock =
                MIBlock.of(block.getName().getString(), block.getLootTable().location().toString());
        MIBlock.BlockStatePack blockStatePack = new MIBlock.BlockStatePack(miBlock, miPos, lightLevel, blockState.isSignalSource());
        Map<String, Object> extraInfo = blockStatePack.getExtraInfo();
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
            extraInfo.put(entry.getKey().getName(), entry.getValue());
        }

        if (blockState.getBlock() instanceof EntityBlock) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof Container) {
                Container container = (Container) entity;
                extraInfo.put("container", MIBlock.buildContainerPack(container));
            }
        }

        if (blockStatePack.isSignalSource()) {
            int down = blockState.getSignal(level, pos, Direction.DOWN);
            int up = blockState.getSignal(level, pos, Direction.UP);
            int north = blockState.getSignal(level, pos, Direction.NORTH);
            int south = blockState.getSignal(level, pos, Direction.SOUTH);
            int west = blockState.getSignal(level, pos, Direction.WEST);
            int east = blockState.getSignal(level, pos, Direction.EAST);

            Map<String, Integer> signal = Map.of(
                    "down", down,
                    "up", up,
                    "north", north,
                    "south", south,
                    "west", west,
                    "east", east
            );

            extraInfo.putAll(signal);
        }

        return blockStatePack;
    }
}
