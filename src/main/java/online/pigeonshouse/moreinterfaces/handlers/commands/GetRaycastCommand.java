package online.pigeonshouse.moreinterfaces.handlers.commands;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.eventhandler.BlockStateEventHandler;
import online.pigeonshouse.moreinterfaces.handlers.MIBlock;
import online.pigeonshouse.moreinterfaces.handlers.MIEntity;
import online.pigeonshouse.moreinterfaces.handlers.MIPos;
import online.pigeonshouse.moreinterfaces.handlers.message.RaycastHitResultMessage;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;
import online.pigeonshouse.moreinterfaces.utils.WorldUtil;

import java.util.UUID;

/**
 * 调用参数:
 * <p>
 * 射线构建方式 - raycast
 * 1. 坐标 - coordinate
 * 2. 实体 - entity
 * <p>
 * 实体 - uuid
 * 实体世界 - entityWorld
 * <p>
 * x - x
 * y - y
 * z - z
 * 水平方向的旋转角度 - yaw
 * 垂直方向的旋转角度 - pitch
 * 世界名 - world
 * <p>
 * 距离 - distance
 */
public class GetRaycastCommand implements CommandHandler {
    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();
        double distance = map.getDouble(session, "distance");

        String raycast = map.getString(session, "raycast");
        if (raycast.equals("entity")) {
            ServerLevel entityWorld = WorldUtil.getLevel(server.overworld(),
                    map.getString(session, "entityWorld"));

            Entity entity = entityWorld
                    .getEntity(UUID.fromString(map.getString(session, "uuid")));

            if (entity == null) {
                session.sendErrorMsg(MessageCode.ENTITY_NOT_FOUND, "找不到实体");
                return;
            }

            HitResult result =
                    server.submit(() -> WorldUtil.rayTrace(entity, distance, false)).join();

            sendResult(session, server, entityWorld, result);
        }else if (raycast.equals("coordinate")) {
            ServerLevel world = WorldUtil.getLevel(server.overworld(),
                    map.getString(session, "world"));

            if (world == null) {
                session.sendErrorMsg(MessageCode.LEVEL_NOT_FOUND, "找不到世界");
                return;
            }

            double x = map.getDouble(session, "x");
            double y = map.getDouble(session, "y");
            double z = map.getDouble(session, "z");
            float yaw = map.getFloat(session, "yaw");
            float pitch = map.getFloat(session, "pitch");

            HitResult result = server.submit(() -> WorldUtil.rayTrace(world, x, y, z, yaw, pitch, distance, false))
                    .join();

            sendResult(session, server, world, result);
        }else {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "无效的参数: raycast");
        }
    }

    private void sendResult(ChatSession session, MinecraftServer server, ServerLevel level, HitResult result) {
        switch (result.getType()) {
            case BLOCK:
                BlockHitResult blockResult = (BlockHitResult) result;
                BlockPos blockPos = blockResult.getBlockPos();
                MIPos miPos = MIUtil.buildMIPos(level, blockPos);

                MIBlock.BlockStatePack statePack = server.submit(() -> {
                    BlockState blockState = level.getBlockState(blockPos);
                    return BlockStateEventHandler
                            .createBlockStatePack(level, blockPos, miPos, blockState);
                }).join();
                session.sendMsg(new RaycastHitResultMessage(session.sessionId(), statePack));
                return;

            case ENTITY:
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity entity = entityResult.getEntity();
                MIEntity miEntity = MIUtil.buildMIEntity(entity);

                session.sendMsg(new RaycastHitResultMessage(session.sessionId(), miEntity));
                return;

            default:
                session.sendMsg(new RaycastHitResultMessage(session.sessionId()));
        }
    }
}
