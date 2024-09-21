package online.pigeonshouse.moreinterfaces.netty.command;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.config.RemoteToken;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.ConnectionManage;
import online.pigeonshouse.moreinterfaces.netty.message.CommandMessage;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.netty.message.NotSessionErrorMessage;
import online.pigeonshouse.moreinterfaces.utils.ChannelUtil;
import online.pigeonshouse.moreinterfaces.utils.PowerUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ChannelHandler.Sharable
public class CommandMessageHandler extends SimpleChannelInboundHandler<CommandMessage> {
    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CommandMessage msg) throws Exception {
        if (msg.getSessionId() == null || msg.getSessionId().isBlank()) {
            ctx.channel().writeAndFlush(new NotSessionErrorMessage(MessageCode.SESSION_ERROR, "SessionId is null or blank"))
                    .sync();
            return;
        }

        if (msg.getToken() == null || msg.getToken().isBlank()) {
            ChannelUtil.sendErrorMessage(ctx.channel(), msg.getSessionId(),
                    MessageCode.AUTH_ERROR, "Token is null or blank");
            return;
        }

        if (msg.getCommand() == null || msg.getCommand().isBlank()) {
            ChannelUtil.sendErrorMessage(ctx.channel(), msg.getSessionId(),
                    MessageCode.INSTRUCTION_ERROR, "Command is null or blank");
            return;
        }

        RemoteToken remoteToken = PowerUtil.getToken(msg.getToken());
        if (Objects.isNull(remoteToken)) {
            ChannelUtil.sendErrorMessage(ctx.channel(), msg.getSessionId(),
                    MessageCode.AUTH_ERROR, "Token is invalid!");
            return;
        }

        CommandHandler commandHandler = CommandManage.getCommand(msg.getToken(), msg.getCommand());

        if (commandHandler == null) {
            ChannelUtil.sendErrorMessage(ctx.channel(), msg.getSessionId(),
                    MessageCode.INSTRUCTION_ERROR, "Command not found!");
            return;
        }

        ChatSession session = new ChatSession(ctx.channel(), msg.getSessionId(), remoteToken);

        Map<String, Object> data = Optional.ofNullable(msg.getData())
                .orElse(new HashMap<>());

        MoreInterfaces.MORE_INTERFACES.get().runThread(() -> {
            try {
                commandHandler.onCommand(session, new CommandMap(data));
            } catch (Exception e) {
                if (e instanceof CommandArgException) return;
                log.error("Command error", e);
                session.sendErrorMsg(MessageCode.UNKNOWN_ERROR, "Unknown error");
            }
        });
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        log.info("Client connected: {}", ctx.channel().remoteAddress());
        ConnectionManage.addUser(ctx.channel());
    }
}
