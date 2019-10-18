/**
 * Copyright 2019 Marek Będkowski
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package it.ekursy.blog.netty.introduction.timeserver.split.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.ChannelHandler;

import it.ekursy.blog.netty.introduction.timeserver.split.server.handlers.CurrentTimeEncoder;
import it.ekursy.blog.netty.introduction.timeserver.split.server.handlers.CurrentTimeHandler;

public class TimeServer extends BaseServer {

    private final Logger logger = LogManager.getLogger( TimeServer.class );

    public TimeServer(String host, int port)
    {
        super( host, port );
    }

    @Override
    protected List<ChannelHandler> getHandlers()
    {
        var handlers = new ArrayList<ChannelHandler>();

        handlers.add( new CurrentTimeEncoder() );
        handlers.add( new CurrentTimeHandler() );

        return handlers;
    }

    public static void main(String[] args) throws Exception
    {
        try {
            var timeServer = new TimeServer( "127.0.0.1", 8080 );

            timeServer.connect();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

}
