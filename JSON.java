import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.io.BufferedReader;

/**
 * Utilities for our simple implementation of JSON.
 * 
 * @author Samuel A. Rebelsky
 * @author Arsal Shaikh
 * @author Pranav K Bhandari
 * @author Shibam Mukhopadhyay
 */
public class JSON {
  // +---------------+-----------------------------------------------
  // | Static fields |
  // +---------------+

  /**
   * The current position in the input.
   */
  static int pos;

  // +----------------+----------------------------------------------
  // | Static methods |
  // +----------------+

  /**
   * Parse a string into JSON.
   */
  public static JSONValue parse(String source) throws ParseException, IOException {
    StringReader reader = new StringReader(source);
    return parse(reader);
  } // parse(String)

  /**
   * Parse a file into JSON.
   */
  public static JSONValue parseFile(String filename) throws ParseException, IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filename)); // to-fix
    JSONValue result = parse(reader);
    reader.close();
    return result;
  } // parseFile(String)

  /**
   * Parse JSON from a reader.
   */
  public static JSONValue parse(Reader source) throws ParseException, IOException {
    pos = 0;
    JSONValue result = parseKernel(source);
    if (-1 != skipWhitespace(source)) {
      throw new ParseException("Characters remain at end", pos);
    }
    return result;
  } // parse(Reader)

  // +---------------+-----------------------------------------------
  // | Local helpers |
  // +---------------+

  /**
   * Parse JSON from a reader, keeping track of the current position
   */
  static JSONValue parseKernel(Reader source) throws ParseException, IOException {
    int ch;
    ch = skipWhitespace(source);
    if (-1 == ch) {
      throw new ParseException("Unexpected end of file", pos);
    }
    char curr = (char) ch;
    try {
      return type(source, curr);
    } catch (Exception e) {
      throw new ParseException("Character not recognized", pos);
    }
  } // parseKernel(Reader)

  /*
   * Based on the character read, figure out the type of JSON to parse
   */
  static JSONValue type(Reader source, char beginCh) throws Exception {
    if (Character.isDigit(beginCh)) {
      try {
        return parseJSONNum(source, false);
      } catch (Exception e) {
        throw new ParseException("Character not recognized", pos);
      } // try catch
    } // if

    switch (beginCh) {
      case '"':
        return parseJSONString(source);
      case '-':
        return parseJSONNum(source, true);
      case '{':
        return parseJSONHash(source);
      case '[':
        return parseJSONArray(source);
      case 'n':
      case 'f':
      case 't':
        return parseJSONConstant(source);
      default:
        throw new ParseException("Character not recognized", pos);
    } // switch case
  } // type(Reader, char)

  /**
   * Get the next character from source, skipping over whitespace.
   */
  static int skipWhitespace(Reader source) throws IOException {
    int ch;
    do {
      ch = source.read();
      ++pos;
    } while (isWhitespace(ch));
    return ch;
  } // skipWhitespace(Reader)

  /**
   * Determine if a character is JSON whitespace (newline, carriage return, space, or tab).
   */
  static boolean isWhitespace(int ch) {
    return (' ' == ch) || ('\n' == ch) || ('\r' == ch) || ('\t' == ch);
  } // isWhiteSpace(int)

  /*
   * Check if the character is special!
   */
  private static char specialChar(Reader source, char currChar) throws IOException, ParseException {
    if (currChar == '\\') {
      source.mark(0);
      char nextChar = (char) source.read();
      switch (nextChar) {
        case '\\':
          return '\\';
        case 'n':
          return '\n';
        case 't':
          return '\t';
        case 'r':
          return '\r';
        case '"':
          return '\"';
        case '/':
          return '/';
        case 'b':
          return '\b';
        case 'f':
          return '\f';
        case 'u':
          return unicodeConverter(source);
        default:
          throw new ParseException(
              "Invalid String syntax, backslash not followed by valid character", pos);
      } // switch case
    } // if-else
    return currChar;
  } // specialChar(Reader, char)

  /*
   * Reads from source and converts into unicode character.
   */
  private static char unicodeConverter(Reader source) throws IOException, ParseException {
    StringBuilder hexValue = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      source.mark(0);
      char currChar = (char) source.read();
      if (Character.isDigit(currChar) || ('A' <= currChar && currChar <= 'F')) {
        hexValue.append(Character.toUpperCase(currChar));
      } else {
        throw new ParseException("Invalid hexadecimal unicode character", pos);
      }
    }
    return (char) Integer.parseInt(hexValue.toString(), 16);
  } // unicodeConverter(Reader)

  /**
   * Parse JSONString.
   */
  private static JSONString parseJSONString(Reader source) throws IOException, ParseException {
    String output = "";
    int inputChar = skipWhitespace(source);
    char currentChar = (char) inputChar;

    // Read till end of JSONString
    while (currentChar != '"') {
      // deal with special charactes
      output += specialChar(source, currentChar);

      source.mark(1);
      inputChar = source.read();
      currentChar = (char) inputChar;

      // If end of file
      if (inputChar == -1) {
        throw new ParseException("Unexpected end of file", pos);
      } // if
    } // while
    return new JSONString(output);
  } // parseString(Reader source)

  /**
   * Parse JSONNum, returns JSONInteger or JSONReal.
   */
  private static JSONValue parseJSONNum(Reader source, boolean isNegative)
      throws IOException, ParseException {
    // initialize values
    int inputChar;
    boolean seenExponent = false;
    boolean seenSign = false;
    boolean seenDecimal = false;
    String output = "";

    if (isNegative) {
      output += '-';
    } else {
      // Unread the first digit
      source.reset();
    } // if else


    // Read character
    inputChar = skipWhitespace(source);
    char currentChar = (char) inputChar;

    while (currentChar != ' ' || inputChar != -1) {
      if (!validateChar(currentChar, seenDecimal, seenExponent, seenSign)) {
        break;
      } // if

      switch (currentChar) {
        case '.':
          seenDecimal = true;
          break;
        case 'e':
          seenExponent = true;
          break;
        case '+':
          seenSign = true;
          break;
        case '-':
          seenSign = true;
          break;
        default:
          break;
      } // switch case

      output += currentChar;

      // Read next char
      source.mark(0);
      inputChar = source.read();
      currentChar = (char) inputChar;
    } // while


    if (seenExponent || seenDecimal) {
      return new JSONReal(output.toString());
    } // if

    return new JSONInteger(output.toString());
  } // parseNum(Reader, boolean)

  /*
   * Validate characters in a number.
   */
  private static boolean validateChar(char currentChar, boolean seenDecimal, boolean seenExponent,
      boolean seenSign) throws ParseException {
    String validChars = ".eE-+";
    if (!Character.isDigit(currentChar) && !validChars.contains(String.valueOf(currentChar))) {
      return false;
    } // if

    // if seeing for second time
    if (currentChar == '.' && seenDecimal) {
      throw new ParseException("Invalid real value. Cannot have more than one decimal point", pos);
    } // if
    if ((currentChar == 'e' || currentChar == 'E') && seenExponent) {
      throw new ParseException("Invalid exponent notation", pos);
    } // if
    if (seenSign && !seenExponent) {
      return false; // Sign can only be added after exponent
    } // if
    return true;
  } // validateChar(char, boolean, boolean, boolean)


  /*
   * Parse JSONArray.
   */
  private static JSONArray parseJSONArray(Reader source) throws Exception {
    JSONArray output = new JSONArray();
    int inputChar = skipWhitespace(source);
    char currentChar = (char) inputChar;

    // if array is empty
    if (currentChar == ']') {
      inputChar = skipWhitespace(source);
      currentChar = (char) inputChar;
      return output;
    } // if

    // Until end of file
    while (inputChar != -1) {
      JSONValue arrayElement = type(source, currentChar);
      output.add(arrayElement);
      source.reset();

      inputChar = skipWhitespace(source);
      currentChar = (char) inputChar;
      if (currentChar == '"' && (arrayElement instanceof JSONString)) {
        inputChar = skipWhitespace(source);
        currentChar = (char) inputChar;
      } // if

      // if end of array
      if (currentChar == ']') {
        inputChar = skipWhitespace(source);
        currentChar = (char) inputChar;
        return output;
      } // if

      // Check for comma
      if (currentChar != ',') {
        throw new ParseException("Comma not found. Instead found " + currentChar, pos);
      } // if

      inputChar = skipWhitespace(source);
      currentChar = (char) inputChar;
    } // while


    throw new ParseException("Invalid array!", pos);
  } // parseJSONArray(Reader)

  /*
   * Parse JSONHash.
   */
  private static JSONHash parseJSONHash(Reader source) throws Exception {
    JSONHash output = new JSONHash();
    JSONValue key;
    JSONValue value;

    int inputChar = skipWhitespace(source);
    char currentChar = (char) inputChar;

    // if hashtable is empty
    if (currentChar == '}') {
      inputChar = skipWhitespace(source);
      return output;
    } // if

    // Until end of file
    while (inputChar != -1) {
      // Read JSONKey
      key = type(source, currentChar);

      // Verify key type
      if (!(key instanceof JSONString)) {
        throw new ParseException("Invalid key. JSON key must be of type String", pos);
      } // if

      // Read next character
      inputChar = skipWhitespace(source);
      currentChar = (char) inputChar;
      if (currentChar != ':') {
        throw new ParseException("Invalid hash syntax", pos);
      } // if

      // Read JSONValue
      source.mark(0);
      inputChar = skipWhitespace(source);
      currentChar = (char) inputChar;
      value = type(source, currentChar);

      output.set((JSONString) key, value);
      source.reset();

      // Check for end of string
      inputChar = skipWhitespace(source);
      currentChar = (char) inputChar;


      // if starting new pair
      if (currentChar == '"') {
        inputChar = skipWhitespace(source);
      } // if

      // if end of hashtable
      if (currentChar == '}') {
        inputChar = skipWhitespace(source);
        return output;
      } // if

      // if not a new pair
      if (currentChar != ',') {
        throw new ParseException("Comma not found. Instead found " + currentChar, pos);
      } // if


      inputChar = skipWhitespace(source);
      currentChar = (char) inputChar;
    } // while


    throw new ParseException("Invalid Array", pos);
  } // parseJSONHash(Reader)

  /*
   * Parse JSONConstant.
   */
  private static JSONConstant parseJSONConstant(Reader source) throws IOException, ParseException {
    String constant = "";

    source.reset();
    int inputChar = skipWhitespace(source);
    char currentChar = (char) inputChar;

    // Read five characters
    while (constant.length() <= 5) {
      constant += currentChar;

      if (constant.equals("null")) {
        skipWhitespace(source);
        return new JSONConstant(null);
      } // if
      if (constant.equals("true")) {
        skipWhitespace(source);
        return new JSONConstant(true);
      } // if
      if (constant.equals("false")) {
        skipWhitespace(source);
        return new JSONConstant(false);
      } // if

      inputChar = skipWhitespace(source);
      currentChar = (char) inputChar;
    } // while

    throw new ParseException("Invalid Constant", pos);
  } // parseJSONConstant(Reader)
} // class JSON
