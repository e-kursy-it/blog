package it.ekursy.blog.netty.introduction.timeserver.single.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import it.ekursy.blog.netty.introduction.timeserver.single.client.handlers.TimeClientHandler;

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
        handlers.add( new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception
            {
                logger.info( "Channel active!" );
                super.channelActive( ctx );
            }
        } );

        handlers.add( new TimeClientHandler() );

        return handlers;
    }

    public static void main(String[] args) throws Exception
    {
        try {
            var activateClient = new TimeClient( "127.0.0.1", 8080 );

            activateClient.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
