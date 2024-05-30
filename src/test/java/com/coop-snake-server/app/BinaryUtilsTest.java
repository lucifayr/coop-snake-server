package com.coopsnakeserver.app;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.pojo.Coordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

/**
 * created: 21.05.2024
 *
 * @author June L. Gschwantner
 */
public class BinaryUtilsTest {
    @Test
    public void shouldConvertBytesToInt32Correctly() {
        var case1 = new byte[] { 0, 0, 0, 42 };
        assertEquals(42, BinaryUtils.bytesToInt32(case1));

        // decimal: 12397123, binary: 00000000101111010010101001000011
        var case2 = new byte[] { (byte) 0b00000000, (byte) 0b10111101, (byte) 0b00101010, (byte) 0b01000011 };
        assertEquals(12397123, BinaryUtils.bytesToInt32(case2));

        var case3 = new byte[] { 1, 2, 3, 4, 5 };
        assertThrows(RuntimeException.class, () -> BinaryUtils.bytesToInt32(case3));
    }

    @Test
    public void shouldConvertInt32ToBytesCorrectly() {
        var case1 = 257;
        assertArrayEquals(new byte[] { 0, 0, 1, 1 }, BinaryUtils.int32ToBytes(case1));

        var case2 = (1 << 16) - 3; // 2^16 -3
        assertArrayEquals(new byte[] { 0, 0, (byte) 255, (byte) 253 },
                BinaryUtils.int32ToBytes(case2));
    }

    // Basic property test
    @Test
    public void shouldConvertBothWaysCorrectly() {
        var iterations = 100_000;

        for (int i = 0; i < iterations; i++) {
            var input = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
            var output = BinaryUtils.int32ToBytes(input);
            assertEquals(input, BinaryUtils.bytesToInt32(output));
        }
    }

    @Test
    public void shouldConvertIteratorToBytesCoorrectly() {
        var cord1 = new Coordinate((short) 10, (short) 11);
        var cord2 = new Coordinate((short) 120, (short) 80);
        var cord3 = new Coordinate((short) 23, (short) 84);
        var cord1Bytes = cord1.intoBytes();
        var cord2Bytes = cord2.intoBytes();
        var cord3Bytes = cord3.intoBytes();

        var input = new Coordinate[] { cord1, cord2, cord3 };

        assertArrayEquals(new byte[] {
                0, 10, 0, 11
        }, cord1Bytes);
        assertArrayEquals(BinaryUtils.concat(cord1Bytes, cord2Bytes, cord3Bytes), BinaryUtils.iteratorToBytes(input));
    }
}
