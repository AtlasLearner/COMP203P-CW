package com.tfl.billing;

import com.oyster.OysterCardReader;
import com.oyster.ScanListener;
import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;

import java.util.*;

public class TravelTracker implements ScanListener {

    private CustomerDatabaseInterface customerDatabaseInterface;
    private ClockInterface clock;

    private final List<JourneyEvent> eventLog = new ArrayList<JourneyEvent>();
    private final Set<UUID> currentlyTravelling = new HashSet<UUID>();

    public TravelTracker() {
        this.customerDatabaseInterface = CustomerDatabaseAdapter.getInstance();
        this.clock = new SystemTime();
    }

    public TravelTracker(CustomerDatabaseInterface customerDatabaseInterface, ClockInterface clock) {
        this.customerDatabaseInterface = customerDatabaseInterface;
        this.clock = clock;
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
            eventLog.add(new JourneyEnd(cardId, readerId, clock));
            currentlyTravelling.remove(cardId);
        } else {
            if (CustomerDatabase.getInstance().isRegisteredId(cardId)) {
                currentlyTravelling.add(cardId);
                eventLog.add(new JourneyStart(cardId, readerId, clock));
            } else {
                throw new UnknownOysterCardException(cardId);
            }
        }
    }

}
