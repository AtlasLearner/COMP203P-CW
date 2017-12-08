package com.tfl.billing;

import com.oyster.OysterCardReader;
import com.oyster.ScanListener;
import com.tfl.external.Customer;

import java.util.*;

public class TravelTracker implements ScanListener {

    private CustomerDatabaseInterface customerDatabaseInterface;
    private ClockInterface time;

    private final List<JourneyEvent> eventLog = new ArrayList<JourneyEvent>();
    private final Set<UUID> currentlyTravelling = new HashSet<UUID>();

    public TravelTracker() {
        this.customerDatabaseInterface = CustomerDatabaseAdapter.getInstance();
        this.time = new SystemTime();
    }

    public TravelTracker(CustomerDatabaseInterface customerDatabaseInterface, ClockInterface time) {
        this.customerDatabaseInterface = customerDatabaseInterface;
        this.time = time;
    }

    public List<JourneyEvent> getEventLog() {
        return eventLog;
    }

    public CustomerDatabaseInterface getCustomerDatabaseInterface() {
        return customerDatabaseInterface;
    }

    public List<Journey> getCustomerJourneys(Customer customer) {
        List<JourneyEvent> customerJourneyEvents = new ArrayList<JourneyEvent>();
        for (JourneyEvent journeyEvent : eventLog) {
            if (journeyEvent.cardId().equals(customer.cardId())) {
                customerJourneyEvents.add(journeyEvent);
            }
        }

        List<Journey> journeys = new ArrayList<Journey>();

        JourneyEvent start = null;
        for (JourneyEvent event : customerJourneyEvents) {
            if (event instanceof JourneyStart) {
                start = event;
            }
            if (event instanceof JourneyEnd && start != null) {
                journeys.add(new Journey(start, event));
                start = null;
            }
        }
        return journeys;
    }

    public void connect(OysterCardReader... cardReaders) {
        for (OysterCardReader cardReader : cardReaders) {
            cardReader.register(this);
        }
    }

    @Override
    public void cardScanned(UUID cardId, UUID readerId) {
        if (currentlyTravelling.contains(cardId)) {
            eventLog.add(new JourneyEnd(cardId, readerId, time));
            currentlyTravelling.remove(cardId);
        } else {
            if (customerDatabaseInterface.isRegisteredId(cardId)) {
                currentlyTravelling.add(cardId);
                eventLog.add(new JourneyStart(cardId, readerId, time));
            } else {
                throw new UnknownOysterCardException(cardId);
            }
        }
    }

}
