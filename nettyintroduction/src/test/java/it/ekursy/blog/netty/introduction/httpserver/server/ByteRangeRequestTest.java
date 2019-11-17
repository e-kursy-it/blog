/**
 * Copyright 2019 Marek Będkowski
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package it.ekursy.blog.netty.introduction.httpserver.server;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.config.plugins.convert.HexConverter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import it.ekursy.blog.netty.introduction.httpserver.server.handlers.ChunkedFileReadingHandler;
import it.ekursy.blog.netty.introduction.httpserver.server.handlers.HealthCheckHandler;
import it.ekursy.blog.netty.introduction.httpserver.server.handlers.RangeResponseProducingHandler;
import it.ekursy.blog.netty.introduction.httpserver.server.handlers.ResponseHeaderProducingHandler;
import org.junit.Test;

public class ByteRangeRequestTest {

    byte[] req1 = HexConverter.parseHexBinary(
            "474554202f4269675f4275636b5f42756e6e795f313038305f3130735f324d422e6d703420485454502f312e310d0a486f73743a206c6f63616c686f73743a383138300d0a4163636570743a20746578742f68746d6c2c6170706c69636174696f6e2f7868746d6c2b786d6c2c6170706c69636174696f6e2f786d6c3b713d302e392c2a2f2a3b713d302e380d0a557067726164652d496e7365637572652d52657175657374733a20310d0a436f6f6b69653a205f67613d4741312e312e3239333634333036332e313433383032353833320d0a557365722d4167656e743a204d6f7a696c6c612f352e3020284d6163696e746f73683b20496e74656c204d6163204f5320582031305f31355f3129204170706c655765624b69742f3630352e312e313520284b48544d4c2c206c696b65204765636b6f292056657273696f6e2f31332e302e33205361666172692f3630352e312e31350d0a4163636570742d4c616e67756167653a20706c2d706c0d0a4163636570742d456e636f64696e673a20677a69702c206465666c6174650d0a436f6e6e656374696f6e3a206b6565702d616c6976650d0a0d0a" );

    byte[] req2 = HexConverter.parseHexBinary(
            "474554202f4269675f4275636b5f42756e6e795f313038305f3130735f324d422e6d703420485454502f312e310d0a486f73743a206c6f63616c686f73743a383138300d0a4163636570742d4c616e67756167653a20706c2d706c0d0a582d506c61796261636b2d53657373696f6e2d49643a2034393535463745382d444130342d343934442d413734392d4441444444324532344235450d0a436f6f6b69653a205f67613d4741312e312e3239333634333036332e313433383032353833320d0a52616e67653a2062797465733d302d310d0a4163636570743a202a2f2a0d0a557365722d4167656e743a204d6f7a696c6c612f352e3020284d6163696e746f73683b20496e74656c204d6163204f5320582031305f31355f3129204170706c655765624b69742f3630352e312e313520284b48544d4c2c206c696b65204765636b6f292056657273696f6e2f31332e302e33205361666172692f3630352e312e31350d0a526566657265723a20687474703a2f2f6c6f63616c686f73743a383138302f4269675f4275636b5f42756e6e795f313038305f3130735f324d422e6d70340d0a4163636570742d456e636f64696e673a206964656e746974790d0a436f6e6e656374696f6e3a204b6565702d416c6976650d0a0d0a" );

    byte[] req3 = HexConverter.parseHexBinary(
            "020000004500021500004000400600007f0000017f000001ce851ff4d0b8c049fec5cc34801818e9000a00000101080a0a1f6a0d0a1f6a09474554202f4269675f4275636b5f42756e6e795f313038305f3130735f324d422e6d703420485454502f312e310d0a486f73743a206c6f63616c686f73743a383138300d0a4163636570742d4c616e67756167653a20706c2d706c0d0a582d506c61796261636b2d53657373696f6e2d49643a2034393535463745382d444130342d343934442d413734392d4441444444324532344235450d0a436f6f6b69653a205f67613d4741312e312e3239333634333036332e313433383032353833320d0a52616e67653a2062797465733d302d313936313937370d0a4163636570743a202a2f2a0d0a557365722d4167656e743a204d6f7a696c6c612f352e3020284d6163696e746f73683b20496e74656c204d6163204f5320582031305f31355f3129204170706c655765624b69742f3630352e312e313520284b48544d4c2c206c696b65204765636b6f292056657273696f6e2f31332e302e33205361666172692f3630352e312e31350d0a526566657265723a20687474703a2f2f6c6f63616c686f73743a383138302f4269675f4275636b5f42756e6e795f313038305f3130735f324d422e6d70340d0a4163636570742d456e636f64696e673a206964656e746974790d0a436f6e6e656374696f6e3a204b6565702d416c6976650d0a0d0a" );

    @Test
    public void testByteRange() throws Exception
    {
        var handlers = getHandlers( Paths.get( "src/test/resources/" ) ).toArray( new ChannelHandler[ 0 ] );
        var server = new EmbeddedChannel( handlers );

        server.writeOneInbound( Unpooled.wrappedBuffer( req1 ) ).sync();
        server.writeOneInbound( Unpooled.wrappedBuffer( req2 ) ).sync();
        server.writeOneInbound( Unpooled.wrappedBuffer( req3 ) ).sync();
    }

    static List<ChannelHandler> getHandlers(Path filesLocation)
    {
        var handlers = new ArrayList<ChannelHandler>();

        handlers.add( new HttpServerCodec() );
        handlers.add( new HttpObjectAggregator( 65536 ) );
        handlers.add( new ChunkedWriteHandler() );
        handlers.add( new HealthCheckHandler() );
        handlers.add( new ResponseHeaderProducingHandler( filesLocation ) );
        handlers.add( new RangeResponseProducingHandler() );
        handlers.add( new ChunkedFileReadingHandler() );

        return handlers;
    }

}
