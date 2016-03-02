package com.redacted;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redacted.TimedAddressCache.TimedAddressCacheBuilder;

public class TimedAddressCacheTest {

  private static InetAddress add1;
  private static InetAddress add2;
  private static InetAddress add3;
  private static InetAddress add4;
  private static InetAddress add5;
  private static InetAddress add6;
  private static InetAddress add7;
  private static InetAddress add8;
  private static InetAddress add9;
  private static InetAddress add10;
  private static InetAddress add11;
  private static InetAddress add12;
  private static InetAddress add13;
  private static InetAddress add14;
  private static InetAddress add15;
  private static InetAddress add16;
  private static InetAddress add17;
  private static InetAddress add18;
  private static InetAddress add19;
  private static InetAddress add20;

  private static Date pastDate1;
  private static Date pastDate2;
  private static Date pastDate3;
  private static Date pastDate4;
  private static Date pastDate5;

  private static Date futureDate1;
  private static Date futureDate2;
  private static Date futureDate3;
  private static Date futureDate4;
  private static Date futureDate5;

  private static TimedInetAddress tAddExpired1;
  private static TimedInetAddress tAddExpired2;
  private static TimedInetAddress tAddExpired3;
  private static TimedInetAddress tAddExpired4;
  private static TimedInetAddress tAddExpired5;
  private static TimedInetAddress tAddValid6;
  private static TimedInetAddress tAddValid7;
  private static TimedInetAddress tAddValid8;
  private static TimedInetAddress tAddValid9;
  private static TimedInetAddress tAddValid10;

  private static Collection<InetAddress> addresses = new LinkedList<>();

  private static Collection<TimedInetAddress> timedAddresses = new LinkedList<>();

  private TimedAddressCache cache;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    add1 = InetAddress.getByName("161.74.139.24");
    add2 = InetAddress.getByName("38.103.53.43");
    add3 = InetAddress.getByName("134.239.164.154");
    add4 = InetAddress.getByName("205.189.163.68");
    add5 = InetAddress.getByName("132.179.50.9");
    add6 = InetAddress.getByName("238.145.145.242");
    add7 = InetAddress.getByName("236.85.128.3");
    add8 = InetAddress.getByName("30.241.211.190");
    add9 = InetAddress.getByName("61.95.214.98");
    add10 = InetAddress.getByName("198.13.141.77");
    add11 = InetAddress.getByName("251.50.231.202");
    add12 = InetAddress.getByName("238.140.15.116");
    add13 = InetAddress.getByName("64.64.124.47");
    add14 = InetAddress.getByName("209.15.35.191");
    add15 = InetAddress.getByName("99.162.194.128");
    add16 = InetAddress.getByName("149.150.64.209");
    add17 = InetAddress.getByName("245.24.53.189");
    add18 = InetAddress.getByName("37.193.11.33");
    add19 = InetAddress.getByName("243.242.235.226");
    add20 = InetAddress.getByName("127.249.88.191");

    pastDate1 = new Date(1453569833129L);
    pastDate2 = new Date(1453569833128L);
    pastDate3 = new Date(1453569833127L);
    pastDate4 = new Date(1453569833126L);
    pastDate5 = new Date(1453569833125L);

    futureDate1 = new Date(2147472000000L);
    futureDate2 = new Date(2147472000001L);
    futureDate3 = new Date(2147472000002L);
    futureDate4 = new Date(2147472000003L);
    futureDate5 = new Date(2147472000004L);

    tAddExpired1 = new TimedInetAddress(add11, pastDate1);
    tAddExpired2 = new TimedInetAddress(add12, pastDate2);
    tAddExpired3 = new TimedInetAddress(add13, pastDate3);
    tAddExpired4 = new TimedInetAddress(add14, pastDate4);
    tAddExpired5 = new TimedInetAddress(add15, pastDate5);
    tAddValid6 = new TimedInetAddress(add16, futureDate1);
    tAddValid7 = new TimedInetAddress(add17, futureDate2);
    tAddValid8 = new TimedInetAddress(add18, futureDate3);
    tAddValid9 = new TimedInetAddress(add19, futureDate4);
    tAddValid10 = new TimedInetAddress(add20, futureDate5);

    addresses.add(add1);
    addresses.add(add2);
    addresses.add(add3);
    addresses.add(add4);
    addresses.add(add5);
    addresses.add(add6);
    addresses.add(add7);
    addresses.add(add8);
    addresses.add(add9);
    addresses.add(add10);

    timedAddresses.add(tAddExpired1);
    timedAddresses.add(tAddExpired2);
    timedAddresses.add(tAddExpired3);
    timedAddresses.add(tAddExpired4);
    timedAddresses.add(tAddExpired5);
    timedAddresses.add(tAddValid6);
    timedAddresses.add(tAddValid7);
    timedAddresses.add(tAddValid8);
    timedAddresses.add(tAddValid9);
    timedAddresses.add(tAddValid10);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    add1 = null;
    add2 = null;
    add3 = null;
    add4 = null;
    add5 = null;
    add6 = null;
    add7 = null;
    add8 = null;
    add9 = null;
    add10 = null;
    add11 = null;
    add12 = null;
    add13 = null;
    add14 = null;
    add15 = null;
    add16 = null;
    add17 = null;
    add18 = null;
    add19 = null;
    add20 = null;

    pastDate1 = null;
    pastDate2 = null;
    pastDate3 = null;
    pastDate4 = null;
    pastDate5 = null;

    futureDate1 = null;
    futureDate2 = null;
    futureDate3 = null;
    futureDate4 = null;
    futureDate5 = null;

    tAddExpired1 = null;
    tAddExpired2 = null;
    tAddExpired3 = null;
    tAddExpired4 = null;
    tAddExpired5 = null;
    tAddValid6 = null;
    tAddValid7 = null;
    tAddValid8 = null;
    tAddValid9 = null;
    tAddValid10 = null;

    addresses = null;

    timedAddresses = null;
  }

  @Before
  public void setUp() throws Exception {
    cache = new TimedAddressCacheBuilder().addAddresses(addresses)
        .addTimedAddresses(timedAddresses).build();
  }

  @After
  public void tearDown() throws Exception {
    cache.close();
  }

  @Test
  public void testOfferNew() {
    try {
      InetAddress add = InetAddress.getByName("208.59.35.193");
      boolean result = cache.offer(add);

      assertThat("Should be accepted by cache.", true, is(result));
    } catch (Exception e) {
      fail("Exception thrown in testOfferNew.");
    }
  }

  @Test
  public void testTimedOfferNew() {
    try {
      InetAddress add = InetAddress.getByName("208.59.35.193");
      TimedInetAddress tAdd = new TimedInetAddress(add, futureDate1);
      boolean result = cache.offer(tAdd);

      assertThat("Should be accepted by cache.", true, is(result));
    } catch (Exception e) {
      fail("Exception thrown in testTimedOfferNew.");
    }
  }

  @Test
  public void testOfferExisting() {
    try {
      boolean result = cache.offer(tAddValid10.getInetAddress());

      assertThat("Should NOT be accepted by cache.", false, is(result));
      assertThat("Also should no longer be at the end.", cache.peek(),
          is(not(tAddValid10.getInetAddress())));
    } catch (Exception e) {
      fail("Exception thrown in testOffer.");
    }
  }

  @Test
  public void testTimedOfferExisting() {
    try {
      boolean result = cache.offer(tAddValid10);

      assertThat("Should NOT be accepted by cache.", false, is(result));
      assertThat("Also should no longer be at the end.", cache.peek(),
          is(not(tAddValid10.getInetAddress())));
    } catch (Exception e) {
      fail("Exception thrown in testTimedOffer.");
    }
  }

  @Test
  public void testContainsTrue() {
    assertThat("Already exists.", true, is(cache.contains(add1)));
  }

  @Test
  public void testContainsFalse() {
    try {
      InetAddress add = InetAddress.getByName("208.59.35.193");
      assertThat("Already exists.", false, is(cache.contains(add)));
    } catch (Exception e) {
      fail("Exception thrown in testContainsFalse.");
    }
  }

  @Test
  public void testRemoveExistingAddress() {
    assertThat("Existing element removed.", true, is(cache.remove(add1)));
  }

  @Test
  public void testRemoveNonexistentAddress() {
    try {
      InetAddress add = InetAddress.getByName("208.59.35.193");
      assertThat("No address to remove.", true, is(not(cache.remove(add))));
    } catch (Exception e) {
      fail("Exception thrown in testRemoveAddressFalse.");
    }
  }

  @Test
  public void testPeek() {
    assertThat(
        "The most recently added InetAddress is correctly at the end of the cache.",
        cache.peek(), is(add20));
  }

  @Test
  public void testRemove() {
    assertThat(
        "The most recently added InetAddress has correctly been removed from the cache.",
        cache.remove(), is(add20));
  }

  @Test
  public void testTake() {
    try {
      assertThat(
          "The most recently added InetAddress has correctly been removed from the cache.",
          cache.take(), is(add20));
    } catch (InterruptedException e) {
      fail("Exception thrown in testTake.");
    }
  }

  @Test
  public void testSize() {
    try {
      Thread.sleep(1000); // must wait for default initial delay to pass
      assertThat("Cache size should be 15 (due to 5 expired addresses).", 15,
          is(cache.size()));
    } catch (Exception e) {
      fail("Exception thrown in testSize.");
    }

  }

  @Test
  public void testCleanupTask() {
    try {
      TimedAddressCache tCache = new TimedAddressCache.TimedAddressCacheBuilder()
          .addAddresses(addresses).addTimedAddresses(timedAddresses)
          .cleanupInitialDelay(2500, TimeUnit.MILLISECONDS).build();
      assertThat(
          "Cache size should be 20 (5 expired addresses not yet evicted due to initial delay).",
          20, is(tCache.size()));
      Thread.sleep(5000); // twice length of cleanup initial delay
      assertThat(
          "Cache size should now be 15 (5 expired addresses should now have been evacuated).",
          15, is(tCache.size()));
    } catch (Exception e) {
      fail("Exception thrown in testCleanupTask.");
    }
  }

  @Test
  public void testIsEmpty() {
    assertThat("Initial cache size is not empty.", true,
        is(not(cache.isEmpty())));
  }

  @Test
  public void testMaximumCapacity() {
    TimedAddressCache tCache = new TimedAddressCache.TimedAddressCacheBuilder()
        .maximumCapacity(1).build();
    assertThat("Initially empty.", true, is(tCache.isEmpty()));
    assertThat("Add one element.", true, is(tCache.offer(add1)));
    assertThat("Cannot add second element (cache is at maximum capacity).",
        true, is(not(tCache.offer(add2))));
  }
}
