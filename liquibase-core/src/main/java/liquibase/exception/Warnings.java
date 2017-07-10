package liquibase.exception;

import java.util.ArrayList;
import java.util.List;

public class Warnings {

    private List<String> messages = new ArrayList<>();

    public Warnings addWarning(String warning) {
        messages.add(warning);
        return this;
    }

    public Warnings addAll(Warnings warnings) {
        if (warnings != null) {
            this.messages.addAll(warnings.getMessages());
        }
        return this;
    }

    public List<String> getMessages() {
        return messages;
    }

    public boolean hasWarnings() {
        return !messages.isEmpty();
    }
}
