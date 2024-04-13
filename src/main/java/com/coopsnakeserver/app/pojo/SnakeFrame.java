package com.coopsnakeserver.app.pojo;

import java.util.List;

/**
 *
 * created: 13.04.2024
 *
 * @author June L. Gschwantner
 */
public class SnakeFrame {
    // TODO:
    // If we end up sending a large amount of this data it might be necessary
    // to encode this more efficiently, and not simply resend every segment on
    // change.
    //
    // - Send only updates since a snake moves in a predictable way, being in sync
    // with the client is highly important if we do this
    //
    // - Encode the data as a list of points + direction + length, this way every
    // line segment of the snake would only require 1 point (2 bytes) + 2 extra
    // bytes for direction and length
    private List<Coordiante> segments;
    private SessionPlayers owner;
}
