package liquibase.sdk.verifytest;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.util.MD5Util;
import liquibase.util.StringUtils;

import java.util.*;

public class TestPermutation implements Comparable<TestPermutation> {

    private String notRanMessage;
    private SortedMap<String, Value> data = new TreeMap<String, Value>();
    private SortedMap<String,Value> description = new TreeMap<String, Value>();

    private String key = "";
    private String tableKey = "";
    private SortedMap<String,Value> notes = new TreeMap<String, Value>();

    private Setup setup;
    private Verification verification;
    private Cleanup cleanup;

    private boolean valid = true;
    private boolean verified = false;
    private boolean canVerify;

    public static OkResult OK = new OkResult();
    private SortedSet<String> tableParameters;
    private TestPermutation previousRun;
    private boolean canSave = false;

    public TestPermutation(Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                describe(entry.getKey(), entry.getValue());
            }
        }
        recomputeKey();
    }

    public String getKey() {
        return key;
    }

    public boolean getCanVerify() {
        return canVerify;
    }

    public void setCanVerify(boolean canVerify) {
        this.canVerify = canVerify;
    }

    public boolean isValid() {
        return valid;
    }

    public TestPermutation setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    public String getNotRanMessage() {
        return notRanMessage;
    }

    public TestPermutation setNotRanMessage(String notRanMessage) {
        this.notRanMessage = notRanMessage;
        return this;
    }

    public SortedSet<String> getTableParameters() {
        return tableParameters;
    }

    public TestPermutation setup(Setup setup) {
        this.setup = setup;
        return this;
    }

    public SortedMap<String, Value> getDescription() {
        return description;
    }

    public String getTableKey() {
        return tableKey;
    }

    public SortedMap<String, Value> getNotes() {
        return notes;
    }

    public SortedMap<String, Value> getData() {
        return data;
    }

    public void describe(String key, Object value) {
        describe(key, value, OutputFormat.DefaultFormat);
    }

    public void describe(String key, Object value, OutputFormat outputFormat) {
        description.put(key, new Value(value, outputFormat));
    }

    protected void recomputeKey() {
        if (tableParameters != null && tableParameters.size() > 0) {
            SortedMap<String, Value> tableDescription = new TreeMap<String, Value>();
            for (Map.Entry<String, Value> rowEntry : description.entrySet()) {
                if (!tableParameters.contains(rowEntry.getKey())) {
                    tableDescription.put(rowEntry.getKey(), rowEntry.getValue());
                }

            }
            tableKey = toKey(tableDescription);
        } else {
            tableKey = "";
        }
        if (description.size() == 0) {
            key = "";
        } else {
            key = toKey(description);
        }
    }

    protected String toKey(SortedMap<String, Value> description) {
        StringUtils.StringUtilsFormatter formatter = new StringUtils.StringUtilsFormatter() {
            @Override
            public String toString(Object obj) {
                return ((Value) obj).serialize();
            }
        };


        return MD5Util.computeMD5(StringUtils.join(description, ",", formatter)).substring(0, 6);
    }

    public TestPermutation note(String key, Object value) {
        note(key, value, OutputFormat.DefaultFormat);
        return this;
    }

    public TestPermutation note(String key, Object value, OutputFormat outputFormat) {
        notes.put(key, new Value(value, outputFormat));
        return this;
    }

    public TestPermutation data(String key, Object value) {
        data(key, value, OutputFormat.DefaultFormat);
        return this;
    }

    public TestPermutation data(String key, Object value, OutputFormat outputFormat) {
        data.put(key, new Value(value, outputFormat));
        return this;
    }

    public TestPermutation cleanup(Cleanup cleanup) {
        this.cleanup = cleanup;
        return this;
    }

    public TestPermutation test() throws Exception {
        if (notRanMessage != null) {
            return this;
        }

        if (previousRun != null) {
            if (previousRun.getVerified()) {
                boolean allEqual = true;
                if (previousRun.getData().size() == this.getData().size()) {
                    for (Map.Entry<String, Value> previousData : previousRun.getData().entrySet()) {
                        if (!previousData.getValue().serialize().equals(this.getData().get(previousData.getKey()).serialize())) {
                            allEqual = false;
                            break;
                        }
                    }
                }
                if (allEqual) {
                    this.setValid(true);
                    this.setVerified(true);
                    canSave = true;
                    return this;
                }
            }
        }

        try {
            if (setup != null) {
                SetupResult result = setup.run();

                if (result == null) {
                    throw new UnexpectedLiquibaseException("No result returned by setup");
                } else {
                    if (!result.isValid()) {
                        valid = false;
                        canVerify = false;
                        notRanMessage = result.getMessage();
                    } else if (!result.canVerify()) {
                        canVerify = false;
                        notRanMessage = result.getMessage();
                    } else {
                        valid = true;
                        canVerify = true;
                    }
                }
            }
        } catch (Throwable e) {
            valid = false;
            canVerify = false;
            SortedMap<String, Value> fullDescription = new TreeMap<String, Value>(description);
            String message = "Error executing setup\n"+
                    "Description: "+ output(fullDescription)+"\n"+
                    "Notes: "+output(notes)+"\n"+
                    "Data: "+output(data);
            notRanMessage = e.getMessage();
            throw new UnexpectedLiquibaseException(message, e);
        }

        if (!valid || !canVerify) {
            canSave = true;
            return this;
        }

        Exception cleanupError = null;
        try {
            try {
                if (this.verification == null) {
                    throw new UnexpectedLiquibaseSdkException("No expectation set");
                } else {
                    verification.run();
                }
            } catch (CannotVerifyException e) {
                this.verified = false;
            } catch (Throwable e) {
                String message = "Error executing verification\n"+
                        "Description: "+ output(description)+"\n"+
                        "Notes: "+output(notes)+"\n"+
                        "Data: "+output(data);
                throw new RuntimeException(message, e);
            }
            this.verified = true;
        } finally {
            if (cleanup != null) {
                try {
                    cleanup.run();
                } catch (Exception e) {
                    cleanupError = e;
                }
            }
        }

        if (cleanupError != null) {
            throw new UnexpectedLiquibaseSdkException("Cleanup error", cleanupError);
        }

        canSave = true;
        return this;
    }

    @Override
    public int compareTo(TestPermutation o) {
        int i = this.getTableKey().compareTo(o.getTableKey());
        if (i == 0) {
            return this.getKey().compareTo(o.getKey());
        }
        return i;
    }

    private String output(SortedMap<String, Value> map) {
        List<String> out = new ArrayList<String>();
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            out.add(entry.getKey()+"=\""+entry.getValue().serialize()+"\"");
        }

        return StringUtils.join(out, ", ");
    }

    public boolean getCanSave() {
        return canSave;
    }

    public boolean getVerified() {
        return verified;
    }

    public TestPermutation setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public TestPermutation asTable(Collection<String> tableParameters) {
        this.tableParameters = new TreeSet<String>(tableParameters);
        recomputeKey();
        return this;
    }

    public TestPermutation setPreviousRun(TestPermutation previousRun) {
        this.previousRun = previousRun;
        return this;
    }

    public TestPermutation expect(Verification logic) {
        verification = logic;
        return this;
    }

    public static interface SetupResult {
        boolean isValid();
        boolean canVerify();
        String getMessage();
    }

    public static class Invalid implements SetupResult {

        private String message;

        public Invalid(String message) {
            this.message = message;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean canVerify() {
            return false;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    public static class CannotVerify implements SetupResult {

        private String message;

        public CannotVerify(String message) {
            this.message = message;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean canVerify() {
            return false;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }


    public static class OkResult implements SetupResult {

        public OkResult() {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean canVerify() {
            return true;
        }

        @Override
        public String getMessage() {
            return null;
        }
    }



    public static interface Setup {
        public SetupResult run() throws Exception;
    }

    public static interface Verification {
        public void run();
    }

    public static interface Cleanup {
        public void run();
    }

    public static class CannotVerifyException extends RuntimeException {
        public CannotVerifyException(String message) {
            super(message);
        }

        public CannotVerifyException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    public static class Value {
        private Object value;
        private OutputFormat format;
        private String stringValue;

        public Value(Object value, OutputFormat format) {
            this.value = value;
            this.format = format;
        }

        public Object getValue() {
            return value;
        }

        public String serialize() {
            if (stringValue == null) {
                stringValue = format.format(value);
            }
            return stringValue;
        }
    }

}
