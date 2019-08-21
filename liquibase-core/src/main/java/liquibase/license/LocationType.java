package liquibase.license;

/**
 * Enumerate the different places that a license can be installed from.
 *
 */
public enum LocationType {
  ENVIRONMENT_VARIABLE,
  SYSTEM_PROPERTY,
  FILE_PATH,
  BASE64_STRING
}
