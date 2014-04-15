package liquibase.change;

public class VerificationResult {

    protected Throwable exception;
    protected boolean verified = true;
    private boolean passed;
    private String message;

    public VerificationResult(boolean passed) {
        this(passed, null);
    }

    public VerificationResult(boolean passed, String failedMessage) {
        this.passed = passed;
        if (!passed) {
            this.message = failedMessage;
        }
    }

    public boolean getVerified() {
        return verified;
    }

    public boolean getPassed() {
        return passed;
    }

    public boolean getVerifiedPassed() {
        return verified && passed;
    }

    public boolean getVerifiedFailed() {
        return verified && !passed;
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

    /**
     * Convenience method to run another check for this verification result. If this result already failed, this will be a no-op.
     * If this result had passed before and the new checkResult fails this result will change to failed.
     */
    public void additionalCheck(boolean newPassedValue, String failMessage) {
        if (this.passed && !newPassedValue) {
            this.passed = false;
            this.message = failMessage;
        }
    }

    public static class Unverified extends VerificationResult {
        public Unverified(String message) {
            super(false, message);
            this.verified = false;
        }

        public Unverified(Throwable e) {
            this(e.getMessage());
            this.exception = e;
        }
    }
}
