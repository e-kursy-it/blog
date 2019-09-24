package it.ekursy.blog.netty.introduction.httpserver.server.event;

import java.nio.file.Path;

public class FileRangeRequestEvent {

    private final Path path;

    private final int[] range;

    /**
     * @param path
     * @param range
     */
    public FileRangeRequestEvent(Path path, int[] range)
    {
        this.path = path;
        this.range = range;
    }

    public Path getPath()
    {
        return path;
    }

    public int[] getRange()
    {
        return range;
    }
}
