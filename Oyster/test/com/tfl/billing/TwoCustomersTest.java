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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TwoCustomersTest {
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
    public void twoCustomersOnePeakShortJourneysTest() {
        customers.add(seanLee);
        customers.add(kennethLay);

        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(2.90).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));
            allowing(customerDatabaseInterface).isRegisteredId(KFL_Card.id()); will(returnValue(true));

            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));

            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(kennethLay)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(18, 30);
        kingsCrossReader.touch(SL_Card);
        kingsCrossReader.touch(KFL_Card);

        clock.setTime(18, 45);
        paddingtonReader.touch(SL_Card);
        paddingtonReader.touch(KFL_Card);

        paymentsManager.chargeAccounts();

    }
}
