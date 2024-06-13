package com.coopsnakeserver.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import com.coopsnakeserver.app.game.GameUtils;
import com.coopsnakeserver.app.pojo.Coordinate;

/**
 * created: 13.06.2024
 *
 * @author June L. Gschwantner
 */
public class GameUtilsTest {
    @Test
    public void shouldCreateValidCorrindates() {
        var out1p1 = GameUtils.initialCoords((short) 2, (short) 4, (byte) 1);
        var out1p2 = GameUtils.initialCoords((short) 2, (short) 4, (byte) 2);

        assertArrayEquals(
                new Coordinate[] { new Coordinate((short) 3, (short) 3), new Coordinate((short) 2, (short) 3) },
                out1p1.toArray());
        assertArrayEquals(
                new Coordinate[] { new Coordinate((short) 1, (short) 1), new Coordinate((short) 2, (short) 1) },
                out1p2.toArray());

        var out2p1 = GameUtils.initialCoords((short) 2, (short) 5, (byte) 1);
        var out2p2 = GameUtils.initialCoords((short) 2, (short) 5, (byte) 2);
        var out2p3 = GameUtils.initialCoords((short) 2, (short) 5, (byte) 3);

        assertArrayEquals(
                new Coordinate[] { new Coordinate((short) 3, (short) 3), new Coordinate((short) 2, (short) 3) },
                out2p1.toArray());
        assertArrayEquals(
                new Coordinate[] { new Coordinate((short) 1, (short) 1), new Coordinate((short) 2, (short) 1) },
                out2p2.toArray());
        assertArrayEquals(
                new Coordinate[] { new Coordinate((short) 3, (short) 4), new Coordinate((short) 2, (short) 4) },
                out2p3.toArray());
    }
}
