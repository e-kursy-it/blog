/**
 * Copyright 2019 Marek Będkowski
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
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
