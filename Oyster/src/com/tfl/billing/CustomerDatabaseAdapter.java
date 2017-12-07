package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;

import java.util.List;
import java.util.UUID;

public class CustomerDatabaseAdapter implements CustomerDatabaseInterface {

    private static CustomerDatabaseAdapter instance = null;

    private CustomerDatabase database = CustomerDatabase.getInstance();

    public static CustomerDatabaseAdapter getInstance() {
        if(instance == null) {
            instance = new CustomerDatabaseAdapter();
        }
        return instance;
    }

    @Override
    public List<Customer> getCustomers() {
        return database.getCustomers();
    }

    @Override
    public boolean isRegisteredId(UUID cardId) {
        return database.isRegisteredId(cardId);
    }

}
