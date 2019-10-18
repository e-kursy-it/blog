/**
 * Copyright 2019 Marek Będkowski
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package it.ekursy.blog.netty.introduction.timeserver.split.server.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import it.ekursy.blog.netty.introduction.timeserver.split.pojo.UnixTime;

public class CurrentTimeEncoder extends MessageToByteEncoder<UnixTime> {

    private final Logger logger = LogManager.getLogger();

    @Override
    protected void encode(ChannelHandlerContext ctx, UnixTime unixTime, ByteBuf byteBuf) throws Exception
    {
        var now = unixTime.getTime();
        byteBuf.writeInt( now );

        logger.info( "writing time" );
    }
}
