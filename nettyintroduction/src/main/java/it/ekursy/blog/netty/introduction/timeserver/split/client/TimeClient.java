package it.ekursy.blog.netty.introduction.timeserver.split.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelHandler;

import it.ekursy.blog.netty.introduction.timeserver.split.client.handlers.CurrentTimeDecoder;
import it.ekursy.blog.netty.introduction.timeserver.split.client.handlers.CurrentTimeHandler;

public class TimeClient extends BaseClient {

    private final Logger logger = LogManager.getLogger( TimeClient.class );

    /**
     * @param host
     * @param port
     */
    public TimeClient(String host, int port)
    {
        super( host, port );
    }

    @Override
    protected List<ChannelHandler> getHandlers()
    {
        var handlers = new ArrayList<ChannelHandler>();

        handlers.add( new CurrentTimeDecoder() );
        handlers.add( new CurrentTimeHandler() );

        return handlers;
    }

    public static void main(String[] args) throws Exception
    {
        try {
            var timeClient = new TimeClient( "127.0.0.1", 8080 );

            timeClient.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
