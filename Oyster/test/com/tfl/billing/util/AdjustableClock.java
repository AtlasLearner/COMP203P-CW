package com.tfl.billing.util;

import com.tfl.billing.ClockInterface;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class AdjustableClock implements ClockInterface {
    long time;
    public long currentTime(){
        return time;
    }
    public void setTime(int hour, int minutes){
        time = (hour * 60 + minutes) * 60 * 1000;
    }


    public Instant instant() { return null; }
    public ZoneId getZone() { return null; }
    public Clock withZone(ZoneId zone) { return null; }
}
