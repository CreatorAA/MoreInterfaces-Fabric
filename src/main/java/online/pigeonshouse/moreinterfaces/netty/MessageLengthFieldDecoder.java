package online.pigeonshouse.moreinterfaces.netty;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MessageLengthFieldDecoder extends LengthFieldBasedFrameDecoder {
    public MessageLengthFieldDecoder(){
        this(500 * 1024,MessageCodecSharable.HEADER_LENGTH - 4,4,0,0);
    }

    public MessageLengthFieldDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}