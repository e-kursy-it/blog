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
        } else if (evt instanceof FileRangeRequestEvent ) {
            ctx.pipeline().remove( this );
        }
    }

    private void startFileProcessing(ChannelHandlerContext ctx, FileAvailableEvent evt)
    {
        logger.info( "Start processing file" );
        RandomAccessFile raf;
        ChunkedFile chunkedFile;
        try {
            raf = new RandomAccessFile( evt.getPath().toFile(), "r" );
            chunkedFile = new ChunkedFile( raf, 0, raf.length(), 8192 );
            var httpChunks = new HttpChunkedInput( chunkedFile );
            var sendFileFuture = ctx.writeAndFlush( httpChunks, ctx.newProgressivePromise() );

            showProgress( sendFileFuture );
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
