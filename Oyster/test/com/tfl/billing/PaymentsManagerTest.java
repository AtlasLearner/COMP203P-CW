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

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.not;

public class PaymentsManagerTest {

    private PaymentsSystemInterface paymentsSystemInterface;
    private CustomerDatabaseInterface customerDatabaseInterface;
    private PeakCheckerInterface peakCheckerInterface = new PeakChecker();
    private DurationCheckerInterface durationCheckerInterface = new DurationChecker();
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
    public void noJourneyTest(){
        customers.add(seanLee);
        List<Journey> journeys = new ArrayList<>();
        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            ignoring(paymentsSystemInterface).charge(with.is(not(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
            exactly(1).of(paymentsSystemInterface).charge(with(seanLee), with(aNonNull(List.class)), with(cost));
        }});

        paymentsManager.chargeAccounts();
    }

    @Test
    public void offPeakTest(){
        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);

        clock.setTime(12,0);
        JourneyStart start = new JourneyStart(SL_Card.id(), victoriaReader.id(), clock);

        clock.setTime(12, 30);
        JourneyEnd end =  new JourneyEnd(SL_Card.id(), paddingtonReader.id(), clock);

        Journey journey = new Journey(start, end);

        assertFalse(peakCheckerInterface.isPeak(journey));
    }

    @Test
    public void peakTest(){
        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);

        clock.setTime(17, 01);
        JourneyStart start = new JourneyStart(SL_Card.id(), victoriaReader.id(), clock);

        clock.setTime(18, 01);
        JourneyEnd end = new JourneyEnd(SL_Card.id(), paddingtonReader.id(), clock);

        Journey journey = new Journey(start, end);
        assertTrue(peakCheckerInterface.isPeak(journey));
    }

    @Test
    public void longJourneyTest() {
        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);

        clock.setTime(17, 01);
        JourneyStart start = new JourneyStart(SL_Card.id(), victoriaReader.id(), clock);

        clock.setTime(17, 30);
        JourneyEnd end = new JourneyEnd(SL_Card.id(), paddingtonReader.id(), clock);

        Journey journey = new Journey(start, end);
        assertTrue(durationCheckerInterface.isLongJourney(journey));
    }

    @Test
    public void shortJourneyTest() {
        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);

        clock.setTime(17, 01);
        JourneyStart start = new JourneyStart(SL_Card.id(), victoriaReader.id(), clock);

        clock.setTime(17, 20);
        JourneyEnd end = new JourneyEnd(SL_Card.id(), paddingtonReader.id(), clock);

        Journey journey = new Journey(start, end);
        assertFalse(durationCheckerInterface.isLongJourney(journey));
    }

    @Test
    public void oneCustomerOneOffPeakShortJourneyTest() {
        customers.add(seanLee);

        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(1.6).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));
            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(11, 11);
        kingsCrossReader.touch(SL_Card);

        clock.setTime(11, 16);
        paddingtonReader.touch(SL_Card);


        paymentsManager.chargeAccounts();
    }

    @Test
    public void oneCustomerOneOffPeakLongJourneyTest() {
        customers.add(seanLee);

        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(2.7).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));
            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(11, 11);
        kingsCrossReader.touch(SL_Card);

        clock.setTime(12, 12);
        paddingtonReader.touch(SL_Card);


        paymentsManager.chargeAccounts();
    }
    @Test
    public void oneCustomerPeakShortJourneyTest(){
        customers.add(seanLee);
        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(2.90).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));
            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(17, 11);
        kingsCrossReader.touch(SL_Card);

        clock.setTime(17, 16);
        paddingtonReader.touch(SL_Card);


        paymentsManager.chargeAccounts();

    }
    @Test
    public void oneCustomerPeakLongJourneyTest() {
        customers.add(seanLee);

        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(3.80).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));
            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(17, 11);
        kingsCrossReader.touch(SL_Card);

        clock.setTime(18, 11);
        paddingtonReader.touch(SL_Card);


        paymentsManager.chargeAccounts();
    }

    @Test
    public void oneCustomerOnePeakShortAndOneOffPeakLongJourneyTest() {
        customers.add(seanLee);

        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(5.60).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));
            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(18, 30);
        kingsCrossReader.touch(SL_Card);

        clock.setTime(18, 35);
        paddingtonReader.touch(SL_Card);

        clock.setTime(22, 00);
        paddingtonReader.touch(SL_Card);

        clock.setTime(22, 30);
        kingsCrossReader.touch(SL_Card);

        paymentsManager.chargeAccounts();
    }

    @Test
    public void oneCustomerOnePeakLongAndOneOffPeakShortJourneyTest() {
        customers.add(seanLee);

        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(5.40).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));
            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(18, 30);
        kingsCrossReader.touch(SL_Card);

        clock.setTime(19, 00);
        paddingtonReader.touch(SL_Card);

        clock.setTime(22, 00);
        paddingtonReader.touch(SL_Card);

        clock.setTime(22, 10);
        kingsCrossReader.touch(SL_Card);

        paymentsManager.chargeAccounts();
    }

    @Test
    public void peakJourneyCapTest() {
        customers.add(seanLee);

        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(9.0).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));

            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(18, 30);
        kingsCrossReader.touch(SL_Card);
        clock.setTime(19, 00);
        paddingtonReader.touch(SL_Card);

        clock.setTime(22, 00);
        paddingtonReader.touch(SL_Card);
        clock.setTime(22, 10);
        kingsCrossReader.touch(SL_Card);

        clock.setTime(22, 15);
        kingsCrossReader.touch(SL_Card);
        clock.setTime(22, 45);
        paddingtonReader.touch(SL_Card);

        clock.setTime(23, 00);
        paddingtonReader.touch(SL_Card);
        clock.setTime(23, 30);
        kingsCrossReader.touch(SL_Card);

        paymentsManager.chargeAccounts();
    }

    @Test
    public void offPeakJourneyCapTest() {
        customers.add(seanLee);

        PaymentsManager paymentsManager = new PaymentsManager(tracker, paymentsSystemInterface);
        BigDecimal cost = new BigDecimal(7.0).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(customerDatabaseInterface).isRegisteredId(SL_Card.id()); will(returnValue(true));

            exactly(1).of(customerDatabaseInterface).getCustomers(); will(returnValue(customers));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(seanLee)), with(aNonNull(List.class)), with(equal(cost)));
        }});

        tracker.connect(kingsCrossReader, paddingtonReader);

        clock.setTime(20, 15);
        kingsCrossReader.touch(SL_Card);
        clock.setTime(20, 20);
        paddingtonReader.touch(SL_Card);

        clock.setTime(22, 00);
        paddingtonReader.touch(SL_Card);
        clock.setTime(22, 10);
        kingsCrossReader.touch(SL_Card);

        clock.setTime(22, 15);
        kingsCrossReader.touch(SL_Card);
        clock.setTime(22, 45);
        paddingtonReader.touch(SL_Card);

        clock.setTime(23, 00);
        paddingtonReader.touch(SL_Card);
        clock.setTime(23, 30);
        kingsCrossReader.touch(SL_Card);

        paymentsManager.chargeAccounts();
    }
}
