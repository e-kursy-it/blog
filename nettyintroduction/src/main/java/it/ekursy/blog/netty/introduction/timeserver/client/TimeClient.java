package it.ekursy.blog.netty.introduction.timeserver.client;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeClient extends BaseClient {

    private final Logger logger = LogManager.getLogger( TimeClient.class );

    /**
     *
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
        return List.of( new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception
            {
                super.channelActive( ctx );
                logger.info( "Channel active!" );
            }
        } );
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
