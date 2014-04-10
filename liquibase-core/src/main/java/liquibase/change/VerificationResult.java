package liquibase.change;

public class VerificationResult {

    protected Throwable exception;
    protected boolean verified = true;
    private boolean passed;
    private String message;

    public VerificationResult(boolean passed) {
        this(passed, null);
    }

    public VerificationResult(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }

    public boolean getVerified() {
        return verified;
    }

    public boolean getPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public String toString() {
        String out;
        if (verified) {
            if (passed) {
                out = "Passed verification";
            } else {
                out = "Failed verification";
            }
        } else {
            out = "Could not verify";
        }
        if (message != null) {
            out += ": "+message;
        }

        return out;
    }

    public static class Failed extends VerificationResult {
        public Failed(String message) {
            super(false, message);
            this.verified = false;
        }

        public Failed(Throwable e) {
            this(e.getMessage());
            this.exception = e;
        }
    }
}
