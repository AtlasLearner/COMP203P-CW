package com.tfl.billing;

import java.util.UUID;

public class JourneyStart extends JourneyEvent {
    public JourneyStart(UUID cardId, UUID readerId, ClockInterface time) {
        super(cardId, readerId, time);
    }
}
