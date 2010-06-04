package liquibase.exception;

import java.util.ArrayList;
import java.util.List;

public class Warnings {

    private List<String> messages = new ArrayList<String>();

    public void addWarning(String warning) {
        messages.add(warning);
    }

    public void addAll(Warnings warnings) {
        this.messages.addAll(warnings.getMessages());
    }

    public List<String> getMessages() {
        return messages;
    }

    public boolean hasWarnings() {
        return messages.size() > 0;
    }
}
