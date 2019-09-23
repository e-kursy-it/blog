package it.ekursy.blog.netty.introduction.httpserver.server;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import it.ekursy.blog.netty.introduction.httpserver.server.handlers.ChunkedFileReadingHandler;
import it.ekursy.blog.netty.introduction.httpserver.server.handlers.ResponseHeaderProducingHandler;

public class HttpServer extends BaseServer {

    /**
     * @param host
     * @param port
     */
    protected HttpServer(String host, int port)
    {
        super( host, port );
    }

    @Override
    protected List<ChannelHandler> getHandlers()
    {
        var handlers = new ArrayList<ChannelHandler>();

        handlers.add( new HttpServerCodec() );
        handlers.add( new HttpObjectAggregator( 65536 ) );
        handlers.add( new ChunkedWriteHandler() );
        handlers.add( new ResponseHeaderProducingHandler() );
        handlers.add( new ChunkedFileReadingHandler() );

        return handlers;
    }

    public static void main(String[] args) throws Exception
    {
        try {
            var httpServer = new HttpServer( "127.0.0.1", 8080 );

            httpServer.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }
}
