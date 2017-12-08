package com.tfl.billing;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class SystemTime implements ClockInterface {
    long time;
    @Override
    public long currentTime() { return System.currentTimeMillis(); }

    public Instant instant() { return null; }
    public ZoneId getZone() { return null; }
    public Clock withZone(ZoneId zone) { return null; }
}
