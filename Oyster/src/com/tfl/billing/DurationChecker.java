package com.tfl.billing;

public class DurationChecker implements DurationCheckerInterface {
    @Override
    public boolean isLongJourney (Journey journey) { return (journey.durationSeconds() >= 25 * 60); }
}
