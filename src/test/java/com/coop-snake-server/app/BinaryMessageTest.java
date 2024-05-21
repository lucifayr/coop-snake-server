package com.coopsnakeserver.app;

import com.coopsnakeserver.app.MessageType;
import com.coopsnakeserver.app.BinaryMessage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

/**
 * created: 21.05.2024
 *
 * @author June L. Gschwantner
 */
public class BinaryMessageTest {
    @Test
    public void shouldConstructTheCorrectMessage() {
        var inputData = new byte[] { 127, 0, 0, 1, 42, 65, 10, 24 };
        var inputType = MessageType.SnakePosition;

        var output = new BinaryMessage(inputType, inputData).intoBytes();
        assertEquals(BinaryMessage.MESSAGE_VERSION, output[0]);

        var expectedLen = BinaryMessage.MESSAGE_HEADER_WIDTH_VERSION + BinaryMessage.MESSAGE_HEADER_WIDTH_TYPE
                + BinaryMessage.MESSAGE_HEADER_WIDTH_DATA_LENGTH + inputData.length;
        assertEquals(expectedLen, output.length);

        var expectedLastBtye = 24;
        assertEquals(expectedLastBtye, output[output.length - 1]);
    }

    @Test
    public void shouldParseAMessageCorrectly() {
        var input = new byte[] {
                // Version
                BinaryMessage.MESSAGE_VERSION,
                // Tag
                0, 0, 0, 1,
                // Data length
                0, 0, 0, 8,
                // Data
                127, 0, 0, 1, 42, 65, 10, 24 };

        var output = BinaryMessage.fromBytes(input);
        assertEquals(1, output.getType().tag());

        var expectedData = new byte[] { 127, 0, 0, 1, 42, 65, 10, 24 };
        assertArrayEquals(expectedData, output.getData());
    }

    // Basic property test
    @Test
    public void shouldConstructAndParseMessagesCorrectly() {
        var iterations = 100_000;

        for (int i = 0; i < iterations; i++) {

            // fails if no type exists with the binary tag 0 and 1
            var inputType = MessageType.fromTag(i % 2).get();

            var inputData = new byte[ThreadLocalRandom.current().nextInt(0, 255)];
            ThreadLocalRandom.current().nextBytes(inputData);
            var msg = new BinaryMessage(inputType, inputData).intoBytes();

            var output = BinaryMessage.fromBytes(msg);

            assertEquals(inputType, output.getType());
            assertArrayEquals(inputData, output.getData());
        }
    }
}
