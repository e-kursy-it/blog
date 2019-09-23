package it.ekursy.blog.netty.introduction.httpserver.server.handlers;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.stream.ChunkedFile;

import it.ekursy.blog.netty.introduction.httpserver.server.event.FileAvailableEvent;

public class ChunkedFileReadingHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        super.userEventTriggered( ctx, evt );
        if ( evt instanceof FileAvailableEvent ) {
            startFileProcessing( ctx, (FileAvailableEvent) evt );
        }
    }

    private void startFileProcessing(ChannelHandlerContext ctx, FileAvailableEvent evt)
    {
        RandomAccessFile raf;
        ChunkedFile chunkedFile;
        try {
            raf = new RandomAccessFile( evt.getPath().toFile(), "r" );
            chunkedFile = new ChunkedFile( raf, 0, raf.length(), 8192 );
        }
        catch ( IOException ignore ) {
            ctx.channel().close();
            return;
        }

        var sendFileFuture = ctx.writeAndFlush( new HttpChunkedInput( chunkedFile ), ctx.newProgressivePromise() );

        showProgress( sendFileFuture );
    }

    private void showProgress(ChannelFuture sendFileFuture)
    {
        sendFileFuture.addListener( new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total)
            {
                if ( total < 0 ) { // total unknown
                    logger.info( future.channel() + " Transfer progress: " + progress );
                }
                else {
                    logger.info( future.channel() + " Transfer progress: " + progress + " / " + total );
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future)
            {
                logger.info( future.channel() + " Transfer complete." );
            }
        } );
    }
}
