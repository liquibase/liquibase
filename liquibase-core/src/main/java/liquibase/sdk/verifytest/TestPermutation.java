package liquibase.sdk.verifytest;

import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.util.MD5Util;
import liquibase.util.StringUtils;

import java.util.*;

public class TestPermutation {

    private String notVerifiedMessage;
    private SortedMap<String, Value> data = new TreeMap<String, Value>();
    private SortedMap<String,Value> description = new TreeMap<String, Value>();
    private String key = "";
    private String longKey = "";
    private SortedMap<String,Value> notes = new TreeMap<String, Value>();

    private List<Setup> setupCommands = new ArrayList<Setup>();
    private List<Verification> verifications = new ArrayList<Verification>();
    private List<Cleanup> cleanupCommands = new ArrayList<Cleanup>();

    private boolean verified = false;
    private boolean canVerify;

    public TestPermutation(VerifiedTest test) {
        test.addPermutation(this);
    }

    public String getKey() {
        return key;
    }

    public String getNotVerifiedMessage() {
        return notVerifiedMessage;
    }

    public boolean getCanVerify() {
        return canVerify;
    }

    public void setCanVerify(boolean canVerify) {
        this.canVerify = canVerify;
    }

    public void setNotVerifiedMessage(String notVerifiedMessage) {
        this.notVerifiedMessage = notVerifiedMessage;
    }

    public List<Setup> getSetup() {
        return setupCommands;
    }

    public void addAssertion(Setup setup) {
        this.setupCommands.add(setup);
    }

    public void addSetup(Setup setup) {
        this.setupCommands.add(setup);
    }

    public SortedMap<String, Value> getDescription() {
        return description;
    }

    public String getLongKey() {
        return longKey;
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
        recomputeKey();
    }

    protected void recomputeKey() {
        longKey = StringUtils.join(description, ",", new StringUtils.StringUtilsFormatter() {
            @Override
            public String toString(Object obj) {
                return ((Value) obj).serialize();
            }
        });
        key = MD5Util.computeMD5(longKey);
    }

    public void note(String key, Object value) {
        note(key, value, OutputFormat.DefaultFormat);
    }

    public void note(String key, Object value, OutputFormat outputFormat) {
        notes.put(key, new Value(value, outputFormat));
    }

    public void data(String key, Object value) {
        data(key, value, OutputFormat.DefaultFormat);
    }

    public void data(String key, Object value, OutputFormat outputFormat) {
        data.put(key, new Value(value, outputFormat));
    }

    public List<Verification> getVerifications() {
        return verifications;
    }

    public void addVerification(Verification verification) {
        verifications.add(verification);
    }

    public List<Cleanup> getCleanup() {
        return cleanupCommands;
    }

    public void addCleanup(Cleanup cleanup) {
        cleanupCommands.add(cleanup);
    }

    public void test(VerifiedTest test) throws Exception {
        TestPermutation previousRun = VerifiedTestFactory.getInstance().getSavedRun(test, this);

        if (notVerifiedMessage != null) {
            save(test);
            return;
        }

        try {
            for (Setup setup : this.setupCommands) {
                String message = setup.run();

                if (message != null) {
                    canVerify = false;
                    notVerifiedMessage = message;
                    break;
                }
            }
        } catch (Throwable e) {
            String message = "Error executing setup\n"+
                    "Description: "+ output(description)+"\n"+
                    "Notes: "+output(notes)+"\n"+
                    "Data: "+output(data);
            throw new RuntimeException(message, e);
        }

        if (!canVerify) {
            save(test);
            return;
        }

        Exception cleanupError = null;
        try {
            try {
                for (Verification verification : this.verifications) {
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
            for (Cleanup cleanup : cleanupCommands) {
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

        save(test);
    }

    protected void save(VerifiedTest test) throws Exception {
        VerifiedTestFactory.getInstance().saveRun(test, this);
    }

    private String output(SortedMap<String, Value> map) {
        List<String> out = new ArrayList<String>();
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            out.add(entry.getKey()+"="+entry.getValue().serialize());
        }

        return StringUtils.join(out, ", ");
    }

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public static interface Setup {
        public String run();
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

        public Value(Object value, OutputFormat format) {
            this.value = value;
            this.format = format;
        }

        public String serialize() {
            return format.format(value);
        }
    }

}
