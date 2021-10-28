package liquibase.ui;

public abstract class UiIllegalArgumentExceptionMessageHandler {

    public static IllegalArgumentException addPrefixToExceptionMessage(IllegalArgumentException ex, String input) {
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            return new IllegalArgumentException(
                    String.format("Invalid value: '%s': %s", input, ex.getCause().getMessage()), ex);
        }
        if (ex.getMessage() != null) {
            return new IllegalArgumentException(
                    String.format("Invalid value: '%s': %s", input, ex.getMessage()), ex);
        }
        return ex;
    }
}
