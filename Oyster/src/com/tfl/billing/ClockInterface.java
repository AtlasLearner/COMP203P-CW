package com.tfl.billing;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public interface ClockInterface {
    long currentTime();
    Instant instant();
    ZoneId getZone();
    Clock withZone(ZoneId zone);
}
