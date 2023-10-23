package liquibase.exception;

import java.util.*;

public class Warnings {

    //use linkedHashSet to keep them in order, but don't duplicate warnings that have already been logged
    private final LinkedHashSet<String> messages = new LinkedHashSet<>();

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
        return new ArrayList<>(messages);
    }

    public boolean hasWarnings() {
        return !messages.isEmpty();
    }
}
