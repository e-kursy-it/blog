package it.ekursy.blog.netty.introduction.httpserver.server.event;

import java.nio.file.Path;

public class FileAvailableEvent {

    private final Path path;

    public FileAvailableEvent(Path path)
    {
        this.path = path;
    }

    public Path getPath()
    {
        return path;
    }
}
