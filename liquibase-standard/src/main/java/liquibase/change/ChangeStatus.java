package liquibase.change;

import lombok.Getter;

public class ChangeStatus {

    @Getter
    protected Throwable exception;
    private String message;
    private Status status;

    public ChangeStatus() {
    }

    public ChangeStatus assertComplete(boolean complete, String incompleteMessage) {
        if (complete) {
            if (status == null) {
                status = Status.complete;
            }
        } else {
            if (this.status == Status.complete) {
                this.message = incompleteMessage;
                this.status = Status.incorrect;
            } else if ((this.status != Status.notApplied) && (this.status != Status.unknown)) {
                this.message = incompleteMessage;
                this.status = Status.notApplied;
            }
        }
        return this;
    }


    public ChangeStatus assertCorrect(boolean correct, String incorrectMessage) {
        if (correct) {
            if (status == null) {
                status = Status.complete;
            }
        } else {
            if ((this.status == null) || (this.status == Status.complete)) {
                this.status = Status.incorrect;
                this.message = incorrectMessage;
            }
        }

        return this;
    }


    public ChangeStatus unknown(String message) {
        this.status = Status.unknown;
        this.message = message;

        return this;
    }

    public ChangeStatus unknown(Exception exception) {
        this.exception = exception;
        return unknown(exception.getMessage());
    }

    public Status getStatus() {
        if (this.status == null) {
            return Status.unknown;
        }
        return this.status;
    }

    public String getMessage() {
        if (this.status == null) {
            return "No tests done";
        } else if (this.status == Status.complete) {
            return null;
        }
        return message;
    }

    @Override
    public String toString() {
        String out = getStatus().getName();

        String message = getMessage();
        if (message != null) {
            out += ": "+ message;
        }

        return out;
    }

    public enum Status {
        complete("Complete"),
        incorrect("Incorrect"),
        notApplied("Not Applied"),
        unknown("Unknown");

        private final String name;

        Status(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
