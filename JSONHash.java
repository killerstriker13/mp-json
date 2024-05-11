import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * JSON hashes/objects.
 * 
 * @author Arsal Shaikh
 * @author Pranav Kapoor Bhandari
 * @author Shibam Mukhopadhyay
 * @author Samuel A. Rebelsky (Hash Table lab)
 * @author Garikai Gijima (Hash Table lab partner)
 */
public class JSONHash implements JSONValue {

  // +-----------+-------------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The load factor for expanding the table.
   */
  static final double LOAD_FACTOR = 0.5;

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+
  /**
   * The number of values currently stored in the hash table. We use this to
   * determine when to expand the hash table.
   */
  int size = 0;

  /**
   * The array that we use to store the key/value pairs.
   */
  Object[] buckets = new Object[4];

  /**
   * Our helpful random number generator, used primarily when expanding the size
   * of the table..
   */
  Random rand;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  public JSONHash() {
    this.rand = new Random();
  } // JSONHash()

  // +-------------------------+-------------------------------------
  // | Standard object methods |
  // +-------------------------+

   /**
   * Convert to a string (e.g., for printing).
   */
  public String toString() {
    String result = "";
    Iterator<KVPair<JSONString,JSONValue>> iter = this.iterator();
    while (iter.hasNext()) {
      KVPair<JSONString, JSONValue> pair = (KVPair<JSONString, JSONValue>) iter.next();
      String kvPair = pair.key().toString() + " : " + pair.value().toString();
      result+= kvPair;
      if (iter.hasNext()) {
        result += ",";
      } // if
    } // while
    return "{"+result+"}"; 
  } // toString()

  /**
   * Compare to another object.
   */
  public boolean equals(Object other) {
    if (!(other instanceof JSONHash)) {
      return false;
    } // if

    Iterator<KVPair<JSONString,JSONValue>> iter = this.iterator();
    JSONHash otherHash = (JSONHash) other;
    JSONString thisKey;
    JSONValue otherValue;
    while (iter.hasNext()) {
      // Get pair from this
      KVPair<JSONString, JSONValue> pair = (KVPair<JSONString, JSONValue>) iter.next();
      thisKey = pair.key();
      JSONValue thisValue = pair.value();
      
      try {
        otherValue = otherHash.get(thisKey);
        if (!thisValue.equals(otherValue)) {
          return false;
        } // if
      } catch (IndexOutOfBoundsException e) {
        // if value not found
        return false;
      } // try catch

    } // while
    
    return true; 
  } // equals(Object)

  /**
   * Compute the hash code.
   */
  public int hashCode() {
    return this.buckets.hashCode();
  } // hashCode()

  // +--------------------+------------------------------------------
  // | Additional methods |
  // +--------------------+

  /**
   * Write the value as JSON.
   */
  public void writeJSON(PrintWriter pen) {
    pen.printf(this.toString());
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
    try {
      get(key);
      return true;
    } catch (Exception e) {
      return false;
    } // try catch
  } // containsKey(JSONString)

  /**
   * Get the value associated with a key.
   */
  public JSONValue get(JSONString key) {
    int index = find(key);
    @SuppressWarnings("unchecked")
    ArrayList<KVPair<JSONString, JSONValue>> alist =
        (ArrayList<KVPair<JSONString, JSONValue>>) buckets[index];
    
    if (alist == null) {
      throw new IndexOutOfBoundsException("Invalid key: " + key.toString());
    } 

    for (KVPair<JSONString, JSONValue> pair : alist) {
      if (pair.key().equals(key)) {
        return pair.value();
      } // if
    } // for

    // if not found
    throw new IndexOutOfBoundsException("Invalid key: " + key.toString());
  } // get(JSONString)

  /**
   * Get all of the key/value pairs.
   */
  public Iterator<KVPair<JSONString, JSONValue>> iterator() {
    return new Iterator<KVPair<JSONString,JSONValue>>() {
      int numVisited = 0;
      int currentBucket = 0;
      int nextPair = 0;

      public boolean hasNext() {
        return numVisited < JSONHash.this.size();
      } // hasNext()

      @SuppressWarnings("unchecked")
      public KVPair<JSONString,JSONValue> next() {
        if (!this.hasNext()) {
          throw new IndexOutOfBoundsException();
        } // if

        ArrayList<KVPair<JSONString, JSONValue>> alist = 
            (ArrayList<KVPair<JSONString, JSONValue>>) JSONHash.this.buckets[currentBucket];
        
        // if the bucket is null then skip it
        if (alist == null) {
          currentBucket++;
          nextPair = 0;
          return this.next();
        } // if
        
        int index = nextPair;
        nextPair = (index + 1) % alist.size();
        
        // if reached end of alist then go to the next bucket
        if (nextPair == 0) {
          currentBucket++;
        } // if

        numVisited++;
        return alist.get(index);
      } // next()
    }; // new Iterator
  } // iterator()

  /**
   * Set the value associated with a key.
   */
  @SuppressWarnings("unchecked")
  public void set(JSONString key, JSONValue value) {
    boolean keyExists = false;
    
    // If there are too many entries, expand the table.
    if (this.size > (this.buckets.length * LOAD_FACTOR)) {
      expand();
    } // if

    // Find out where the key belongs.
    int index = find(key);
    
    // Repersents the bucket
    ArrayList<KVPair<JSONString, JSONValue>> alist =
        (ArrayList<KVPair<JSONString, JSONValue>>) this.buckets[index];
    
    // If bucket is empty then create a new list there.
    if (alist == null) {
      alist = new ArrayList<KVPair<JSONString, JSONValue>>();
      this.buckets[index] = alist;
    } // if

    for (int i = 0; i < alist.size(); i++) {
      if (alist.get(i).key().equals(key)) {
        KVPair<JSONString, JSONValue> pair = new KVPair<JSONString, JSONValue>(key, value);
        alist.set(i, pair);
        keyExists = true;
      } // if
    } // for

    // If value was not replaced
    if (keyExists == false) {
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
   * Expands the capacity of the table.
   */
  @SuppressWarnings("unchecked")
  private void expand() {
    // Figure out the size of the new table
    int newSize = (2 * this.buckets.length) + rand.nextInt(10);
    // Remember the old table
    Object[] oldBuckets = this.buckets;
    // Create a new table of that size.
    this.buckets = new Object[newSize];
    // Move all buckets from the old table to their appropriate
    // location in the new table.
    this.size = 0;
    for (int i = 0; i < oldBuckets.length; i++) {
      if (oldBuckets[i] == null) {
        continue;
      } // if
      for (KVPair<JSONString, JSONValue> pair : (ArrayList<KVPair<JSONString, JSONValue>>) oldBuckets[i]) {
        this.set(pair.key(), pair.value());
      } // for
    } // for
  } // expand()

  /**
   * Calculate the hash of the key to get the index to the store the key.
   */
  private int find(JSONString key) {
    return Math.abs(key.hashCode()) % this.buckets.length;
  } // find(JSONString)
} // class JSONHash
