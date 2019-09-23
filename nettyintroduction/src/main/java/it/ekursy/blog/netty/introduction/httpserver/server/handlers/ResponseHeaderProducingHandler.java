package it.ekursy.blog.netty.introduction.httpserver.server.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

import it.ekursy.blog.netty.introduction.httpserver.server.event.FileAvailableEvent;

public class ResponseHeaderProducingHandler extends MessageToMessageDecoder<FullHttpRequest> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        // wait until response is produced
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, List<Object> list) throws Exception
    {
        if ( !HttpMethod.GET.equals( fullHttpRequest.method() ) ) {
            sendError( channelHandlerContext, HttpResponseStatus.METHOD_NOT_ALLOWED );
            return;
        }

        var path = Paths.get( fullHttpRequest.uri() );

        if ( !Files.exists( path ) ) {
            sendError( channelHandlerContext, HttpResponseStatus.NOT_FOUND );
            return;
        }

        try {
            var response = new DefaultHttpResponse( HTTP_1_1, OK );
            HttpUtil.setContentLength( response, Files.size( path ) );

            channelHandlerContext.write( response );

            channelHandlerContext.fireChannelActive();
            channelHandlerContext.fireUserEventTriggered( new FileAvailableEvent( path ) );
        }
        finally {
            channelHandlerContext.channel().pipeline().remove( this );
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status)
    {
        var response = new DefaultFullHttpResponse( HTTP_1_1, status, Unpooled.copiedBuffer( "Failure: " + status + "\r\n", CharsetUtil.UTF_8 ) );
        response.headers().set( HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8" );
        ctx.writeAndFlush( response ).addListener( ChannelFutureListener.CLOSE );
    }
}
