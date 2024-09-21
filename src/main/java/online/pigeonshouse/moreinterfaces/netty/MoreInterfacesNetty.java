package online.pigeonshouse.moreinterfaces.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import online.pigeonshouse.moreinterfaces.config.RemoteConfig;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMessageHandler;
import online.pigeonshouse.moreinterfaces.netty.message.Message;
import online.pigeonshouse.moreinterfaces.netty.message.PingMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MoreInterfacesNetty extends ChannelInitializer<NioSocketChannel> {
    private static final Logger log = LogManager.getLogger(MoreInterfacesNetty.class);
    private final RemoteConfig config;
    private Channel channel;

    private final AtomicBoolean isStart = new AtomicBoolean(false);
    private ServerBootstrap serverBootstrap;
    private NioEventLoopGroup bossGroup, workerGroup;

    public MoreInterfacesNetty(RemoteConfig config) {
        this.config = config;
    }

    public void start() {
        if (isStart.get()) {
            synchronized (isStart) {
                if (isStart.get()) {
                    throw new RuntimeException("netty is already start");
                }
            }
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                .channel(NioServerSocketChannel.class)
                .childHandler(this)
                .group(bossGroup, workerGroup);

        try {
            ChannelFuture channelFuture = serverBootstrap.bind(config.getPort())
                    .sync();

            isStart.set(true);
            channel = channelFuture.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (!isStart.get()) {
            return;
        }

        Optional.ofNullable(channel)
                .ifPresent(channelFuture1 -> {
                    try {
                        channelFuture1.close().sync();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                });

        isStart.set(false);
    }

    public void sendMessage(Message msg) throws InterruptedException {
        Objects.requireNonNull(channel, "channelFuture is null");

        channel.writeAndFlush(msg).sync();
    }

    public static final MessageCodecSharable CODEC = new MessageCodecSharable();
    public static final TimeOutHandler TIME_OUT_HANDLER = new TimeOutHandler();
    public static final CommandMessageHandler COMMAND_MESSAGE_HANDLER = new CommandMessageHandler();

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new MessageLengthFieldDecoder())
                .addLast(CODEC)
                .addLast(new IdleStateHandler(10, 5, 0, TimeUnit.MINUTES))
                .addLast(TIME_OUT_HANDLER)
                .addLast(COMMAND_MESSAGE_HANDLER);

        pipeline.channel().writeAndFlush(new PingMessage());
    }

    @ChannelHandler.Sharable
    public static class TimeOutHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.READER_IDLE) {
                    log.info("Connection idle, close it {}", ctx.channel().remoteAddress());
                    ConnectionManage.removeUser(ctx.channel());
                }else if (event.state() == IdleState.WRITER_IDLE) {
                    ctx.channel().writeAndFlush(new PingMessage());
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        /**
         * 用户断开连接
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("Connection closed, close it {}", ctx.channel().remoteAddress());
            ConnectionManage.removeUser(ctx.channel());
        }
    }
}
