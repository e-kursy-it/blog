package it.ekursy.blog.netty.introduction.timeserver.split.client.handlers;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import it.ekursy.blog.netty.introduction.timeserver.split.pojo.UnixTime;

public class CurrentTimeDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception
    {
        if ( byteBuf.readableBytes() >= 4 ) {
            var time = byteBuf.readUnsignedInt();

            var currentTime = UnixTime.fromTime( time );

            out.add( currentTime );
        }
    }
}
