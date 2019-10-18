/**
 * Copyright 2019 Marek Będkowski
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package it.ekursy.blog.netty.introduction.timeserver.split.pojo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class UnixTime {

    private final LocalDateTime currentTime;

    public UnixTime()
    {
        currentTime = LocalDateTime.now();
    }

    public UnixTime(LocalDateTime currentTime)
    {
        this.currentTime = currentTime;
    }

    public LocalDateTime getCurrentTime()
    {
        return currentTime;
    }

    public int getTime()
    {
        var now = currentTime.toInstant( ZoneOffset.UTC ).toEpochMilli();
        return (int) ( ( now / 1000 ) + 2208988800L );
    }

    public static UnixTime fromTime(long time)
    {
        var now = ( time - 2208988800L ) * 1000;
        var currentTime = LocalDateTime.ofInstant( Instant.ofEpochMilli( now ), ZoneOffset.UTC );
        return new UnixTime( currentTime );
    }

}
