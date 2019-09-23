package it.ekursy.blog.netty.introduction.timeserver.split.server.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import it.ekursy.blog.netty.introduction.timeserver.split.pojo.UnixTime;

public class CurrentTimeHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelActive( ctx );

        var currentUnixTime = new UnixTime();

        var writeFuture = ctx.writeAndFlush( currentUnixTime );

        writeFuture.addListener( ChannelFutureListener.CLOSE );
    }
}
