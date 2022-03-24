package liquibase.license; 

/**
 * A Location is an object that has a name, LocationType, value, and path. There are currently
 * four LocationTypes in use. The FILE_PATH type is the most direct. The ENVIRONMENT_VARIABLE
 * and SYSTEM_PROPERTY types are things that can refer to a path. These three types eventually
 * lead to a path on the filesystem that can be used to get a file.
 * The fourth type is BASE64_STRING. For this type, the value is a BASE64 encoded string of a
 * license file.
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

  /**
   * Only use this constructor for things where the name and the value are the same
   * 
   * @param value
   */
  public Location(String value) {
    this(value, value);
  }

  @Override
  public String toString() {
    int substring_length = 10;
    if (value.length() < 10) {
      substring_length = value.length();
    }
    return String.format("Base64 string starting with '%s' (%s)", value.substring(0,substring_length), name);
  }

  public String toDisplayString() {
    return String.format("(%s)", name);
  }

  public String getPath() {
    String path = this.value;
    if (path != null && path.startsWith("file:")){
      path = path.substring(5);
    }
    return path;
  }

}
