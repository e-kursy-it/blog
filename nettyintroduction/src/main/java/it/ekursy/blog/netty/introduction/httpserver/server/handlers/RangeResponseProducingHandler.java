package it.ekursy.blog.netty.introduction.httpserver.server.handlers;

import static io.netty.handler.codec.http.HttpResponseStatus.PARTIAL_CONTENT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;

import it.ekursy.blog.netty.introduction.httpserver.server.event.FileRangeRequestEvent;

public class RangeResponseProducingHandler extends MessageToMessageDecoder<FileRangeRequestEvent> {

    private final Logger logger = LogManager.getLogger();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, FileRangeRequestEvent fileRangeRequestEvent, List<Object> list) throws Exception
    {
        try {
            var range = fileRangeRequestEvent.getRange();
            var length = ( range[ 1 ] - range[ 0 ] ) + 1;
            var path = fileRangeRequestEvent.getPath();

            HttpResponse response;
            if ( length > 0 ) {

                var raf = new RandomAccessFile( path.toFile(), "r" );
                var output = new byte[ length ];
                raf.read( output, range[ 0 ], length );

                raf.close();

                logger.info( "producting content" );
                var buf = Unpooled.wrappedBuffer( output );
                response = new DefaultFullHttpResponse( HTTP_1_1, PARTIAL_CONTENT, buf );
            }
            else {
                response = new DefaultFullHttpResponse( HTTP_1_1, PARTIAL_CONTENT );
            }
            var fileSize = Files.size( path );
            var bytes = String.format( "bytes %d-%d/%d", range[ 0 ], range[ 1 ], fileSize );

            logger.info( "producing byte: {}", bytes );

            HttpUtil.setContentLength( response, length );
            response.headers().set( "content-type", "video/mp4" );
            response.headers().set( "Accept-Ranges", "bytes" );
            response.headers().set( "content-range", bytes );

            logger.info( "producting response" );
            channelHandlerContext.writeAndFlush( response ).addListener( ChannelFutureListener.CLOSE );
        }
        catch ( Exception e ) {
            logger.error( "error", e );
        }
        finally {
            channelHandlerContext.pipeline().remove( this );
        }
    }
}
