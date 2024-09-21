package online.pigeonshouse.moreinterfaces.handlers.commands;

import net.minecraft.server.MinecraftServer;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.handlers.message.PlayerListMessage;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;

/**
 * 获取在线玩家列表
 */
public class GetPlayerListCommand implements CommandHandler {
    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        MinecraftServer minecraftServer = MoreInterfaces.MINECRAFT_SERVER.get();
        MIPlayer[] players = minecraftServer.getPlayerList().getPlayers()
                .stream()
                .map(MIUtil::buildMIPlayer)
                .toList().toArray(new MIPlayer[0]);

        session.sendMsg(new PlayerListMessage(session.sessionId(), players));
    }
}
