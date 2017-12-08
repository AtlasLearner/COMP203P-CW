package com.tfl.billing;

import com.tfl.external.Customer;

import java.math.BigDecimal;
import java.util.List;

public class PaymentsManager {
    private TravelTracker travelTracker;
    private PaymentsSystemInterface paymentsSystemInterface;
    private PeakCheckerInterface peakChecker = new PeakChecker();
    private PriceCalculator priceCalculator = new PriceCalculator();

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
            if (peakChecker.isPeak(journey)) { peakTrue = true; }
            totalCost = totalCost.add(priceCalculator.calculatePrice(journey));
        }
        if(peakTrue == false && totalCost.compareTo(Pricing.OFF_PEAK_JOURNEY_CAP) == 1) { totalCost = Pricing.OFF_PEAK_JOURNEY_CAP; }
        if(peakTrue && totalCost.compareTo(Pricing.PEAK_JOURNEY_CAP) == 1) { totalCost = Pricing.PEAK_JOURNEY_CAP; }
        return totalCost;
    }

    private BigDecimal roundToNearestPenny(BigDecimal poundsAndPence) { return poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP); }

}
