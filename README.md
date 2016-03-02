# Assignment README
The following was completed for a take-home interview.

The task was to implement a cache from the provided AddressCache.java interface with the following requirements: (1) a LIFO retrieval policy and (2) a FIFO eviction policy with a (3) internal cleanup task that periodically removes the oldest elements (i.e. InetAddresses) from the cache.

The following documents asmptotic complexity for the implemented methods, design decisions, and general explanations (although most explanations for the source code can be found in comments). It is comprised of two parts: (1) the source code and (2) tests. Note that as per the instructions, we prefer (in order): functionally correct and easy-to read code, performance over space, and asymptotic runtime over amortized runtime.

**Built using Java 1.8.**

## (1) Source Code

### TimedAddressCache.java
This class implements `AddressCache`.

##### Data structures
High-performance alternatives for the internal data structures such as Ben Manes's *caffeine* and *concurrentlinkedhashmap* and Guava's *cache* were considered, however were discounted because of a lack of ordering and inherent overhead. Instead, I opted for: (1) a `LinkedBlockingDeque` (the main underlying data structure) and (2) a `ConcurrentHashMap`. The reason for these particular `Deque` and `Map` implementations was to allow for thread-safe modification in the cleanup task. The reason for the auxiliary `ConcurrentHashMap` was to improve the asymptotic performance  of `contains()`. Note that *amortized* performance of `remove(InetAddress)` by means of lazy deletion (i.e. flagging the deletion and then actually removing the data on calls to *peek, size, etc.*) could have been improved to *O(1)* and all others (amortized) held the same. However, as aforementioned, we place greater weight on asymptotic runtime over amortized.

##### Asymptotic complexity

Here we analyze the asymptotic complexity of the methods implemented in `TimedAddressCache`.

> [LinkedBlockingDeque Javadoc](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/LinkedBlockingDeque.html): Most operations run in constant time (ignoring time spent blocking). Exceptions include remove, removeFirstOccurrence, removeLastOccurrence, contains, iterator.remove(), and the bulk operations, all of which run in linear time.

- `offer()`: We first check to see if the offered address already exists (O(1) against internal `ConcurrentHashMap`). If it does, we remove the element from the `LinkedBlockingDeque` (O(n)) and add it to the front (O(1)). If it does not, we add it to both the Map and the Deque (both O(1)). Hence, the worst-case asymptotic time is **O(n)**.
- `contains()`: We simply check against the `ConcurrentHashMap` for **O(1)** time.
- `remove(InetAddress)`: Removing a particular element from the Deque requires O(n) time (the Map takes O(1) time). Thus, we have **O(n)** time.
- `peek()`: As with any other `Queue`/`Deque`, this operation is constant: **O(1)**.
- `remove()`: As with any other `Queue`/`Deque` (and, in this case, our `Map`), this operation is constant: **O(1)**.
- `take()`: As with any other `Queue`/`Deque` (and, in this case, our `Map`), this operation is constant: **O(1)**.
- `size()`: As with any other `Queue`/`Deque`, this operation is constant: **O(1)**.
- `isEmpty()`: As with any other `Queue`/`Deque`, this operation is constant: **O(1)**.
- `runCleanupTask()`: Here we iterate on an Iterable of the `LinkedBlockingDeque`, removing expired addresses. According to the documentation: "iterator.remove() ... run(s) in linear time." Thus, we in fact have a runtime of **O(n^2)**. However, note that this task runs on a separate thread.
- `maintainConsistencyTask()`: Also **O(n^2)** for similar aforementioned reasons.

##### Assumptions & behaviors
- The default behavior when this cache begins to run out of memory... is to just let it happen! To allow for a better behaved cache, build with the maximumCapacity() parameter.
- Every input `InetAddress` must have an associated expiration date. If one is not assigned, the expiration date is set to a default period of time after the addition of a `InetAddress` into the cache.
- The internal BlockingDeque and the internal Map instances rely on the other to be 'in sync' with one another. While a background thread is running (nearly) constantly to ensure that the two are synchronized with one another, this is not guaranteed.

##### TimedAddressCacheBuilder
`TimedAddressCacheBuilder` follows the builder pattern for creating instances of the `TimedAddressCache` class. It is a nested class in the `TimedAddressCache` class. The reasons for a builder class were twofold: (1) due to type erasure, Java does not distinguish between collections of type `InetAddress` and `TimedInetAddress`, thus necessitating the need for different methods that could accept Collections of differing types and (2) easier API by which to create TimedAddressCaches. Both builder methods for `Collection`s of `InetAddress`es and `TimedInetAddress`es were included because the latter was contrived (and preferred) and the former was given by the assignment.

### TimedInetAddress.java

This class associates an `InetAddress` with an expiration date, created specifically for the demands of this assignment. For Collections, unlike Maps, where a simple `InetAddress`->ExpirationDate Key->Value did the job, I needed an concrete way of associating a particular `InetAddress` with an expiration date. Rather than have a default expiration date, I did not permit this class to be instantiated with any null values. This greatly reduced null-pointer checking in various areas of my `TimedAddressCache` and Builder implementations. More info in the comments.

## (2) Tests

Basic JUnit testing was done for all public methods of TimedAddressCache and TimedInetAddress. These are included in the `src/test/java` directory and can be run on the command line by calling `mvn test` from the root directory.
