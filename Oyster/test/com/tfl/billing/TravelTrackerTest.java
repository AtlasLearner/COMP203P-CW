package com.tfl.billing;

import com.oyster.OysterCard;
import com.oyster.OysterCardReader;
import com.tfl.billing.util.AdjustableClock;
import com.tfl.external.Customer;
import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TravelTrackerTest {
    private PaymentsSystemInterface paymentsSystemInterface;
    private CustomerDatabaseInterface customerDatabaseInterface;
    private TravelTracker tracker;

    private final OysterCardReader paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
    private final OysterCardReader victoriaReader = OysterReaderLocator.atStation(Station.VICTORIA_STATION);
    private final OysterCardReader kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);

    final OysterCard myCard1 = new OysterCard("42694269-8cf0-11bd-b23e-10b96e4ef00d");
    final OysterCard myCard2 = new OysterCard("12341234-8cf0-11bd-b23e-10b96e4ef00d");
    private Customer sean = new Customer("Sean Lee", myCard1);
    private Customer kenneth = new Customer("Kenneth Forbes Lay", myCard2);

    private List<Customer> customers = new ArrayList<Customer>(){
        {
            this.add(sean);
            this.add(kenneth);
        }
    };

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    AdjustableClock clock = new AdjustableClock();

    @Before
    public void beforeAll() {
        customerDatabaseInterface = context.mock(CustomerDatabaseInterface.class);
        paymentsSystemInterface = context.mock(PaymentsSystemInterface.class);
        tracker = new TravelTracker(customerDatabaseInterface, clock);
    }

    @Test
    public void cardScanTest(){
        context.checking(new Expectations(){{
            exactly(1).of(customerDatabaseInterface).isRegisteredId(myCard1.id());
            will(returnValue(true));
        }});

        tracker.cardScanned(myCard1.id(), paddingtonReader.id());
    }
}
