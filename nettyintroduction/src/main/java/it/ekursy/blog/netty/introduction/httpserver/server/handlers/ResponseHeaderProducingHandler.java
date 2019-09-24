package it.ekursy.blog.netty.introduction.httpserver.server.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.file.Files;
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
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

import it.ekursy.blog.netty.introduction.httpserver.server.event.FileAvailableEvent;
import it.ekursy.blog.netty.introduction.httpserver.server.event.FileRangeRequestEvent;

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

            var range = fullHttpRequest.headers().get( "range" );
            var path = Paths.get( filesLocation.toString(), fullHttpRequest.uri() );

            if ( range == null ) {

                logger.info( "Start download: {}", path );
                if ( !Files.exists( path ) || Files.isDirectory( path ) ) {
                    sendError( channelHandlerContext, HttpResponseStatus.NOT_FOUND );
                    return;
                }

                if ( !path.toString().endsWith( ".mp4" ) ) {
                    logger.warn( "Path does not end with .mp4: '{}'", path );
                    sendError( channelHandlerContext, HttpResponseStatus.BAD_REQUEST );
                    return;
                }

                try {
                    var response = new DefaultHttpResponse( HTTP_1_1, OK );
                    HttpUtil.setContentLength( response, Files.size( path ) );
                    setContentTypeHeader( response, path );
                    channelHandlerContext.write( response );

                    channelHandlerContext.fireChannelActive();
                    channelHandlerContext.fireUserEventTriggered( new FileAvailableEvent( path ) );
                }
                finally {
                    //                    channelHandlerContext.channel().pipeline().remove( this );
                }
            }
            else {
                logger.info( "found ranges: {}", range );

                fullHttpRequest.retain();
                channelHandlerContext.fireChannelActive();

                int[] ranges = Arrays.stream( range.replace( "bytes=", "" ).split( "-" ) ).mapToInt( Integer::valueOf ).toArray();

                if ( ranges.length < 2 ) {
                    var response = new DefaultHttpResponse( HTTP_1_1, OK );
                    HttpUtil.setContentLength( response, Files.size( path ) );
                    setContentTypeHeader( response, path );
                    channelHandlerContext.write( response );

                    channelHandlerContext.fireChannelActive();
                    channelHandlerContext.fireUserEventTriggered( new FileAvailableEvent( path ) );

                }
                else {
                    channelHandlerContext.fireChannelRead( new FileRangeRequestEvent( path, ranges ) );
                }
            }
        }
        catch ( Exception e ) {
            logger.error( "erro", e );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        super.exceptionCaught( ctx, cause );
        logger.error( cause );
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status)
    {
        var response = new DefaultFullHttpResponse( HTTP_1_1, status, Unpooled.copiedBuffer( "Failure: " + status + "\r\n", CharsetUtil.UTF_8 ) );
        response.headers().set( HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8" );
        ctx.writeAndFlush( response ).addListener( ChannelFutureListener.CLOSE );
    }

    private void setContentTypeHeader(HttpResponse response, Path path)
    {
        response.headers().set( HttpHeaderNames.CONTENT_TYPE, "video/mp4" );
    }

}
