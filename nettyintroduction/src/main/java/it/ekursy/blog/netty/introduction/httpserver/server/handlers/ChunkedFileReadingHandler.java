/**
 * Copyright 2019 Marek Będkowski
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package it.ekursy.blog.netty.introduction.httpserver.server.handlers;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.stream.ChunkedFile;

import it.ekursy.blog.netty.introduction.httpserver.server.event.FileAvailableEvent;
import it.ekursy.blog.netty.introduction.httpserver.server.event.FileRangeRequestEvent;

public class ChunkedFileReadingHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        super.userEventTriggered( ctx, evt );
        if ( evt instanceof FileAvailableEvent ) {
            startFileProcessing( ctx, (FileAvailableEvent) evt );
        }
        else if ( evt instanceof FileRangeRequestEvent ) {
            ctx.pipeline().remove( this );
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        logger.info( "Channel closed" );
    }

    private void startFileProcessing(ChannelHandlerContext ctx, FileAvailableEvent evt)
    {
        logger.info( "Start processing file" );
        try {
            // DefaultFileRegion misbehaves when using byte-range requests
            logger.debug("Opening file: {}", evt.getPath() );
            var raf = new RandomAccessFile( evt.getPath().toFile(), "r" );
            var fileLength = raf.length();
            logger.debug( "file opened, size: {} ", fileLength);
            var chunkedFile = new ChunkedFile( raf, 0, fileLength, 8192 );
            logger.debug( "chunked file created " );
            var httpChunkedInput = new HttpChunkedInput( chunkedFile );
            logger.debug( "Chunked input created" );
            var sendFileFuture = ctx.writeAndFlush( httpChunkedInput, ctx.newProgressivePromise() );
            logger.debug( "Write and flush called - producing response" );

            showProgress( sendFileFuture );

            if ( !evt.isKeepAlive() ) {
                logger.debug( "keep-alive: false - adding close handler" );
                sendFileFuture.addListener( ChannelFutureListener.CLOSE );
            }
        }
        catch ( IOException ignore ) {
            logger.error( "error", ignore );
            ctx.channel().close();
            return;
        }

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
