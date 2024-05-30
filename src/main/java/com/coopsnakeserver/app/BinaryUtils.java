package com.coopsnakeserver.app;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * BinaryUtils
 *
 * created: 21.05.2024
 *
 * @author June L. Gshwantner
 */
public class BinaryUtils {
    public static byte[] concat(Iterable<byte[]> byteArrays) {
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

    // Duplicated to make the API more comfortable to use.
    //
    // i.e. Allows calling
    // concat(bytes1, bytes2, bytes3);
    // instead of
    // concat(new byte[] {bytes1, bytes2, bytes3});
    public static byte[] concat(byte[]... byteArrays) {
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

    public static byte[] iteratorToBytes(IntoBytes[] iterable) {
        var byteArrays = new ArrayList<byte[]>(iterable.length);
        for (var element : iterable) {
            byteArrays.add(element.intoBytes());
        }

        return concat(byteArrays);
    }

    /**
     * Convert an unsigned int 32 to a 4 byte slice (big endian).
     *
     * @param value Unsigned integer to convert to byte slice.
     */
    public static byte[] int32ToBytes(int value) {
        DevUtils.assertion(value >= 0, "integer must be greater or equal to 0. Recevied value " + value);

        var bytes = ByteBuffer
                .allocate(Integer.BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(value)
                .array();

        return bytes;
    }

    /**
     * Convert an unsigned int 16 to a 2 byte slice (big endian).
     *
     * @param value Unsigned integer to convert to byte slice.
     */
    public static byte[] int16ToBytes(short value) {
        DevUtils.assertion(value >= 0, "short integer must be greater or equal to 0. Recevied value " + value);

        var bytes = ByteBuffer
                .allocate(Short.BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .putShort(value)
                .array();

        return bytes;
    }

    /**
     * Convert an byte slice of length 4 to an integer. Byte slice is read
     * as big endian.
     *
     * @param value Byte slice to convert to an integer. Must be exactly 4 bytes
     *              long.
     */
    public static int bytesToInt32(byte[] value) {
        DevUtils.assertion(value.length == 4,
                "length of byte array should be 4 when converting to an int. Received byte array "
                        + Arrays.toString(value));

        var int32 = ByteBuffer
                .wrap(value)
                .order(ByteOrder.BIG_ENDIAN)
                .put(value)
                .getInt(0);

        return int32;
    }

    /**
     * Convert an byte slice of length 2 to a 16 bit int. Byte slice is read
     * as big endian.
     *
     * @param value Byte slice to convert to an integer. Must be exactly 2 bytes
     *              long.
     */
    public static short bytesToInt16(byte[] value) {
        DevUtils.assertion(value.length == 2,
                "length of byte array should be 2 when converting to an int16. Received byte array "
                        + Arrays.toString(value));

        var int16 = ByteBuffer
                .wrap(value)
                .order(ByteOrder.BIG_ENDIAN)
                .put(value)
                .getShort(0);

        return int16;
    }
}
