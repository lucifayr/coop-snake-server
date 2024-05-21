package com.coopsnakeserver.app;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * BinaryUtils
 *
 * created: 21.05.2024
 *
 * @author June L. Gshwantner
 */
public class BinaryUtils {
    public static byte[] concatBytes(byte[]... byteArrays) {
        var countAllBytes = 0;
        for (var bytes : byteArrays) {
            countAllBytes += bytes.length;
        }

        var buffer = ByteBuffer.allocate(countAllBytes).order(ByteOrder.BIG_ENDIAN);
        for (var bytes : byteArrays) {
            buffer.put(bytes);
        }

        return buffer.array();
    }

    public static byte[] int32ToBytes(int value) {
        assert (value >= 0) : "integer must be greater 0. Recevied value " + value;

        var bytes = ByteBuffer
                .allocate(Integer.BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(value)
                .array();

        return bytes;
    }

    public static int bytesToInt32(byte[] value) {
        assert (value.length == 4)
                : "length of byte array should always be 4. Received byte array " + Arrays.toString(value);

        var int32 = ByteBuffer
                .wrap(value)
                .order(ByteOrder.BIG_ENDIAN)
                .put(value)
                .getInt(0);

        return int32;
    }
}
