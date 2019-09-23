package it.ekursy.blog.netty.introduction.httpserver.server;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

abstract class BaseServer {

    private final Logger logger = LogManager.getLogger( BaseServer.class );

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final int port;
    private final String host;

    /**
     * @param host
     * @param port
     */
    protected BaseServer(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    protected ServerBootstrap getBootstrap()
    {
        var bootstrap = new ServerBootstrap();
        bootstrap.group( bossGroup, workerGroup );
        bootstrap.channel( NioServerSocketChannel.class );
        bootstrap.option( ChannelOption.SO_KEEPALIVE, true );
        bootstrap.childHandler( new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel socketChannel) throws Exception
            {
                logger.info( "Channel initialized" );
                var pipeline = socketChannel.pipeline();
                var handlers = getHandlers();

                handlers.forEach( pipeline::addLast );
            }
        } );

        bootstrap.option( ChannelOption.SO_BACKLOG, 128 );

        return bootstrap;
    }

    public final void connect() throws InterruptedException, UnknownHostException
    {
        try {
            var bootstrap = getBootstrap();

            var channelFuture = bootstrap.bind( host, port ).sync();

            channelFuture.addListener( (future) -> {
                if ( future.isSuccess() ) {
                    logger.info( "Server started at {}:{}", host, port );
                }
            } );

            channelFuture.channel().closeFuture().sync();
        }
        finally {
            channelClosed();
        }
    }

    protected void channelClosed()
    {
        workerGroup.shutdownGracefully();
    }

    protected abstract List<ChannelHandler> getHandlers();
}
