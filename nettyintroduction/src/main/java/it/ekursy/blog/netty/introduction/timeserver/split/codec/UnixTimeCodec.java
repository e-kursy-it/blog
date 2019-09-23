package it.ekursy.blog.netty.introduction.timeserver.split.codec;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import it.ekursy.blog.netty.introduction.timeserver.split.pojo.UnixTime;

public class UnixTimeCodec extends ByteToMessageCodec<UnixTime> {

    private final Logger logger = LogManager.getLogger();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, UnixTime unixTime, ByteBuf byteBuf) throws Exception
    {
        var now = unixTime.getTime();
        byteBuf.writeInt( now );

        logger.info( "writing time" );
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception
    {
        if ( byteBuf.readableBytes() >= 4 ) {
            var time = byteBuf.readUnsignedInt();

            var currentTime = UnixTime.fromTime( time );

            out.add( currentTime );
        }
    }
}
