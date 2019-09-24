package it.ekursy.blog.netty.introduction.httpserver.server;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import it.ekursy.blog.netty.introduction.httpserver.server.handlers.ChunkedFileReadingHandler;
import it.ekursy.blog.netty.introduction.httpserver.server.handlers.HealthCheckHandler;
import it.ekursy.blog.netty.introduction.httpserver.server.handlers.RangeResponseProducingHandler;
import it.ekursy.blog.netty.introduction.httpserver.server.handlers.ResponseHeaderProducingHandler;

public class HttpServer extends BaseServer {

    private final Path filesLocation;

    /**
     * @param port
     * @param filesLocation
     */
    protected HttpServer(String host, int port, Path filesLocation)
    {
        super( host, port );
        this.filesLocation = filesLocation;
    }

    @Override
    protected List<ChannelHandler> getHandlers()
    {
        var handlers = new ArrayList<ChannelHandler>();

        handlers.add( new HttpServerCodec() );
        handlers.add( new HttpObjectAggregator( 65536 ) );
        handlers.add( new ChunkedWriteHandler() );
        handlers.add( new HealthCheckHandler() );
        handlers.add( new ResponseHeaderProducingHandler( filesLocation ) );
        handlers.add( new RangeResponseProducingHandler() );
        handlers.add( new ChunkedFileReadingHandler() );

        return handlers;
    }

    public static void main(String[] args) throws Exception
    {
        try {
            var host = args[ 0 ];
            var filesLocation = Paths.get( args[ 1 ] );
            System.out.println( filesLocation );
            var httpServer = new HttpServer( host, 8180, filesLocation );

            httpServer.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }
}
