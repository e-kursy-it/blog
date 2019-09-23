package it.ekursy.blog.netty.introduction.activate;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ActivateClient {

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
            var b = new Bootstrap();
            b.group( workerGroup );
            b.channel( NioSocketChannel.class );
            b.option( ChannelOption.SO_KEEPALIVE, true );
            b.handler( new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                }
            } );

            var hostInetAddress = InetAddress.getByName( host );
            var connectFuture = b.connect( hostInetAddress, port ).sync();

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
            var activateClient = new ActivateClient( "localhost", 8180 );

            activateClient.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
