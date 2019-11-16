/**
 * Copyright 2019 Marek Będkowski
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package it.ekursy.blog.netty.introduction.httpserver.server.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.PARTIAL_CONTENT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.stream.ChunkedFile;

import it.ekursy.blog.netty.introduction.httpserver.server.event.FileRangeRequestEvent;

public class RangeResponseProducingHandler extends MessageToMessageDecoder<FileRangeRequestEvent> {

    private final Logger logger = LogManager.getLogger();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, FileRangeRequestEvent fileRangeRequestEvent, List<Object> list) throws Exception
    {
        try {
            var range = fileRangeRequestEvent.getRange();
            var path = fileRangeRequestEvent.getPath();
            var fileSize = Files.size( path );
            var contentRangeBytes = String.format( "bytes %d-%d/%d", range.getStart(), range.getEnd(), fileSize );

            logger.info( "producing byte: {}", contentRangeBytes );

            // some caching should be added on slower filesystems e.g. Amazon EFS
            var raf = new RandomAccessFile( path.toFile(), "r" );
            var chunkedFile = new ChunkedFile( raf, range.getStart(), raf.length(), range.getSize() );

            var buf = chunkedFile.readChunk( channelHandlerContext.alloc() );

            chunkedFile.close();

            logger.info( "producing content" );
            var response = new DefaultFullHttpResponse( HTTP_1_1, PARTIAL_CONTENT, buf );

            HttpUtil.setContentLength( response, range.getSize() );
            response.headers().set( HttpHeaderNames.CONTENT_TYPE, "video/mp4" );
            response.headers().set( HttpHeaderNames.ACCEPT_RANGES, HttpHeaderValues.BYTES );
            response.headers().set( HttpHeaderNames.CONTENT_RANGE, contentRangeBytes );

            logger.info( "writing response" );
            var writeFuture = channelHandlerContext.writeAndFlush( response );
            writeFuture.addListener( ChannelFutureListener.CLOSE );
        }
        catch ( Exception e ) {
            logger.error( "error", e );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        super.exceptionCaught( ctx, cause );
        logger.error( "exception", cause );
    }
}
