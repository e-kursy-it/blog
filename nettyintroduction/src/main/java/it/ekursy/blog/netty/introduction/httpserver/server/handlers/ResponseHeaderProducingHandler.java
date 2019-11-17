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

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

import it.ekursy.blog.netty.introduction.httpserver.server.event.FileAvailableEvent;
import it.ekursy.blog.netty.introduction.httpserver.server.event.FileRangeRequestEvent;
import it.ekursy.blog.netty.introduction.httpserver.server.event.FileRangeRequestEvent.Range;

public class ResponseHeaderProducingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final Logger logger = LogManager.getLogger();

    private final Path filesLocation;

    /**
     * @param filesLocation
     */
    public ResponseHeaderProducingHandler(Path filesLocation)
    {
        this.filesLocation = filesLocation;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        // wait until response is produced
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception
    {
        try {

            if ( !HttpMethod.GET.equals( fullHttpRequest.method() ) ) {
                logger.warn( "Got invalid request: {}", fullHttpRequest.method() );
                sendError( channelHandlerContext, HttpResponseStatus.METHOD_NOT_ALLOWED );
                return;
            }
            var path = Paths.get( filesLocation.toString(), fullHttpRequest.uri() );

            try {
                // simple protection agains FS wide requests
                var realPath = path.toRealPath( LinkOption.NOFOLLOW_LINKS );
                if ( Files.isDirectory( path ) || !realPath.startsWith( filesLocation.toAbsolutePath().toString() ) ) {
                    throw new NoSuchFileException( path.toString() );
                }
            }
            catch ( NoSuchFileException e ) {
                sendError( channelHandlerContext, HttpResponseStatus.NOT_FOUND );
                return;
            }

            if ( !path.toString().endsWith( ".mp4" ) ) {
                logger.warn( "Path does not end with .mp4: '{}'", path );
                sendError( channelHandlerContext, HttpResponseStatus.BAD_REQUEST );
                return;
            }

            logger.info( "Start download: {}", path );
            var rangeHeaderValue = fullHttpRequest.headers().get( HttpHeaderNames.RANGE );
            var keepAlive = HttpUtil.isKeepAlive( fullHttpRequest );

            if ( rangeHeaderValue == null ) {

                var response = new DefaultHttpResponse( HTTP_1_1, OK );
                HttpUtil.setContentLength( response, Files.size( path ) );
                HttpUtil.setKeepAlive( response, true );
                response.headers().set( HttpHeaderNames.CONTENT_TYPE, "video/mp4" );
                response.headers().set( HttpHeaderNames.ACCEPT_RANGES, HttpHeaderValues.BYTES );
                channelHandlerContext.write( response );

                channelHandlerContext.fireChannelActive();
                channelHandlerContext.fireUserEventTriggered( new FileAvailableEvent( path, keepAlive ) );
            }
            else {
                logger.info( "found ranges: {}", rangeHeaderValue );

                channelHandlerContext.fireChannelActive();

                var ranges = Arrays.stream( rangeHeaderValue.replace( "bytes=", "" ).split( "-" ) ).mapToLong( Long::valueOf ).toArray();

                if ( ranges.length < 2 ) {
                    var response = new DefaultHttpResponse( HTTP_1_1, OK );
                    HttpUtil.setContentLength( response, Files.size( path ) );
                    response.headers().set( HttpHeaderNames.CONTENT_TYPE, "video/mp4" );
                    response.headers().set( HttpHeaderNames.ACCEPT_RANGES, HttpHeaderValues.BYTES );
                    channelHandlerContext.write( response );

                    channelHandlerContext.fireChannelActive();
                    channelHandlerContext.fireUserEventTriggered( new FileAvailableEvent( path, keepAlive ) );
                }
                else {
                    var range = new Range( ranges[ 0 ], ranges[ 1 ] );
                    if ( range.getSize() <= 0 ) {
                        sendError( channelHandlerContext, HttpResponseStatus.BAD_REQUEST );
                    }
                    else {
                        channelHandlerContext.fireChannelRead( new FileRangeRequestEvent( path, range, keepAlive ) );
                    }
                }
            }
        }
        catch ( Exception e ) {
            logger.error( "error", e );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        super.exceptionCaught( ctx, cause );
        logger.error( "error", cause );
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status)
    {
        var response = new DefaultFullHttpResponse( HTTP_1_1, status, Unpooled.copiedBuffer( "Failure: " + status + "\r\n", CharsetUtil.UTF_8 ) );
        response.headers().set( HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8" );
        ctx.writeAndFlush( response ).addListener( ChannelFutureListener.CLOSE );
    }

}
