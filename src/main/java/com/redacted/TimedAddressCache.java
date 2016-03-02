package com.redacted;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the {@link AddressCache} interface. The cache has a
 * "Last-In-First-Out" (LIFO) retrieval policy and a "First-In-First-Out" (FIFO)
 * eviction policy. Methods such as peek(), remove() and take() retrieve the
 * most recently added element and an internal cleanup task that in periodic
 * intervals removes the oldest elements from the cache.
 * 
 * Note that this cache runs two background tasks, each on their own separate
 * threads: (1) A cleanup task that evicts expired addresses periodically and
 * (2) A maintenance task that ensures consistency between the internal
 * {@link BlockingDeque} and {@link Map}. The parameters for these tasks are set
 * at build() time, else with default values. These default values are: (1)
 * initial delay: 100ms, rate: 5000ms and (2) initial delay: 100ms, delay
 * (between completed tasks): 1ms.
 * 
 * @author young-raekim
 *
 */
public class TimedAddressCache implements AddressCache {

  /* Default period after which a TimedInetAddress expires, in milliseconds. */
  private static final long ADDRESS_TIMEOUT_MILLIS_DEFAULT = 5 * 60 * 1000;

  /* For the background tasks. */
  private final ScheduledExecutorService cleanupExecutor = Executors
      .newScheduledThreadPool(1);
  private final ScheduledExecutorService maintenanceExecutor = Executors
      .newScheduledThreadPool(1);

  /* Stores the Cache data. */
  private BlockingDeque<TimedInetAddress> blockingDequeCache;
  private Map<InetAddress, Date> mapCache;

  /**
   * Private constructor for {@link TimedAddressCache} which sets instance
   * variables to values given by the {@link TimedAddressCacheBuilder}.
   * 
   * @param builder
   *          the builder at build() time
   */
  private TimedAddressCache(TimedAddressCacheBuilder builder) {
    blockingDequeCache = new LinkedBlockingDeque<>(builder.maximumCapacity);
    blockingDequeCache.addAll(builder.cacheData);
    mapCache = new ConcurrentHashMap<>(toInetAddressDateMap(builder.cacheData));

    runCleanupTask(builder.cleanupInitialDelayMillis, builder.cleanupRateMillis);
    maintainConsistencyTask(builder.maintenanceInitialDelayMillis,
        builder.maintenanceDelayMillis);
  }

  /**
   * Creates a Map whose keys are {@link InetAddress}es and whose values are
   * their expiration dates, derived from the {@link TimedInetAddress}es of the
   * given Collection.
   * 
   * @param c
   *          the input collection of {@link TimedInetAddress}es
   * @return a Map of {@link InetAddress}es to expiration dates
   */
  private static Map<InetAddress, Date> toInetAddressDateMap(
      Collection<TimedInetAddress> c) {
    // HashMap has fastest performance among Maps.
    Map<InetAddress, Date> map = new HashMap<>();

    for (Iterator<TimedInetAddress> iter = c.iterator(); iter.hasNext();) {
      TimedInetAddress curr = iter.next();
      map.put(curr.getInetAddress(), curr.getExpirationDate());
    }

    return map;
  }

  @Override
  public boolean offer(InetAddress address) {
    TimedInetAddress timedAddress = new TimedInetAddress(address, new Date(
        System.currentTimeMillis() + ADDRESS_TIMEOUT_MILLIS_DEFAULT));
    return offer(timedAddress);
  }

  /**
   * Another offer method that takes a {@link TimedInetAddress}, giving the user
   * to configure the caching time for a particular {@link InetAddress} element.
   * 
   * @param timedAddress
   *          the {@link TimedInetAddress}
   * @return {@code true} if the timedAddress was successfully added.
   *         {@code false} if the {@link InetAddress} of the given
   *         {@link TimedInetAddress} was already in the cache.
   */
  public boolean offer(TimedInetAddress timedAddress) {
    if (!contains(timedAddress.getInetAddress())) {
      boolean added = blockingDequeCache.offer(timedAddress);
      if (added) {
        mapCache.put(timedAddress.getInetAddress(),
            timedAddress.getExpirationDate());
      }

      return added;
    }

    moveFront(timedAddress.getInetAddress());

    return false;
  }

  /**
   * Moves an existing element in the cache to the front.
   * 
   * @param address
   *          the {@link InetAddress}
   * @return {@code true} if the element was successfully added to the front of
   *         the cache. {@code false} if given address does not exist.
   */
  private boolean moveFront(InetAddress address) {
    if (!contains(address)) { // no element to move to front
      return false;
    }

    TimedInetAddress existingTimedAddress = new TimedInetAddress(address,
        mapCache.get(address));
    blockingDequeCache.removeLastOccurrence(existingTimedAddress);
    blockingDequeCache.addFirst(existingTimedAddress);

    return true;
  }

  @Override
  public boolean contains(InetAddress address) {
    return mapCache.containsKey(address);
  }

  @Override
  public boolean remove(InetAddress address) {
    TimedInetAddress timedAddress = new TimedInetAddress(address);
    boolean successfullyRemoved = blockingDequeCache
        .removeLastOccurrence(timedAddress); // only one should exist anyway
    mapCache.remove(address);

    return successfullyRemoved;
  }

  @Override
  public InetAddress peek() {
    return blockingDequeCache.peekLast().getInetAddress();
  }

  @Override
  public InetAddress remove() {
    TimedInetAddress timedAddress = blockingDequeCache.pollLast();

    if (timedAddress == null) {
      return null;
    }

    InetAddress address = timedAddress.getInetAddress();
    mapCache.remove(address);

    return address;
  }

  @Override
  public InetAddress take() throws InterruptedException {
    TimedInetAddress timedAddress = blockingDequeCache.takeLast();
    InetAddress address = timedAddress.getInetAddress();
    mapCache.remove(address);

    return address;
  }

  @Override
  public void close() {
    blockingDequeCache = null;
    mapCache = null;
    cleanupExecutor.shutdownNow();
    maintenanceExecutor.shutdownNow();
  }

  @Override
  public int size() {
    return blockingDequeCache.size();
  }

  @Override
  public boolean isEmpty() {
    return (size() == 0);
  }

  /**
   * Runs a background cleanup task on a separate thread, evicting expired
   * addresses of this cache in FIFO order.
   * 
   * @param initialDelayMillis
   *          the initial delay for running the task, in milliseconds
   * @param rateMillis
   *          the frequency at which the task occurs, in milliseconds
   */
  private void runCleanupTask(long initialDelayMillis, long rateMillis) {
    cleanupExecutor.scheduleAtFixedRate(new CacheCleanupTask(),
        initialDelayMillis, rateMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Runs a background maintenance task on a separate thread, keeping the
   * internal {@link Map} consistent with respect to the internal
   * {@link LinkedBlockingDeque}, adding or removing addresses as necessary to
   * or from the {@link Map}.
   * 
   * @param initialDelayMillis
   *          the initial delay for running the task, in milliseconds
   * @param delayMillis
   *          the delay period before the next task begins, in milliseconds
   */
  private void maintainConsistencyTask(long initialDelayMillis, long delayMillis) {
    maintenanceExecutor.scheduleWithFixedDelay(new MaintainConsistencyTask(),
        initialDelayMillis, delayMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * The background cleanup task, evicting expired addresses of
   * {@link TimedAddressCache} in FIFO order.
   * 
   * @author young-raekim
   *
   */
  private final class CacheCleanupTask implements Runnable {

    @Override
    public void run() {
      // ensure referenced instance variables have been initialized to avoid
      // weird-ness from multithreading
      if (blockingDequeCache == null || mapCache == null) {
        return;
      }

      try {
        for (Iterator<TimedInetAddress> it = blockingDequeCache.iterator(); it
            .hasNext();) {
          TimedInetAddress curr = it.next();
          if (curr.isExpired()) {
            it.remove();

            mapCache.remove(curr.getInetAddress());
          }
        }
      } catch (Exception e) {
        System.err
            .println("Error in executing cache cleanup task. It will no longer be run.");
        e.printStackTrace();

        throw new RuntimeException(e);
      }
    }
  }

  /**
   * The background consistency maintenance task, ensuring that the internal
   * {@link Map} is consistent with the internal {@link LinkedBlockingDeque}.
   * Namely, if the {@link LinkedBlockingDeque} contains addresses that the
   * {@link Map} does not, these addresses will get added to the {@link Map}.
   * Similarly, if the {@link Map} contains addresses that the
   * {@link LinkedBlockingDeque} does not, these addresses will be removed from
   * the {@link Map}.
   * 
   * @author young-raekim
   *
   */
  private final class MaintainConsistencyTask implements Runnable {

    @Override
    public void run() {
      if (blockingDequeCache == null || mapCache == null) {
        return;
      }

      try {
        // If the internal BlockingDeque contains addresses that the Map does
        // not, add these addresses to the mapCache
        for (Iterator<TimedInetAddress> it = blockingDequeCache.iterator(); it
            .hasNext();) {
          TimedInetAddress curr = it.next();
          if (mapCache.get(curr) == null) {
            mapCache.put(curr.getInetAddress(), curr.getExpirationDate());
          }
        }

        // If the Map contains addresses that the BlockingDeque does
        // not, remove these entries from the Map
        for (InetAddress address : mapCache.keySet()) {
          if (!blockingDequeCache.contains(new TimedInetAddress(address))) {
            mapCache.remove(address);
          }
        }
      } catch (Exception e) {
        System.err
            .println("Error in executing cache maintenance task. It will no longer be run.");
        e.printStackTrace();

        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Builder for {@link TimedAddressCache}
   * 
   * @author young-raekim
   *
   */
  public static class TimedAddressCacheBuilder {

    /* Instance variables set to default values. Times in milliseconds. */
    // LinkedList produces iterators with the desired behavior.
    private Collection<TimedInetAddress> cacheData = new LinkedList<>();
    private int maximumCapacity = Integer.MAX_VALUE;
    private long cleanupInitialDelayMillis = 100;
    private long cleanupRateMillis = 5 * 1000;
    private long maintenanceInitialDelayMillis = 100;
    private long maintenanceDelayMillis = 1;

    /**
     * Adds the given collection of {@link InetAddress}es to the
     * {@link TimedAddressCache}, added in traversal order of the given
     * collection's iterator. Each {@link InetAddress} is instantiated as a new
     * {@link TimedAddressCache} with an expiration date of the construction
     * time plus a default duration, in milliseconds, specified by a constant.
     * Note that the {@link TimedAddressCache} is initially empty.
     * 
     * @param addresses
     *          the given collection of {@link InetAddress}es
     * @return the resulting {@link TimedAddressCacheBuilder}
     */
    public TimedAddressCacheBuilder addAddresses(
        Collection<InetAddress> addresses) {
      this.cacheData.addAll(toTimedCollection(addresses));
      return this;
    }

    /**
     * Adds the given collection of {@link InetAddress}es to the
     * {@link TimedAddressCache}, added in traversal order of the given
     * collection's iterator. Note that the {@link TimedAddressCache} is
     * initially empty.
     * 
     * @param timedAddresses
     *          the given collection of {@link TimedInetAddress}es
     * @return the resulting {@link TimedAddressCacheBuilder}
     */
    public TimedAddressCacheBuilder addTimedAddresses(
        Collection<TimedInetAddress> timedAddresses) {
      this.cacheData.addAll(timedAddresses);
      return this;
    }

    /**
     * Sets a maximum capacity for the {@link TimedAddressCache}.
     * 
     * @param maximumCapacity
     *          the capacity of the cache
     * @return
     */
    public TimedAddressCacheBuilder maximumCapacity(int maximumCapacity) {
      this.maximumCapacity = maximumCapacity;
      return this;
    }

    /**
     * Sets the {@link TimedAddressCache}'s background cleanup task to start
     * after the given initial delay.
     * 
     * @param initialDelay
     *          the initial delay
     * @param unit
     *          the unit for the initial delay
     * @return the resulting {@link TimedAddressCacheBuilder}
     */
    public TimedAddressCacheBuilder cleanupInitialDelay(long initialDelay,
        TimeUnit unit) {
      this.cleanupInitialDelayMillis = TimeUnit.MILLISECONDS.convert(
          initialDelay, unit);
      return this;
    }

    /**
     * Sets the {@link TimedAddressCache}'s background cleanup task to occur at
     * the given rate.
     * 
     * @param rate
     *          the cleanup rate
     * @param unit
     *          the unit for the cleanup rate
     * @return the resulting {@link TimedAddressCacheBuilder}
     */
    public TimedAddressCacheBuilder cleanupRate(long rate, TimeUnit unit) {
      this.cleanupRateMillis = TimeUnit.MILLISECONDS.convert(rate, unit);
      return this;
    }

    /**
     * Sets the {@link TimedAddressCache}'s background maintenance task to start
     * after the given initial delay.
     * 
     * @param initialDelay
     *          the initial delay
     * @param unit
     *          the unit for the initial delay
     * @return
     */
    public TimedAddressCacheBuilder maintenanceInitialDelay(long initialDelay,
        TimeUnit unit) {
      this.maintenanceInitialDelayMillis = TimeUnit.MILLISECONDS.convert(
          initialDelay, unit);
      return this;
    }

    /**
     * Sets the {@link TimedAddressCache}'s background maintenance task to
     * repeat, on completion, after the given delay.
     * 
     * @param delay
     *          the delay
     * @param unit
     *          the unit for the delay
     * @return
     */
    public TimedAddressCacheBuilder maintenanceDelay(long delay, TimeUnit unit) {
      this.maintenanceDelayMillis = TimeUnit.MILLISECONDS.convert(delay, unit);
      return this;
    }

    /**
     * Builds a {@link TimedAddressCache} from the
     * {@link TimedAddressCacheBuilder}'s values.
     * 
     * @return the resulting {@link TimedAddressCache}
     */
    public TimedAddressCache build() {
      return new TimedAddressCache(this);
    }

    /**
     * Converts a given collection of {@link InetAddress}es into a collection of
     * {@link TimedInetAddress}es in traversal order of the input's iterator.
     * Each new TimedInetAddress gets a default expiration date set to the time
     * of creation plus a default timeout value.
     * 
     * @param c
     *          the collection to be converted
     * @return the converted collection
     */
    private static Collection<TimedInetAddress> toTimedCollection(
        Collection<InetAddress> c) {
      // LinkedList chosen for well-behaved iterator.
      Collection<TimedInetAddress> timedCollection = new LinkedList<>();

      for (Iterator<InetAddress> cIter = c.iterator(); cIter.hasNext();) {
        InetAddress curr = cIter.next();
        timedCollection.add(new TimedInetAddress(curr, new Date(System
            .currentTimeMillis() + ADDRESS_TIMEOUT_MILLIS_DEFAULT)));
      }

      return timedCollection;
    }
  }
}
