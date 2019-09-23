package it.ekursy.blog.netty.introduction.activate;

import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ActivateClient {

    private final Logger logger = LogManager.getLogger( ActivateClient.class );

    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final String host;
    private final int port;

    public ActivateClient(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public void connect() throws InterruptedException, UnknownHostException
    {
        try {
            var bootstrap = new Bootstrap();
            bootstrap.group( workerGroup );
            bootstrap.channel( NioSocketChannel.class );
            bootstrap.option( ChannelOption.SO_KEEPALIVE, true );
            bootstrap.handler( new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception
                {
                    logger.info( "Chanel initialized" );
                }
            } );

            var connectFuture = bootstrap.connect( host, port ).sync();

            var closeFuture = connectFuture.channel().closeFuture();

            closeFuture.sync();
        }
        finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception
    {
        try {
            var activateClient = new ActivateClient( "127.0.0.1", 8080 );

            activateClient.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
