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
