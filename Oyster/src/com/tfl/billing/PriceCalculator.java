package com.tfl.billing;

import java.math.BigDecimal;

public class PriceCalculator {
    private DurationCheckerInterface durationChecker = new DurationChecker();
    private PeakCheckerInterface peakChecker = new PeakChecker();

    public BigDecimal calculatePrice(Journey journey) {
        BigDecimal journeyPrice = Pricing.OFF_PEAK_SHORT_JOURNEY_PRICE;
        if (peakChecker.isPeak(journey) && durationChecker.isLongJourney(journey)){
            journeyPrice = Pricing.PEAK_LONG_JOURNEY_PRICE;
        }
        if (peakChecker.isPeak(journey) && !durationChecker.isLongJourney(journey)){
            journeyPrice = Pricing.PEAK_SHORT_JOURNEY_PRICE;
        }
        if (!peakChecker.isPeak(journey) && durationChecker.isLongJourney(journey)) {
            journeyPrice = Pricing.OFF_PEAK_LONG_JOURNEY_PRICE;
        }
        return journeyPrice;
    }


}
