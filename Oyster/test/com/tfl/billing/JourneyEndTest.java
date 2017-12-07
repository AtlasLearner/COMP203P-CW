package com.tfl.billing;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class JourneyEndTest {
    private UUID cardID = UUID.randomUUID();
    private UUID readerID = UUID.randomUUID();
    private ClockInterface time = new SystemTime();

    private JourneyEnd end = new JourneyEnd(cardID, readerID, time);

    @Test
    public void CheckcardID() { assertEquals(end.cardId(), cardID); }

    @Test
    public void CheckreaderID() { assertEquals(end.readerId(), readerID); }

    @Test
    public void Checktime() { assertEquals(end.time(), time.currentTime());}


}
