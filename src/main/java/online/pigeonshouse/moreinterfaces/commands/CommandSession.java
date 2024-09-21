package online.pigeonshouse.moreinterfaces.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import online.pigeonshouse.moreinterfaces.config.RemoteConfig;
import online.pigeonshouse.moreinterfaces.config.RemoteToken;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.message.Message;
import online.pigeonshouse.moreinterfaces.utils.ComponentUtil;
import online.pigeonshouse.moreinterfaces.utils.GsonUtil;

import java.util.function.Function;

public class CommandSession extends ChatSession {
    final CommandSourceStack source;

    public CommandSession(CommandSourceStack source) {
        super(null, getSessionId(source), getToken(source));
        this.source = source;
    }

    private static String getSessionId(CommandSourceStack source) {
        try {
            return source.getPlayerOrException().getStringUUID();
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static RemoteToken getToken(CommandSourceStack source) {
        String uuid;
        try {
            uuid = source.getPlayerOrException().getStringUUID();
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        return source.hasPermission(4) ? RemoteConfig.Config.INSTANCE.getRootToken(uuid) :
                new RemoteToken(uuid, 0);
    }

    @Override
    public void sendMsg(Message message, Function<ChatSession, Void> callback) {
        String json = GsonUtil.FORMATTER_GSON.toJson(message);

        MutableComponent text = ComponentUtil.literal(message.getClass().getSimpleName())
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE)
                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        ComponentUtil.literal(json))))
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, json)));

        MutableComponent result = ComponentUtil.literal("result: ")
                .withStyle(ChatFormatting.GREEN)
                .append(text);

        source.sendSuccess(result, false);
    }

    @Override
    public void sendErrorMsg(int errorCode, String errorMsg) {
        MutableComponent text = ComponentUtil.literal(errorMsg)
                .withStyle(ChatFormatting.RED);
        source.sendSuccess(text, false);
    }
}
