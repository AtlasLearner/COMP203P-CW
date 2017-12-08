package com.tfl.billing;

import com.oyster.OysterCard;
import com.oyster.OysterCardReader;
import com.tfl.billing.util.AdjustableClock;
import com.tfl.external.Customer;
import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TravelTrackerTest {
    private PaymentsSystemInterface paymentsSystemInterface;
    private CustomerDatabaseInterface customerDatabaseInterface;
    private TravelTracker tracker;

    private List<Customer> customers;
    private AdjustableClock clock;

    private final OysterCard SL_Card = new OysterCard("42694269-8cf0-11bd-b23e-10b96e4ef00d");
    private final Customer seanLee = new Customer("Sean Lee", SL_Card);

    private final OysterCard KFL_Card = new OysterCard("12341234-8cf0-11bd-b23e-10b96e4ef00d");
    private final Customer kennethLay = new Customer("Kenneth Forbes Lay", KFL_Card);

    private final OysterCard RM_Card = new OysterCard("12451451-8cf0-11bd-b23e-10b96e4ef00d");
    private final Customer ryoMochi = new Customer("Ryo Mochizuki", RM_Card);

    private OysterCardReader paddingtonReader;
    private OysterCardReader victoriaReader;
    private OysterCardReader kingsCrossReader;

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Before
    public void beforeAll() {
        customerDatabaseInterface = context.mock(CustomerDatabaseInterface.class);
        paymentsSystemInterface = context.mock(PaymentsSystemInterface.class);
        clock = new AdjustableClock();

        paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
        victoriaReader = OysterReaderLocator.atStation(Station.VICTORIA_STATION);
        kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);

        tracker = new TravelTracker(customerDatabaseInterface, clock);
        customers = new ArrayList<>();
    }

    @After
    public void endOfTest() {
        customerDatabaseInterface = null;
        paymentsSystemInterface = null;
        clock = null;

        paddingtonReader = null;
        victoriaReader = null;
        kingsCrossReader = null;

        tracker = null;
        customers = null;
    }

    @Test
    public void cardScanTest(){
        context.checking(new Expectations(){{
            exactly(1).of(customerDatabaseInterface).isRegisteredId(SL_Card.id());
            will(returnValue(true));
        }});
        tracker.cardScanned(SL_Card.id(), paddingtonReader.id());
    }

    @Test
    public void getCustomerJourneysTest(){
        context.checking(new Expectations(){{
            exactly(1).of(customerDatabaseInterface).isRegisteredId(KFL_Card.id());
            will(returnValue(true));
            exactly(1).of(customerDatabaseInterface).isRegisteredId(SL_Card.id());
            will(returnValue(true));
        }});
        tracker.connect(paddingtonReader, victoriaReader, kingsCrossReader);
        paddingtonReader.touch(KFL_Card);
        victoriaReader.touch(KFL_Card);

        JourneyEvent start = tracker.getEventLog().get(0);
        JourneyEvent end = tracker.getEventLog().get(1);
        Journey journeyTest = new Journey(start, end);

        assertEquals(tracker.getCustomerJourneys(kennethLay).get(0).originId(),journeyTest.originId());
        assertEquals(tracker.getCustomerJourneys(kennethLay).get(0).destinationId(),journeyTest.destinationId());

        paddingtonReader.touch(SL_Card);
        kingsCrossReader.touch(SL_Card);

        Journey journeyTest2 = new Journey(start, end);

        assertEquals(tracker.getCustomerJourneys(seanLee).get(0).originId() , journeyTest2.originId());
        assertEquals(tracker.getCustomerJourneys(seanLee).get(0).startTime(), journeyTest2.startTime());
        assertEquals(tracker.getCustomerJourneys(seanLee).get(0).endTime(), journeyTest2.endTime());
    }

}
