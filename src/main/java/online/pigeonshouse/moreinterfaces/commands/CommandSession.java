package online.pigeonshouse.moreinterfaces.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import online.pigeonshouse.moreinterfaces.config.RemoteConfig;
import online.pigeonshouse.moreinterfaces.config.RemoteToken;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.message.Message;
import online.pigeonshouse.moreinterfaces.utils.GsonUtil;

import java.util.function.Function;

public class CommandSession extends ChatSession {
    final CommandSourceStack source;

    public CommandSession(CommandSourceStack source) {
        super(null, getSessionId(source), getToken(source));
        this.source = source;
    }

    private static String getSessionId(CommandSourceStack source) {
        if (!source.isPlayer()) {
            throw new IllegalStateException("CommandSourceStack must be a player");
        }

        return source.getPlayer().getStringUUID();
    }

    private static RemoteToken getToken(CommandSourceStack source) {
        if (!source.isPlayer()) {
            throw new IllegalStateException("CommandSourceStack must be a player");
        }

        String uuid = source.getPlayer().getStringUUID();

        // 实际上权限应该为2，这里的0为调试模式使用
        return source.hasPermission(4) ? RemoteConfig.Config.INSTANCE.getRootToken(uuid) :
                new RemoteToken(uuid, 0);
    }

    @Override
    public void sendMsg(Message message, Function<ChatSession, Void> callback) {
        String json = GsonUtil.FORMATTER_GSON.toJson(message);

        MutableComponent text = Component.literal(message.getClass().getSimpleName())
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE)
                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.literal(json))))
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, json)));

        MutableComponent result = Component.literal("result: ")
                .withStyle(ChatFormatting.GREEN)
                .append(text);

        source.sendSystemMessage(result);
    }

    @Override
    public void sendErrorMsg(int errorCode, String errorMsg) {
        MutableComponent text = Component.literal(errorMsg)
                .withStyle(ChatFormatting.RED);
        source.sendSystemMessage(text);
    }
}
