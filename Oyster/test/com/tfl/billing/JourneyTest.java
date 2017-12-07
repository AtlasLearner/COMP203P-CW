package com.tfl.billing;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class JourneyTest {
    private UUID cardId = UUID.randomUUID();
    private UUID startReaderId = UUID.randomUUID();
    private UUID endReaderId = UUID.randomUUID();
    private ClockInterface time = new SystemTime();

    private JourneyEvent start = new JourneyStart(cardId, startReaderId, time);
    private JourneyEvent end = new JourneyStart(cardId, endReaderId, time);

    private Journey journey = new Journey(start, end);

    @Test
    public void CheckOriginID() {
        assertEquals(journey.originId(), start.readerId());
    }

    @Test
    public void CheckDestinationID() {
        assertEquals(journey.destinationId(), end.readerId());
    }

    @Test
    public void CheckFormattedStartTime() {
        assertEquals(journey.formattedStartTime(), SimpleDateFormat.getInstance().format(new Date(start.time())));
    }

    @Test
    public void CheckFormattedEndTime() {
        assertEquals(journey.formattedEndTime(), SimpleDateFormat.getInstance().format(new Date(end.time())));
    }

    @Test
    public void CheckStartTime() {
        assertEquals(journey.startTime(), new Date(start.time()));
    }

    @Test
    public void CheckEndTime() {
        assertEquals(journey.endTime(), new Date(end.time()));
    }

    @Test
    public void CheckDurationSeconds() {
        assertEquals(journey.durationSeconds(),  (int)((end.time() - start.time()) / 1000));
    }

    @Test
    public void CheckDurationMinutes() {
        assertEquals(journey.durationMinutes(), "" + journey.durationSeconds() / 60 + ":" + journey.durationSeconds() % 60);
    }
}
