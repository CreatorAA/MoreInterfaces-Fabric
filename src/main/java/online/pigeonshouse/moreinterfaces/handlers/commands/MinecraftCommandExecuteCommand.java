package online.pigeonshouse.moreinterfaces.handlers.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.commands.MICommands;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.CommandUtil;
import online.pigeonshouse.moreinterfaces.utils.PowerUtil;

public class MinecraftCommandExecuteCommand implements CommandHandler {
    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();

        boolean op = map.getBooleanOrDef("op", false);
        String command = map.getString(session, "commands");
        if (command.isBlank()) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "cmd命令参数不能为空");
        }

        CommandSourceStack sourceStack;

        if (op) {
            if (!PowerUtil.getPower(session.token()).queryGroups("commands.minecraft.op")) {
                session.sendErrorMsg(MessageCode.NO_PERMISSION, "您没有权限使用op指令");
                return;
            }

            sourceStack = (CommandSourceStack) session.getOrAdd("server_source_stack",
                    CommandUtil.getServerCommandSourceStack(session));
        } else {
            sourceStack = (CommandSourceStack) session.getOrAdd("user_source_stack",
                    CommandUtil.getUserCommandSourceStack(session));
        }

        Commands commands = MICommands.COMMANDS.get();

        try {
            commands.performPrefixedCommand(sourceStack, command);
        } catch (Exception e) {
            session.sendErrorMsg(MessageCode.UNKNOWN_ERROR, e.getMessage());
        }
    }
}
