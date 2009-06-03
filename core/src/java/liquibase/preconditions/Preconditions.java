package liquibase.preconditions;

import java.util.ArrayList;
import java.util.List;

import liquibase.util.StringUtils;

public class Preconditions extends AndPrecondition {

    public enum FailOption {
        HALT("HALT"),
        CONTINUE("CONTINUE"),
        MARK_RAN("MARK_RAN"),
        WARN("WARN");

        String key;

        FailOption(String key) {
            this.key = key;
        }

        public String toString() {
            return key;
        }
    }

    public enum ErrorOption {
        HALT("HALT"),
        CONTINUE("CONTINUE"),
        MARK_RAN("MARK_RAN"),
        WARN("WARN");

        String key;

        ErrorOption(String key) {
            this.key = key;
        }

        public String toString() {
            return key;
        }
    }

    public enum OnUpdateSQLOption {
        IGNORE("IGNORE"),
        RUN("RUN"),
        FAIL("FAIL");

        String key;

        OnUpdateSQLOption(String key) {
            this.key = key;
        }

        public String toString() {
            return key;
        }
    }

    private FailOption onFail = FailOption.HALT;
    private ErrorOption onError = ErrorOption.HALT;
    private OnUpdateSQLOption onUpdateSQL = OnUpdateSQLOption.IGNORE;

    public FailOption getOnFail() {
        return onFail;
    }

    public void setOnFail(String onFail) {
        if (onFail == null) {
            this.onFail = FailOption.HALT;
        } else {
            for (FailOption option : FailOption.values()) {
                if (option.key.equalsIgnoreCase(onFail)) {
                    this.onFail = option;
                    return;
                }
            }
            List<String> possibleOptions = new ArrayList<String>();
            for (FailOption option : FailOption.values()) {
                possibleOptions.add(option.key);
            }
            throw new RuntimeException("Unknown onFail attribute value '"+onFail+"'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
        }
    }

    public ErrorOption getOnError() {
        return onError;
    }

    public void setOnError(String onError) {
        if (onError == null) {
            this.onError = ErrorOption.HALT;
        } else {
            for (ErrorOption option : ErrorOption.values()) {
                if (option.key.equalsIgnoreCase(onError)) {
                    this.onError = option;
                    return;
                }
            }
            List<String> possibleOptions = new ArrayList<String>();
            for (ErrorOption option : ErrorOption.values()) {
                possibleOptions.add(option.key);
            }
            throw new RuntimeException("Unknown onError attribute value '"+onError+"'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
        }
    }

    /**
     * Get the value of onUpdateSQL attribute.
     *
     * @return value of onUpdateSQL attribute
     */
    public OnUpdateSQLOption getOnUpdateSQL()
    {
        return onUpdateSQL;
    }

    /**
     * Set onUpdateSQL attribute.
     *
     * @param onUpdateSQL value of onUpdateSQL attribute. If null, will default to FAIL.
     */
    public void setOnUpdateSQL(final String onUpdateSQL)
    {
        if (onUpdateSQL == null) {
            this.onUpdateSQL = OnUpdateSQLOption.IGNORE;
        } else {
            for (OnUpdateSQLOption option : OnUpdateSQLOption.values()) {
                if (option.key.equalsIgnoreCase(onUpdateSQL)) {
                    this.onUpdateSQL = option;
                    return;
                }
            }
            List<String> possibleOptions = new ArrayList<String>();
            for (OnUpdateSQLOption option : OnUpdateSQLOption.values()) {
                possibleOptions.add(option.key);
            }
            throw new RuntimeException("Unknown onUpdateSQL attribute value '"+onUpdateSQL+"'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
        }
    }
}
