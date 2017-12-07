package com.tfl.billing;

import com.tfl.external.Customer;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PaymentsManager {
    private TravelTracker travelTracker;
    private PaymentsSystemInterface paymentsSystemInterface;

    static final BigDecimal OFF_PEAK_LONG_JOURNEY_PRICE = new BigDecimal(2.70);
    static final BigDecimal OFF_PEAK_SHORT_JOURNEY_PRICE = new BigDecimal(1.60);
    static final BigDecimal OFF_PEAK_JOURNEY_CAP = new BigDecimal(7.00);
    static final BigDecimal PEAK_LONG_JOURNEY_PRICE = new BigDecimal(3.80);
    static final BigDecimal PEAK_SHORT_JOURNEY_PRICE = new BigDecimal(2.90);
    static final BigDecimal PEAK_JOURNEY_CAP = new BigDecimal(9.00);

    public PaymentsManager(TravelTracker travelTracker, PaymentsSystemInterface paymentsSystemInterface) {
        this.travelTracker = travelTracker;
        this.paymentsSystemInterface = paymentsSystemInterface;
    }

    public PaymentsManager(TravelTracker travelTracker) {
        this.travelTracker = travelTracker;
        this.paymentsSystemInterface =  PaymentsSystemAdapter.getInstance();
    }

    public void chargeAccounts() {
        List<Customer> customers = travelTracker.getCustomerDatabaseInterface().getCustomers();
        for (Customer customer : customers) {
            chargeCustomer(customer);
        }
    }

    private void chargeCustomer(Customer customer) {
        List<Journey> journeys = travelTracker.getCustomerJourneys(customer);
        paymentsSystemInterface.charge(customer, journeys, roundToNearestPenny(TotalCustomerJourney(customer)));
    }

    private BigDecimal TotalCustomerJourney(Customer customer) {
        List<Journey> journeys = travelTracker.getCustomerJourneys(customer);
        boolean peakTrue = false;
        BigDecimal totalCost = new BigDecimal(0);
        for (Journey journey : journeys) {
            BigDecimal journeyPrice = OFF_PEAK_SHORT_JOURNEY_PRICE;
            if (peak(journey) && longJourney(journey)){
                journeyPrice = PEAK_LONG_JOURNEY_PRICE;
                peakTrue = true;
            }
            if (peak(journey) && !longJourney(journey)){
                journeyPrice = PEAK_SHORT_JOURNEY_PRICE;
                peakTrue = true;
            }
            if (!peak(journey) && longJourney(journey)){ journeyPrice = OFF_PEAK_LONG_JOURNEY_PRICE; }
            totalCost = totalCost.add(journeyPrice);
        }
        if(peakTrue == false && totalCost.compareTo(OFF_PEAK_JOURNEY_CAP) == 1) { totalCost = OFF_PEAK_JOURNEY_CAP; }
        if(peakTrue && totalCost.compareTo(PEAK_JOURNEY_CAP) == 1) { totalCost = PEAK_JOURNEY_CAP; }
        return totalCost;
    }

    private BigDecimal roundToNearestPenny(BigDecimal poundsAndPence) { return poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP); }

    public boolean peak(Journey journey) {
        return peak(journey.startTime()) || peak(journey.endTime());
    }

    public boolean longJourney (Journey journey) { return (journey.durationSeconds() >= 25 * 60); }

    private boolean peak(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 19);
    }

}
