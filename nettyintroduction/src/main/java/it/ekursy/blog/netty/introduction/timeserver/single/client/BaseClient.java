package it.ekursy.blog.netty.introduction.timeserver.single.client;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

abstract class BaseClient {


    private final Logger logger = LogManager.getLogger( TimeClient.class );

    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final String host;
    private final int port;

    BaseClient(String host, int port)
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
                    var handlers = getHandlers();
                    var pipeline = socketChannel.pipeline();

                    handlers.forEach( pipeline::addLast );
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

    protected abstract List<ChannelHandler> getHandlers();
}
