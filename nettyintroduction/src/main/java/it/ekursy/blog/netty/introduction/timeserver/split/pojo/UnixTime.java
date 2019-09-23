package it.ekursy.blog.netty.introduction.timeserver.split.pojo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class UnixTime {

    private final LocalDateTime currentTime;

    public UnixTime()
    {
        currentTime = LocalDateTime.now();
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

}
