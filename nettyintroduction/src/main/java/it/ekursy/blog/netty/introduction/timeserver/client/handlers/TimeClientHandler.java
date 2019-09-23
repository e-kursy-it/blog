package it.ekursy.blog.netty.introduction.timeserver.client.handlers;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        logger.info( "Received message" );
        var buffer = (ByteBuf) msg;
        try {
            var currentTimeMillis = ( buffer.readUnsignedInt() - 2208988800L ) * 1000L;
            logger.info( new Date( currentTimeMillis ) );
            ctx.close();
        }
        finally {
            buffer.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}