/**
 * Copyright 2019 Marek Będkowski
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package it.ekursy.blog.netty.introduction.httpserver.server.event;

import java.nio.file.Path;

public class FileRangeRequestEvent {

    private final Path path;

    private final Range range;

    /**
     * @param path
     * @param range
     */
    public FileRangeRequestEvent(Path path, Range range)
    {
        this.path = path;
        this.range = range;
    }

    public Path getPath()
    {
        return path;
    }

    public Range getRange()
    {
        return range;
    }

    public static class Range {
        private final long start;
        private final long end;

        /**
         * @param start
         * @param end
         */
        public Range(long start, long end)
        {
            this.start = start;
            this.end = end;
        }

        public long getStart()
        {
            return start;
        }

        public int getEnd()
        {
            return (int) end;
        }

        public int getSize()
        {
            return (int) ( end - start ) + 1;
        }
    }
}
