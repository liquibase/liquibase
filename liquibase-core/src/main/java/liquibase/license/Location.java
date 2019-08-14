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
  public LocationType type;
  public String value;

  public Location(String name, LocationType type, String value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }

  /**
   * Only use this constructor for things where the name and the value are the same
   * 
   * @param value
   * @param type
   */
  public Location(String value, LocationType type) {
    this(value, type, value);
  }

  @Override
  public String toString() {
    switch (type) {
    case ENVIRONMENT_VARIABLE:
      return String.format("Environment variable '%s' (%s)", name, getPath());

    case FILE_PATH:
      return String.format("File path '%s' (%s)", value, name);

    case SYSTEM_PROPERTY:
      return String.format("System property '%s' (%s)", value, name);

      case BASE64_STRING:
        int substring_length = 10;
        if (value.length() < 10) {
          substring_length = value.length();
        }
        return String.format("Base64 string starting with '%s' (%s)", value.substring(0,substring_length), name);
    }
    return String.format("%s %s %s", type, name, value);
  }

  public String toDisplayString() {
    switch (type) {
    case ENVIRONMENT_VARIABLE:
      return String.format("(%s)", name);

    case FILE_PATH:
      return String.format("(%s)", name);

    case SYSTEM_PROPERTY:
      return String.format("(%s)", name);

    case BASE64_STRING:
      return String.format("(%s)", name);
    }
    return String.format("%s %s %s", type, name, value);
  }

  public String getPath() {
    String path = null;
    switch (this.type) {
    case ENVIRONMENT_VARIABLE:
      path = System.getenv(this.value);
      break;
    case FILE_PATH:
      path = this.value;
      break;
    case SYSTEM_PROPERTY:
      path = System.getProperty(this.value);
      break;
    case BASE64_STRING:
      path = this.value;
      break;
    default:
      throw new RuntimeException("Unknown location type");
    }
    if (path != null && path.startsWith("file:")){
      path = path.substring(5);
    }
    return path;
  }

}
