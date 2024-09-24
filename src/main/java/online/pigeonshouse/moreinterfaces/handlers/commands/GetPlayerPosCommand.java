package online.pigeonshouse.moreinterfaces.handlers.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.handlers.message.PlayerPosMessage;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;

/**
 * 获取玩家位置信息
 */
public class GetPlayerPosCommand implements CommandHandler {
    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        MinecraftServer minecraftServer = MoreInterfaces.MINECRAFT_SERVER.get();
        ServerPlayer player = minecraftServer.getPlayerList()
                .getPlayerByName(map.getString(session, "player"));

        if (player == null) {
            session.sendErrorMsg(MessageCode.ENTITY_NOT_FOUND, "玩家不存在！");
            return;
        }

        MIPlayer miPlayer = MIUtil.buildMIPlayer(player);
        session.sendMsg(new PlayerPosMessage(session.sessionId(), miPlayer));
    }
}
