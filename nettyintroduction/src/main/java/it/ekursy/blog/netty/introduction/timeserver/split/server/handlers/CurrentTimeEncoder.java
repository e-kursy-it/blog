package it.ekursy.blog.netty.introduction.timeserver.split.server.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import it.ekursy.blog.netty.introduction.timeserver.split.pojo.UnixTime;

public class CurrentTimeEncoder extends MessageToByteEncoder<UnixTime> {

    private final Logger logger = LogManager.getLogger();

    @Override
    protected void encode(ChannelHandlerContext ctx, UnixTime unixTime, ByteBuf byteBuf) throws Exception
    {
        var now = unixTime.getTime();
        byteBuf.writeInt( now );

        logger.info( "writing time" );
    }
}
