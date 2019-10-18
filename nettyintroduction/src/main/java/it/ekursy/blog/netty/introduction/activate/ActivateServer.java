/**
 * Copyright 2019 Marek Będkowski
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package it.ekursy.blog.netty.introduction.activate;

import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ActivateServer {

    private final Logger logger = LogManager.getLogger( ActivateServer.class );

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
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

            bootstrap
                .option( ChannelOption.SO_BACKLOG, 128 );

            var channelFuture = bootstrap.bind( host, port ).sync();

            channelFuture.addListener( (future) -> {
                if ( future.isSuccess() ) {
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
            var activateServer = new ActivateServer( "127.0.0.1", 8080 );

            activateServer.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
