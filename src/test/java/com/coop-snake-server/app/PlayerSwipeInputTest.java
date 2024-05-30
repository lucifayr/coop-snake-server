package com.coopsnakeserver.app;

import com.coopsnakeserver.app.pojo.SwipeInputKind;
import com.coopsnakeserver.app.PlayerSwipeInput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

/**
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerSwipeInputTest {
    @Test
    public void shouldParseValidDateCorrectly() {
        var case1 = new byte[] {
                // Swipe Kind
                0,
                // Frame Timestamp
                0, 0, 1, 4
        };

        var output1 = PlayerSwipeInput.fromBytes(case1);
        assertEquals(output1.getKind(), SwipeInputKind.Up);
        assertEquals(output1.getFrameTimestamp(), 260);

        var case2 = new byte[] {
                // Swipe Kind
                3,
                // Frame Timestamp
                0, 0, 0, 0
        };

        var output2 = PlayerSwipeInput.fromBytes(case2);
        assertEquals(output2.getKind(), SwipeInputKind.Left);
        assertEquals(output2.getFrameTimestamp(), 0);
    }

    @Test
    public void shouldThrowOnInvalidData() {
        var case1 = new byte[] {
                // Swipe Kind (invalid)
                5,
                // Frame Timestamp
                0, 0, 1, 4
        };

        assertThrows(RuntimeException.class, () -> PlayerSwipeInput.fromBytes(case1));

        var case2 = new byte[] {
                // Swipe Kind
                0,
                // Frame Timestamp (missing bytes)
                0, 0, 1,
        };

        assertThrows(RuntimeException.class, () -> PlayerSwipeInput.fromBytes(case2));
    }
}
