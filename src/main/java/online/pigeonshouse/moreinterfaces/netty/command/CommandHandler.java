package online.pigeonshouse.moreinterfaces.netty.command;

import online.pigeonshouse.moreinterfaces.netty.ChatSession;

public interface CommandHandler {
    void onCommand(ChatSession session, CommandMap map);
}
