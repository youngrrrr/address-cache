/**
 * 
 */
package com.redacted;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author young-raekim
 *
 */
public class TimedInetAddressTest {

  private static InetAddress address1;
  private static InetAddress address2;

  private static Date pastDate;
  private static Date futureDate;

  private static TimedInetAddress t1a;
  private static TimedInetAddress t1b;
  private static TimedInetAddress t2;
  private static TimedInetAddress t3;
  private static TimedInetAddress t4;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    address1 = InetAddress.getByName("58.24.42.58");
    address2 = InetAddress.getByName("180.191.76.84");

    pastDate = new Date(1453569833129L);
    futureDate = new Date(2147472000000L);

    t1a = new TimedInetAddress(address1, pastDate);
    t1b = new TimedInetAddress(address1, pastDate);
    t2 = new TimedInetAddress(address1, futureDate);
    t3 = new TimedInetAddress(address2, pastDate);
    t4 = new TimedInetAddress(address2, futureDate);
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    address1 = null;
    address2 = null;

    pastDate = null;
    futureDate = null;

    t1a = null;
    t1b = null;
    t2 = null;
    t3 = null;
    t4 = null;
  }

  /**
   * Test method for {@link com.squarespace.TimedInetAddress#getInetAddress()}.
   */
  @Test
  public void testGetInetAddress() {
    assertThat(
        "The TimedInetAddress in last param should return the InetAddress that was used to create it.",
        address1, is(t1a.getInetAddress()));
  }

  /**
   * Test method for
   * {@link com.squarespace.TimedInetAddress#getExpirationDate()}.
   */
  @Test
  public void testGetExpirationDate() {
    assertThat(
        "The TimedInetAddress in last param should return the date that was used to create it.",
        pastDate, is(t1a.getExpirationDate()));
  }

  /**
   * Test method for {@link com.squarespace.TimedInetAddress#isExpired()}.
   */
  @Test
  public void testIsExpired() {
    assertThat("The associated date has expired.", true, is(t1a.isExpired()));
  }

  /**
   * Test method for {@link com.squarespace.TimedInetAddress#isExpired()}.
   */
  @Test
  public void testIsNotExpired() {
    assertThat("The associated date has not yet expired (Jan. 19, 2038).",
        false, is(t4.isExpired()));
  }

  /**
   * Test method for
   * {@link com.squarespace.TimedInetAddress#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObjectEquals() {
    assertThat("Instances created with the same arguments should be equal.",
        true, is(t1a.equals(t1b)));
    assertThat("Instances created with the same arguments should be equal.",
        true, is(t1b.equals(t1a)));
  }

  /**
   * Test method for
   * {@link com.squarespace.TimedInetAddress#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObjectEqualsDifferentDates() throws Exception {
    assertThat(
        "Instances created with different expiration dates should still be equal.",
        true, is(t1a.equals(t2)));
    assertThat(
        "Instances created with different expiration dates should still be equal.",
        true, is(t2.equals(t1a)));
  }

  /**
   * Test method for
   * {@link com.squarespace.TimedInetAddress#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObjectNotEqualsAddresses() throws Exception {
    assertThat("Instances created with different InetAddresses.", false,
        is(t1a.equals(t3)));
    assertThat("Instances created with different InetAddresses.", false,
        is(t3.equals(t1a)));
  }

  /**
   * Test method for
   * {@link com.squarespace.TimedInetAddress#equals(java.lang.Object)}.
   * 
   * @throws Exception
   */
  @Test
  public void testEqualsObjectNotEqualsAddressesDates() throws Exception {
    assertThat(
        "Instances created with different InetAddresses and expiration dates.",
        false, is(t1a.equals(t4)));
    assertThat(
        "Instances created with different InetAddresses and expiration dates.",
        false, is(t4.equals(t1a)));
  }

  @Test
  public void testEquals_Symmetric() {
    TimedInetAddress x = new TimedInetAddress(address1, pastDate);
    TimedInetAddress y = new TimedInetAddress(address1, pastDate);

    assertThat("Same object quality test for equals.", true, is(x.equals(y)));
    assertThat("Same object quality test for equals.", true, is(y.equals(x)));
    assertThat("Same object quality test for hashcode.", x.hashCode(),
        is(y.hashCode()));
  }
}
