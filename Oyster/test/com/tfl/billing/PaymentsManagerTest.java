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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.not;

public class PaymentsManagerTest {

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
    public void customerNoJourney(){
        List<Journey> journeys = new ArrayList<>();
        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        context.checking(new Expectations(){{
            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            ignoring(paymentsSystemInterface).charge(with.is(not(sean)), with(aNonNull(List.class)), with(equal(cost)));
            exactly(1).of(paymentsSystemInterface).charge(with(sean), with(aNonNull(List.class)), with(cost));
        }});
        paymentsManager.chargeAccounts();
    }

    @Test
    public void customerOffPeakJourney(){
        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        clock.setTime(12,0);
        JourneyStart start = new JourneyStart(myCard1.id(), victoriaReader.id(), clock);
        clock.setTime(12, 30);
        JourneyEnd end =  new JourneyEnd(myCard1.id(), paddingtonReader.id(), clock);
        Journey journey = new Journey(start, end);
        assertFalse(paymentsManager.peak(journey));
    }

    /*@Test
    public void customerPeakJourney(){

    }

    @Test
    public void customerLongJourney() {

    }

    @Test
    public void customerShortJourney() {

    }*/

}
