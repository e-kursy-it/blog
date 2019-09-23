package it.ekursy.blog.netty.introduction.activate;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

public class ActivateServer {

    private final Logger logger = LogManager.getLogger( ActivateServer.class );
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final String host;
    private final int port;

    public ActivateServer(String host, int port)
    {
        this.port = port;
        this.host = host;
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
                    var pipeline = socketChannel.pipeline();
                    pipeline.addLast( new ChannelInboundHandlerAdapter() {
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
            } );

            var channelFuture = bootstrap.bind( host, port ).sync();

            channelFuture.addListener( (future) -> {
                if (future.isSuccess()) {
                    logger.info( "Server started at {}:{}", host, port );
                }
            } );

            channelFuture.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception
    {
        try {
            var activateServer = new ActivateServer( "localhost", 8180 );

            activateServer.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
