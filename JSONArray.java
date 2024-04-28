import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * JSON arrays.
 */
public class JSONArray implements JSONValue {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The underlying array.
   */
  ArrayList<JSONValue> values;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Build a new array.
   */
  public JSONArray() {
    this.values = new ArrayList<JSONValue>();
  } // JSONArray() 

  // +-------------------------+-------------------------------------
  // | Standard object methods |
  // +-------------------------+

  /**
   * Convert to a string (e.g., for printing).
   */
  public String toString() {
    String result = "[";

    for(int i = 0; i < values.size(); i++) {
      result += values.get(i).toString() + ", ";
    } // for
    
    return result + "]";          
  } // toString()

  /**
   * Compare to another object.
   */
  public boolean equals(Object other) {
    if (!(other instanceof JSONArray)) {
      return false;
    } // if

    if (this.values.size() != ((JSONArray) other).values.size()) {
      return false;
    } // if

    // Checking each element of array
    for (int i = 0; i < values.size(); i++) {
      if (!this.values.get(i).equals(((JSONArray) other).values.get(i))) {
        return false;
      } // if
    } // for

    return true;
  } // equals(Object)

  /**
   * Compute the hash code.
   */
  public int hashCode() {
    return this.values.hashCode();
  } // hashCode()

  // +--------------------+------------------------------------------
  // | Additional methods |
  // +--------------------+

  /**
   * Write the value as JSON.
   */
  public void writeJSON(PrintWriter pen) {
    int arrayLength = this.values.size();

    pen.printf("[");
    for (int i = 0; i < arrayLength - 1; i++) {
      this.values.get(i).writeJSON(pen);
      pen.printf(", ");
    } // for

    // Print last item
    this.values.get(arrayLength - 1).writeJSON(pen);
    pen.printf("]");
  } // writeJSON(PrintWriter)

  /**
   * Get the underlying value.
   */
  public ArrayList<JSONValue> getValue() {
    return this.values;
  } // getValue()

  // +---------------+-----------------------------------------------
  // | Array methods |
  // +---------------+

  /**
   * Add a value to the end of the array.
   */
  public void add(JSONValue value) {
    this.values.add(value);
  } // add(JSONValue)

  /**
   * Get the value at a particular index.
   */
  public JSONValue get(int index) throws IndexOutOfBoundsException {
    return this.values.get(index);
  } // get(int)

  /**
   * Get the iterator for the elements.
   */
  public Iterator<JSONValue> iterator() {
    return this.values.iterator();
  } // iterator()

  /**
   * Set the value at a particular index.
   */
  public void set(int index, JSONValue value) throws IndexOutOfBoundsException {
    this.values.set(index, value);
  } // set(int, JSONValue)

  /**
   * Determine how many values are in the array.
   */
  public int size() {
    return this.values.size();
  } // size()
} // class JSONArray
