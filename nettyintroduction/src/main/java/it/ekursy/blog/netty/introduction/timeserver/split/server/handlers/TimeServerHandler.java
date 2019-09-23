package it.ekursy.blog.netty.introduction.timeserver.split.server.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import it.ekursy.blog.netty.introduction.timeserver.split.pojo.UnixTime;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        var currentUnixTime = new UnixTime();
        var time = ctx.alloc().buffer( 4 );

        time.writeInt( currentUnixTime.getTime() );

        logger.info( "writing time" );
        var writeFuture = ctx.writeAndFlush( time );
        writeFuture.addListener( ChannelFutureListener.CLOSE );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}