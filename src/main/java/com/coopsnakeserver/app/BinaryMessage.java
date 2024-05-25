package com.coopsnakeserver.app;

import java.util.Arrays;

/**
 * BinaryMessage
 *
 * Class to parse the basic structure of a binary message sent over a websocket.
 *
 * Does not handle serializing/deserializing the {@code}data field{@code}.
 *
 * <pre>
 * |------------------|------------------------|-----------------------|------------------------- |
 * | version (1 byte) | message type (4 bytes) | data length (4 bytes) | data (data length bytes) |
 * |------------------|------------------------|-----------------------|------------------------- |
 * </pre>
 *
 * created: 21.05.2024
 *
 * @author June L. Gschwantner
 */
public class BinaryMessage {
    public static final byte MESSAGE_VERSION = 1;
    public static final int MESSAGE_HEADER_WIDTH_VERSION = 1;
    public static final int MESSAGE_HEADER_WIDTH_TYPE = 4;
    public static final int MESSAGE_HEADER_WIDTH_DATA_LENGTH = 4;

    private final byte[] data;
    private final MessageType type;

    public BinaryMessage(MessageType type, byte[] data) {
        this.data = data;
        this.type = type;
    }

    /**
     * Builds a message byte stream that is read to be sent through a websocket.
     *
     * @param data The data that will be contained in the message. The caller
     *             has to ensure that {@code}data{@code} is in a valid format for
     *             the given
     *             {@code}type{@code} so that any downstream parser can correctly
     *             decode
     *             the message.
     * @param type The type of the message being sent. This indicates to any
     *             parser how to decode the bytes contained in the
     *             {@code}data{@code} field. An example for a {@code}type{@code}
     *             could be snake position data.
     * @return
     */
    public byte[] intoBytes() {
        var msgVersion = new byte[] { MESSAGE_VERSION };
        var dataLength = BinaryUtils.int32ToBytes(this.data.length);
        var msgType = this.type.tagBytes();

        return BinaryUtils.concat(msgVersion, msgType, dataLength, this.data);
    }

    /**
     * Parses a message from a binary sequence.
     * Extracts the data and message type from the binary sequence.
     *
     * Ensures version match between the current server and the message being
     * received.
     *
     * @param msg The binary sequence to parse.
     */
    public static BinaryMessage fromBytes(byte[] msg) {
        var version = msg[0];
        assert (version == MESSAGE_VERSION)
                : String.format("Version mismatch. Expected %d. Received %d.", MESSAGE_VERSION, version);

        var typeStartIdx = 1;
        var typeEndIdx = typeStartIdx + MESSAGE_HEADER_WIDTH_TYPE;
        var typeBytes = Arrays.copyOfRange(msg, typeStartIdx, typeEndIdx);
        var typeTag = BinaryUtils.bytesToInt32(typeBytes);
        var type = MessageType.fromTag(typeTag);
        assert (type.isPresent())
                : String.format("Invalid message type received. Received %d. Valid types are %s", typeTag,
                        Arrays.toString(MessageType.values()));

        var lenStartIdx = typeEndIdx;
        var lenEndIdx = lenStartIdx + MESSAGE_HEADER_WIDTH_DATA_LENGTH;
        var lenBytes = Arrays.copyOfRange(msg, lenStartIdx, lenEndIdx);
        var dataLength = BinaryUtils.bytesToInt32(lenBytes);

        var dataStartIdx = lenEndIdx;
        var dataEndIdx = dataStartIdx + dataLength;
        assert (msg.length >= dataEndIdx) : String.format(
                "Message is too short. Expected minium length %d but only got length %d", dataEndIdx, msg.length);

        var data = Arrays.copyOfRange(msg, dataStartIdx, dataEndIdx);

        return new BinaryMessage(type.get(), data);
    }

    public byte[] getData() {
        return data;
    }

    public MessageType getType() {
        return type;
    }

}
