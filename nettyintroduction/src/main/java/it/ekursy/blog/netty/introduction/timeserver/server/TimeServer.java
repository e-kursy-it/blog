package it.ekursy.blog.netty.introduction.timeserver.server;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeServer extends BaseServer {

    private final Logger logger = LogManager.getLogger( TimeServer.class );

    public TimeServer(String host, int port)
    {
        super( host, port );
    }

    @Override
    protected List<ChannelHandler> getHandlers()
    {
        return List.of( new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception
            {
                logger.info( "Client connected" );
            }

            @Override
            public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception
            {
                logger.info( "Client disconnected" );
            }
        } );
    }

    public static void main(String[] args) throws Exception
    {
        try {
            var activateServer = new TimeServer( "127.0.0.1", 8080 );

            activateServer.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
