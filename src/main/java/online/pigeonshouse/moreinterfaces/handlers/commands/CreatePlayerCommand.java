package online.pigeonshouse.moreinterfaces.handlers.commands;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.handlers.MIServerPlayer;
import online.pigeonshouse.moreinterfaces.handlers.message.PlayerPosMessage;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.PowerUtil;
import online.pigeonshouse.moreinterfaces.utils.WorldUtil;

/**
 * 创建虚拟玩家
 */
public class CreatePlayerCommand implements CommandHandler {
    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        MinecraftServer minecraftServer = MoreInterfaces.MINECRAFT_SERVER.get();
        BlockPos spawnPos = minecraftServer.overworld().getSharedSpawnPos();

        String createPlayer = map.getString(session, "player");
        if (minecraftServer.getPlayerList().getPlayerByName(createPlayer) != null) {
            session.sendErrorMsg(MessageCode.INSTRUCTION_ERROR, "玩家已存在！");
            return;
        }

        String world = map.getStringOrDef("world", WorldUtil.getLevelName(minecraftServer.overworld()));
        String gameMode = map.getStringOrDef("game_mode", "survival");
        boolean flying = map.getBooleanOrDef("flying", false);
        double x = map.getDoubleOrDef(session, "x", spawnPos.getX());
        double y = map.getDoubleOrDef(session, "y", spawnPos.getY());
        double z = map.getDoubleOrDef(session, "z", spawnPos.getZ());
        double yaw = map.getDoubleOrDef(session, "yaw", 0);
        double pitch = map.getDoubleOrDef(session, "pitch", 0);

        ServerLevel level = WorldUtil.getLevel(minecraftServer.overworld(), world);
        if (level == null) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "无法找到世界：" + world);
            return;
        }

        GameType gameType = GameType.byName(gameMode);
        if (gameType != GameType.SURVIVAL &&
                !PowerUtil.getPower(session.token()).queryGroups("commands.message.gametype")) {
            session.sendErrorMsg(MessageCode.NO_PERMISSION, "您没有权限创建非生存模式玩家！");
            return;
        }

        if (flying &&
                !PowerUtil.getPower(session.token()).queryGroups("commands.message.gametype")) {
            session.sendErrorMsg(MessageCode.NO_PERMISSION, "您没有权限使用属性：flying");
            return;
        }

        minecraftServer.execute(() -> {
            MIPlayer fakePlayer =
                    MIServerPlayer.createFakePlayer(createPlayer, minecraftServer, level, gameType, flying,
                            x, y, z, yaw, pitch);
            session.sendMsg(new PlayerPosMessage(session.sessionId(), fakePlayer));
        });
    }
}
