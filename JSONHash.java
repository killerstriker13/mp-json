import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * JSON hashes/objects.
 */
public class JSONHash implements JSONValue {

  // +-----------+-------------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The load factor for expanding the table.
   */
  static final double LOAD_FACTOR = 0.5;

  /**
   * The capacity of the hash table at initialization.
   */
  static final int INITIAL_CAPACITY = 41;

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+
  /**
   * The number of values currently stored in the hash table. We use this to determine when to
   * expand the hash table.
   */
  int size = 0;

  /**
   * The array that we use to store the ArrayList of key/value pairs. (We use an array, rather than
   * an ArrayList, because we want to control expansion and ArrayLists of ArrayLists are just
   * weird.)
   */
  Object[] buckets;

  /**
   * Our helpful random number generator, used primarily when expanding the size of the table..
   */
  Random rand;


  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  public JSONHash() {
    this.rand = new Random();
    this.clear();
  } // JSONHash()

  // +-------------------------+-------------------------------------
  // | Standard object methods |
  // +-------------------------+

  /**
   * Convert to a string (e.g., for printing).
   */
  public String toString() {
    return ""; // STUB
  } // toString()

  /**
   * Compare to another object.
   */
  public boolean equals(Object other) {
    return true; // STUB
  } // equals(Object)

  /**
   * Compute the hash code.
   */
  public int hashCode() {
    return 0; // STUB
  } // hashCode()

  // +--------------------+------------------------------------------
  // | Additional methods |
  // +--------------------+

  /**
   * Write the value as JSON.
   */
  public void writeJSON(PrintWriter pen) {
    // STUB
  } // writeJSON(PrintWriter)

  /**
   * Get the underlying value.
   */
  public Iterator<KVPair<JSONString, JSONValue>> getValue() {
    return this.iterator();
  } // getValue()

  // +-------------------+-------------------------------------------
  // | Hashtable methods |
  // +-------------------+

  public boolean containsKey(JSONString key) {
    // STUB/HACK
    try {
      get(key);
      return true;
    } catch (Exception e) {
      return false;
    } // try/catch
  } // containsKey(K)

  /**
   * Get the value associated with a key.
   */
  public JSONValue get(JSONString key) {
    int index = find(key);
    @SuppressWarnings("unchecked")
    ArrayList<KVPair<JSONString, JSONValue>> alist =
        (ArrayList<KVPair<JSONString, JSONValue>>) buckets[index];
    if (alist == null) {
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    } else {
      for (KVPair<JSONString, JSONValue> pair : alist) {
        if (pair.key().equals(key)) {
          return pair.value();
        } // if (pair.key().equals(key))
      } // for (Pair<JSONString, JSONValue> pair: alist)
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    } // get
  } // get(JSONString)

  /**
   * Get all of the key/value pairs.
   */
  public Iterator<KVPair<JSONString, JSONValue>> iterator() {
    return null; // STUB
  } // iterator()

  /**
   * Set the value associated with a key.
   */
  public void set(JSONString key, JSONValue value) {
    int flag = 0;

    // If there are too many entries, expand the table.
    if (this.size > (this.buckets.length * LOAD_FACTOR)) {
      expand();
    } // if there are too many entries

    // Find out where the key belongs and put the pair there.
    int index = find(key);
    @SuppressWarnings("unchecked")
    ArrayList<KVPair<JSONString, JSONValue>> alist =
        (ArrayList<KVPair<JSONString, JSONValue>>) this.buckets[index];

    // Special case: Nothing there yet
    if (alist == null) {
      alist = new ArrayList<KVPair<JSONString, JSONValue>>();
      this.buckets[index] = alist;
    } else {
      for (int i = 0; i < alist.size(); i++) {
        if (alist.get(i).key().equals(key)) {
          KVPair<JSONString, JSONValue> pair = new KVPair<JSONString, JSONValue>(key, value);
          alist.set(i, pair);
          flag = -1;
        } // if
      } // for
    } // if else

    if (flag != -1) {
      alist.add(new KVPair<JSONString, JSONValue>(key, value));
      ++this.size;
    } // if
    
  } // set(JSONString, JSONValue)

  /**
   * Find out how many key/value pairs are in the hash table.
   */
  public int size() {
    return this.size;
  } // size()

  /**
   * Clear the whole table.
   */
  public void clear() {
    this.buckets = new Object[INITIAL_CAPACITY];
    this.size = 0;
  } // clear()

  @SuppressWarnings("unchecked")
  void expand() {
    // Figure out the size of the new table
    int newSize = 2 * this.buckets.length + rand.nextInt(10);
    // Remember the old table
    Object[] oldBuckets = this.buckets;
    // Create a new table of that size.
    this.buckets = new Object[newSize];
    // Move all buckets from the old table to their appropriate
    // location in the new table.
    for (int i = 0; i < oldBuckets.length; i++) {
      if (oldBuckets[i] == null) {
        continue;
      } // if
      for (KVPair<JSONString, JSONValue> pair : (ArrayList<KVPair<JSONString, JSONValue>>) oldBuckets[i]) {
        this.set(pair.key(), pair.value());
      } // for
    } // for
  } // expand()


  int find(JSONString key) {
    return Math.abs(key.hashCode()) % this.buckets.length;
  } // find(K)
} // class JSONHash
