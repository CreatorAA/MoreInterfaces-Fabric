package online.pigeonshouse.moreinterfaces.netty;

import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.netty.message.Message;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.netty.message.NotSessionErrorMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    public static final byte[] MODS = MoreInterfaces.MOD_ID.getBytes();
    public static final byte[] VERSION = "0.0.1".getBytes();
    private static final HashMap<String,Byte> custom = new HashMap<>();
    public static final int HEADER_LENGTH = MODS.length + VERSION.length + 2 + 1 + 1 + 4 + 4;
    private static final Logger log = LogManager.getLogger(MessageCodecSharable.class);

    private final SerializeFactory factory = SerializeFactory.getFactory();

    public static void addCustom(Class<? extends Message> obj, Byte type){
        custom.put(obj.getName(),type);
    }

    public static void remove(Class<? extends Message> obj){
        custom.remove(obj.getName());
    }

    private byte lastType = SerializeFactory.JsonSerialize.JSON;

    @Override
    protected void encode(ChannelHandlerContext ct, Message message, List<Object> list) throws Exception {
        Byte serializeByte = custom.get(message.getClass().getName());
        byte type = serializeByte == null ? lastType : serializeByte;

        ByteBuf buf = ct.alloc().buffer();
        buf.writeBytes(MODS);
        buf.writeBytes(VERSION);
        buf.writeChar('\n');
        buf.writeByte(type);
        buf.writeByte(0xff);
        byte[] serialize = factory.get(type)
                .serialize(message);
        buf.writeInt(message.getType());
        buf.writeInt(serialize.length);
        buf.writeBytes(serialize);
        list.add(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext ct, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] mods = new byte[MODS.length];
        byte[] version = new byte[VERSION.length];
        byteBuf.readBytes(mods);
        byteBuf.readBytes(version);
        byteBuf.skipBytes(2);

        byte type = byteBuf.readByte();
        Serialize serialize = factory.get(type);

        if (serialize == null){
            ct.channel().close();
            log.error("Netty Decode Error: Serialize is null!", new RuntimeException());
            return;
        }

        lastType = type;

        byteBuf.readByte();
        Class<? extends Message> o = MessageFactory.valueOf(byteBuf.readInt());
        int length = byteBuf.readInt();
        byte[] obj = new byte[length];
        byteBuf.readBytes(obj,0,length);
        Message deserialize = serialize.deserialize(o, obj);
        list.add(deserialize);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof JsonSyntaxException){
            ctx.channel()
                    .writeAndFlush(new NotSessionErrorMessage(MessageCode.JSON_DESERIALIZE_ERROR,
                            "Json Deserialize Error:" + cause.getMessage()));
            cause.printStackTrace();
            return;
        }

        super.exceptionCaught(ctx, cause);
    }
}