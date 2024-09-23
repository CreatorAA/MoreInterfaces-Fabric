package online.pigeonshouse.moreinterfaces.handlers.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.command.CommandArgException;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.CommandUtil;
import online.pigeonshouse.moreinterfaces.utils.PowerUtil;
import online.pigeonshouse.moreinterfaces.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendPlayerMsgCommand implements CommandHandler {
    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);

    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        MinecraftServer minecraftServer = MoreInterfaces.MINECRAFT_SERVER.get();
        boolean all = map.getBooleanOrDef("all", false);

        boolean overlay = map.getBooleanOrDef("overlay", false);
        boolean title = map.getBooleanOrDef("title", false);

        if ((overlay || title) &&
                !PowerUtil.getPower(session.token()).queryGroups("commands.message.screen")) {

            session.sendErrorMsg(MessageCode.NO_PERMISSION, "您没有权限使用overlay或title构建消息！");
            return;
        }

        String msg = map.getString(session, "text");
        Component text = StringUtil.getText(msg);

        if (all) {
            for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
                sendMessage(session, overlay, title, text, serverPlayer);
            }

            session.sendSucceededMessage();
            return;
        }

        String string = map.getStringOrDef("player", null);

        if (string == null) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，缺少参数：player");
            throw new CommandArgException();
        }

        ServerPlayer player = minecraftServer.getPlayerList()
                .getPlayerByName(string);

        if (player == null) {
            session.sendErrorMsg(MessageCode.ENTITY_NOT_FOUND, "玩家不存在！");
            return;
        }

        sendMessage(session, overlay, title, text, player);
        session.sendSucceededMessage();
    }

    private static void sendMessage(ChatSession session, boolean overlay, boolean title, Component text, ServerPlayer player) {
        if (title) {
            CommandSourceStack userSourceStack = (CommandSourceStack) session.getOrAdd("user_source_stack",
                    CommandUtil.getUserCommandSourceStack(session));

            try {
                player.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.TITLE,
                        ComponentUtils.updateForEntity(userSourceStack, text, player, 0)));
            } catch (CommandSyntaxException e) {
                log.error(e);
                session.sendErrorMsg(MessageCode.SYNTAX_ERROR, "消息语法错误！");
            }
        } else {
            player.displayClientMessage(text, overlay);
        }
    }
}
