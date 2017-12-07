package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.List;

public class PaymentsSystemAdapter implements PaymentsSystemInterface {

    private static PaymentsSystemAdapter instance = null;

    public static PaymentsSystemAdapter getInstance() {
        if (instance == null) {
            instance = new PaymentsSystemAdapter();
        }
        return instance;
    }

    @Override
    public void charge(Customer customer, List<Journey> journeys, BigDecimal totalBill) {
        PaymentsSystem.getInstance().charge(customer, journeys, totalBill);
    }
}