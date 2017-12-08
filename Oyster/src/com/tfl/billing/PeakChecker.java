package com.tfl.billing;

import java.util.Calendar;
import java.util.Date;

public class PeakChecker implements PeakCheckerInterface {
    @Override
    public boolean isPeak(Journey journey) { return isPeakHours(journey.startTime()) || isPeakHours(journey.endTime());}

    private boolean isPeakHours(Date time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 19);
    }
}
