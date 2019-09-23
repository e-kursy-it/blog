package it.ekursy.blog.netty.introduction.activate;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ActivateClientWithTimeout {

    private final Logger logger = LogManager.getLogger( ActivateClientWithTimeout.class );

    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final String host;
    private final int port;

    public ActivateClientWithTimeout(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public void connect(Duration timeout) throws InterruptedException, UnknownHostException
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
                    var pipeline = socketChannel.pipeline();
                    pipeline.addLast( new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception
                        {
                            super.channelActive( channelHandlerContext );
                            logger.info( "client connected" );
                            var timer = new Timer();

                            timer.schedule( new TimerTask() {
                                @Override
                                public void run()
                                {
                                    logger.info( "Closing client" );
                                    channelHandlerContext.channel().close();
                                }
                            }, timeout.toMillis() );

                        }
                    } );
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
            var activateClient = new ActivateClientWithTimeout( "127.0.0.1", 8080 );

            activateClient.connect( Duration.ofSeconds( 1 ) );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
