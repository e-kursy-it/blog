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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

public class HealthCheckHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception
    {
        if ( !HttpMethod.GET.equals( fullHttpRequest.method() ) ) {
            sendError( channelHandlerContext, HttpResponseStatus.METHOD_NOT_ALLOWED );
            return;
        }

        var keepAlive = HttpUtil.isKeepAlive( fullHttpRequest );
        var path = fullHttpRequest.uri();

        if ( "/".equals( path ) || "".equals( path ) ) {
            var response = new DefaultHttpResponse( HTTP_1_1, OK );
            HttpUtil.setContentLength( response, 0 );
            response.headers().set( HttpHeaderNames.ACCEPT_RANGES, "byte" );

            response.headers().set( HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8" );
            var writeFuture = channelHandlerContext.writeAndFlush( response );
            if ( !keepAlive ) {
                writeFuture.addListener( ChannelFutureListener.CLOSE );
            }
        }
        else {
            channelHandlerContext.fireChannelRead( fullHttpRequest.retain() );
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status)
    {
        var response = new DefaultFullHttpResponse( HTTP_1_1, status, Unpooled.copiedBuffer( "Failure: " + status + "\r\n", CharsetUtil.UTF_8 ) );
        response.headers().set( HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8" );
        ctx.writeAndFlush( response ).addListener( ChannelFutureListener.CLOSE );
    }

}
