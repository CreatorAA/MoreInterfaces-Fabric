package online.pigeonshouse.moreinterfaces.handlers.commands;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.eventhandler.BlockStateEventHandler;
import online.pigeonshouse.moreinterfaces.handlers.MIBlock;
import online.pigeonshouse.moreinterfaces.handlers.MIPos;
import online.pigeonshouse.moreinterfaces.handlers.message.BlocksMessage;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;
import online.pigeonshouse.moreinterfaces.utils.WorldUtil;

import java.util.List;

/**
 * 查找方块信息
 */
public class QueryBlockCommand implements CommandHandler {
    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();

        ServerLevel level = map.getStringOrDef("world", "").isEmpty() ? server.overworld() :
                WorldUtil.getLevel(server.overworld(), map.getStringOrDef("world", ""));

        if (level == null) {
            session.sendErrorMsg(MessageCode.LEVEL_NOT_FOUND, "世界不存在:" + map.get("world"));
            return;
        }

        BlockPos blockPos = new BlockPos(
                map.getInt(session, "x"),
                map.getInt(session, "y"),
                map.getInt(session, "z")
        );

        if (!level.isLoaded(blockPos)) {
            session.sendErrorMsg(MessageCode.CHUNK_NOT_LOADED, "指定区块未加载！");
            return;
        }

        BlockState blockState = level.getBlockState(blockPos);

        MIPos miPos = MIUtil.buildMIPos(level, blockPos);

        MIBlock.BlockStatePack blockStatePack = server.submit(() -> BlockStateEventHandler.createBlockStatePack(
                level, blockPos,
                miPos, blockState
        )).join();

        session.sendMsg(new BlocksMessage(session.sessionId(), List.of(blockStatePack)));
    }
}
