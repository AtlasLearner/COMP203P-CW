package com.tfl.billing;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class JourneyStartTest {

    private UUID cardID = UUID.randomUUID();
    private UUID readerID = UUID.randomUUID();
    private ClockInterface time = new SystemTime();

    private JourneyStart start = new JourneyStart(cardID, readerID, time);

    @Test
    public void CheckcardID() { assertEquals(start.cardId(), cardID); }

    @Test
    public void CheckreaderID() { assertEquals(start.readerId(), readerID); }

    @Test
    public void Checktime() { assertEquals(start.time(), time.currentTime()); }
}