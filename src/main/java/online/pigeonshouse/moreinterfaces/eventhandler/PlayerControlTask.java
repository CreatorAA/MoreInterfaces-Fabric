package online.pigeonshouse.moreinterfaces.eventhandler;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.handlers.commands.PlayerControlCommand;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.utils.WorldUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class PlayerControlTask extends ServerLifecycleEventHandler.SimpleTickTask {
    private final ServerPlayer player;
    private final BehaviorSynthesizer synthesizer = new BehaviorSynthesizer();

    public PlayerControlTask(ChatSession session, ServerPlayer player) {
        super(session);
        this.player = player;
    }

    @Override
    public Object run() {
        if (player.hasDisconnected()) {
            return null;
        }
        synthesizer.handle(player);
        return null;
    }

    /**
     * 行为合成器
     */
    public class BehaviorSynthesizer {
        private final Map<String, Behavior> behaviors = new HashMap<>();
        private final List<Behavior> behaviorsList = new LinkedList<>();

        public void add(String action, Behavior behavior) {
            Behavior behavior1 = behaviors.get(action);
            if (behavior1 == null) {
                behaviors.put(action, behavior);
                behaviorsList.add(behavior);
                behaviorsList.sort(Comparator.comparingInt(Behavior::priority));
            } else {
                behavior1.andThen(behavior);
            }
        }

        public void remove(String action) {
            Behavior remove = behaviors.remove(action);
            if (remove != null) {
                behaviorsList.remove(remove);
                if (isEmpty()) {
                    PlayerControlCommand.remove(player.getDisplayName().getString());
                }
            }
        }

        public boolean isEmpty() {
            return behaviors.isEmpty();
        }

        public void handle(ServerPlayer player) {
            if (behaviorsList.isEmpty() || player.hasDisconnected()) {
                PlayerControlCommand.remove(player.getDisplayName().getString());
                return;
            }

            for (Behavior behavior : behaviorsList) {
                behavior.behavior(player);
                if (!behavior.isContinue()) {
                    remove(behavior.action());
                }
            }
        }
    }

    public interface Behavior {
        String action();

        /**
         * 传递一个Behavior，并对自己合成
         */
        void andThen(Behavior behavior);

        /**
         * 行为优先级
         */
        int priority();

        /**
         * 行为执行
         */
        void behavior(ServerPlayer player);

        /**
         * 是否继续执行
         */
        default boolean isContinue() {
            return false;
        }

        class Use implements Behavior {
            private final AtomicInteger count = new AtomicInteger(1);
            private final AtomicInteger interval = new AtomicInteger(3);
            private int thisTick = 0;

            public void setCount(int count) {
                this.count.set(count);
            }

            public void setInterval(int interval) {
                this.interval.set(interval);
            }

            @Override
            public String action() {
                return "use";
            }

            @Override
            public void andThen(Behavior behavior) {
                if (this == behavior) return;

                if (behavior instanceof Use) {
                    count.set(((Use) behavior).count.get());
                    interval.set(((Use) behavior).interval.get());
                } else {
                    throw new IllegalArgumentException("can not andThen " + behavior.getClass().getName());
                }
            }

            @Override
            public int priority() {
                return 4;
            }

            int waitTick = 0;

            @Override
            public void behavior(ServerPlayer player) {
                if (thisTick == 0) {
                    thisTick = 1;
                    return;
                }

                if (waitTick > 0){
                    waitTick--;
                    return;
                }

                if (player.isUsingItem()){
                    return;
                }

                if (thisTick++ % interval.get() == 0) {
                    thisTick = 0;

                    HitResult result = WorldUtil.rayTrace(player, 4, false);
                    for (InteractionHand hand : InteractionHand.values()) {
                        switch (result.getType()) {
                            case BLOCK:
                                useBlock(player, (BlockHitResult) result, hand);
                                break;
                            case ENTITY:
                                useEntity(player, (EntityHitResult) result, hand);
                                break;
                        }

                        ItemStack handItem = player.getItemInHand(hand);

                        if (player.gameMode.useItem(player, player.getLevel(), handItem, hand).consumesAction()) {
                            waitTick = 3;
                        }
                    }

                    if (count.get() != -1)
                        count.decrementAndGet();
                }
            }

            private void useBlock(ServerPlayer player, BlockHitResult result, InteractionHand hand) {
                if (player.containerMenu != player.inventoryMenu) return;
                player.resetLastActionTime();
                ServerLevel world = player.getLevel();
                BlockPos pos = result.getBlockPos();
                Direction side = result.getDirection();

                if (pos.getY() < player.getLevel().getMaxBuildHeight() - (side == Direction.UP ? 1 : 0)
                        && world.mayInteract(player, pos)) {
                    InteractionResult result1 = player.gameMode.useItemOn(player, world,
                            player.getItemInHand(hand), hand, result);

                    if (result1.consumesAction() && result1.shouldSwing())
                        player.swing(hand);
                }
            }

            private void useEntity(ServerPlayer player, EntityHitResult result, InteractionHand hand) {
                if (player.containerMenu != player.inventoryMenu) return;

                player.resetLastActionTime();
                Entity entity = result.getEntity();
                Vec3 relativeHitPos = result.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());

                MoreInterfaces.MINECRAFT_SERVER.get().execute(() -> {
                    if (entity.interactAt(player, relativeHitPos, hand).consumesAction()) {
                        return;
                    }
                    player.interactOn(entity, hand);
                });
            }

            @Override
            public boolean isContinue() {
                return count.get() == -1 || count.get() > 0;
            }
        }

        class Attack implements Behavior {
            private final AtomicInteger count = new AtomicInteger(1);
            private final AtomicInteger interval = new AtomicInteger(1);
            private int thisTick = 0;

            public void setCount(int count) {
                this.count.set(count);
            }

            public void setInterval(int interval) {
                this.interval.set(interval);
            }

            @Override
            public String action() {
                return "attack";
            }

            @Override
            public void andThen(Behavior behavior) {
                if (this == behavior) return;

                if (behavior instanceof Attack) {
                    count.set(((Attack) behavior).count.get());
                    interval.set(((Attack) behavior).interval.get());
                } else {
                    throw new IllegalArgumentException("can not andThen " + behavior.getClass().getName());
                }
            }

            @Override
            public int priority() {
                return 5;
            }

            @Override
            public void behavior(ServerPlayer player) {
                if (thisTick == 0) {
                    thisTick = 1;
                    return;
                }

                if (thisTick++ % interval.get() == 0) {
                    thisTick = 0;
                    HitResult result = WorldUtil.rayTrace(player, 4, false);
                    switch (result.getType()) {
                        case BLOCK:
                            attackBlock(player, (BlockHitResult) result);
                            break;
                        case ENTITY:
                            attackEntity(player, (EntityHitResult) result);
                    }

                    if (count.get() != -1) count.decrementAndGet();
                }
            }

            private void attackEntity(ServerPlayer player, EntityHitResult result) {
                player.attack(result.getEntity());
                player.swing(InteractionHand.MAIN_HAND);
                player.resetAttackStrengthTicker();
                player.resetLastActionTime();
            }

            BlockPos currentBlock;
            float curBlockDamageMP = 0;

            private void attackBlock(ServerPlayer player, BlockHitResult result) {
                BlockPos pos = result.getBlockPos();
                Direction side = result.getDirection();
                if (player.blockActionRestricted(player.getLevel(), pos, player.gameMode.getGameModeForPlayer()))
                    return;

                BlockState state = player.getLevel().getBlockState(pos);
                boolean notAir = !state.isAir();

                if (player.gameMode.getGameModeForPlayer().isCreative()) {
                    player.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, side, player.getLevel().getMaxBuildHeight());
                    return;
                }

                if (currentBlock == null || !currentBlock.equals(pos)) {
                    if (currentBlock != null) {
                        player.gameMode.handleBlockBreakAction(currentBlock,
                                ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, side,
                                player.getLevel().getMaxBuildHeight());
                    }
                    player.gameMode.handleBlockBreakAction(pos,
                            ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, side,
                            player.getLevel().getMaxBuildHeight());

                    if (notAir && curBlockDamageMP == 0) {
                        state.attack(player.getLevel(), pos, player);
                    }

                    if (notAir && state.getDestroyProgress(player, player.getLevel(), pos) >= 1) {
                        currentBlock = null;
                    } else {
                        currentBlock = pos;
                        curBlockDamageMP = 0;
                    }
                } else {
                    curBlockDamageMP += state.getDestroyProgress(player, player.getLevel(), pos);

                    if (curBlockDamageMP >= 1) {
                        player.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                                side, player.getLevel().getMaxBuildHeight());
                        currentBlock = null;
                    }
                    player.getLevel().destroyBlockProgress(-1, pos, (int) (curBlockDamageMP * 10));
                }

                player.resetLastActionTime();
                player.swing(InteractionHand.MAIN_HAND);
            }

            @Override
            public boolean isContinue() {
                return count.get() == -1 || count.get() > 0;
            }
        }

        class Jump implements Behavior {
            private final AtomicInteger count = new AtomicInteger(1);
            private final AtomicInteger interval = new AtomicInteger(1);
            private int thisTick = 0;

            public void setCount(int count) {
                this.count.set(count);
            }

            public void setInterval(int interval) {
                this.interval.set(interval);
            }

            @Override
            public String action() {
                return "jump";
            }

            @Override
            public void andThen(Behavior behavior) {
                if (this == behavior) return;

                if (behavior instanceof Jump) {
                    count.set(((Jump) behavior).count.get());
                    interval.set(((Jump) behavior).interval.get());
                } else {
                    throw new IllegalArgumentException("can not andThen " + behavior.getClass().getName());
                }
            }

            @Override
            public int priority() {
                return 0;
            }

            @Override
            public void behavior(ServerPlayer player) {
                if (thisTick == 0) {
                    thisTick = 1;
                    return;
                }

                if (thisTick++ % interval.get() == 0) {
                    thisTick = 0;

                    if (player.isOnGround()) {
                        player.jumpFromGround();
                        if (count.get() != -1)
                            count.decrementAndGet();
                    }
                }
            }

            @Override
            public boolean isContinue() {
                return count.get() == -1 || count.get() > 0;
            }
        }
    }
}
