package liquibase;

/**
 * Enum to control the level of validation to check if a change's supports method is properly implemented.
 */
public enum SupportsMethodValidationLevelsEnum {
        OFF,
        WARN,
        FAIL
    }
