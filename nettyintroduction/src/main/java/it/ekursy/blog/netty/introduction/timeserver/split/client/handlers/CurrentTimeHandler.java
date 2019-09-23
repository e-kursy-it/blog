package it.ekursy.blog.netty.introduction.timeserver.split.client.handlers;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import it.ekursy.blog.netty.introduction.timeserver.split.pojo.UnixTime;

public class CurrentTimeHandler extends MessageToMessageDecoder<UnixTime> {

    private final Logger logger = LogManager.getLogger();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, UnixTime unixTime, List<Object> list) throws Exception
    {
        logger.info( unixTime.getCurrentTime() );
        channelHandlerContext.channel().close();
    }
}
