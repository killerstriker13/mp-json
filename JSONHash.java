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
  @SuppressWarnings("unchecked")
  public String toString() {
    String result = "";
    Iterator iter = this.iterator();
    while (iter.hasNext()){
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

    if(((JSONHash)other).size() != this.size()) {
      return false;
    } // if
    
    for (int i = 0; i < this.buckets.length; i++) {
      @SuppressWarnings("unchecked")
      ArrayList<KVPair<JSONString,JSONValue>> thisList = (ArrayList<KVPair<JSONString,JSONValue>>) this.buckets[i];
      @SuppressWarnings("unchecked")
      ArrayList<KVPair<JSONString,JSONValue>> otherList = (ArrayList<KVPair<JSONString,JSONValue>>) ((JSONHash) other).buckets[i];
      
      if (thisList == null && otherList == null) {
          return true;
      } // if both null
      if (thisList == null || otherList == null) {
          return false;
      } // if either one null only
      
      for (int index = 0; index < thisList.size(); index++) {
        if (!thisList.get(index).key().equals(otherList.get(index).key())) {
          return false;
        } // if key not equals

        if (!thisList.get(index).value().equals(otherList.get(index).value())) {
          return false;
        } // if values not equals
      } // for each pair in the bucket
    } // for each bucket
    
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
      throw new IndexOutOfBoundsException("Invalid key: " + key.toString());
    } else {
      for (KVPair<JSONString, JSONValue> pair : alist) {
        if (pair.key().equals(key)) {
          return pair.value();
        } // if (pair.key().equals(key))
      } // for (Pair<JSONString, JSONValue> pair: alist)
      throw new IndexOutOfBoundsException("Invalid key: " + key.toString());
    } // get
  } // get(JSONString)

  /**
   * Get all of the key/value pairs.
   */
  public Iterator<KVPair<JSONString, JSONValue>> iterator() {
    return new Iterator<KVPair<JSONString,JSONValue>>() {
      int numVisited = 0;
      int[] nextPos = {0,0};
      int currentBucket = 0;
      int nextPair = 0;

      public boolean hasNext() {
        if(numVisited < JSONHash.this.size()){
          return true;
        } // if
        else {
          return false;
        } // else
      } // hasNext()

      @SuppressWarnings("unchecked")
      public KVPair<JSONString,JSONValue> next() {
        if (!this.hasNext()) {
          throw new IndexOutOfBoundsException();
        }

        ArrayList<KVPair<JSONString, JSONValue>> alist = 
            (ArrayList<KVPair<JSONString, JSONValue>>) JSONHash.this.buckets[currentBucket];
        
        // if the bucket is null then skip it
        if (alist == null) {
          currentBucket++;
          nextPair = 0;
          this.next();
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
    int flag = 0;
    // If there are too many entries, expand the table.
    if (this.size > (this.buckets.length * LOAD_FACTOR)) {
      expand();
    } // if
    // Find out where the key belongs and put the pair there.
    int index = find(key);
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
    } // if-else
    if (flag != -1) { // we did not replace the value
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
