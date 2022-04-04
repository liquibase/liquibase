package liquibase.license; 

/**
 * A Location is an object that has a name, and value, used to represent a license. The value is a BASE64 encoded string
 * of a license.
 *  
 * @author Steve Donie
 *
 */
public class Location {

  public String name;
  public String value;

  public Location(String name, String value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    int substring_length = Math.min(value.length(), 10);
    return String.format("Base64 string starting with '%s' (%s)", value.substring(0, substring_length), name);
  }

  public String getValue() {
    return value;
  }
}
