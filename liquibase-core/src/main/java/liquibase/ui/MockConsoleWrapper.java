package liquibase.ui;

import java.util.*;

public class MockConsoleWrapper extends ConsoleUIService.ConsoleWrapper {

    final private List<String> responses;

    public MockConsoleWrapper(String... responses) {
        super(null);
        this.responses = new ArrayList<>(Arrays.asList(responses));
    }

    @Override
    public boolean supportsInput() {
        return true;
    }

    @Override
    public String readLine() {
        return responses.remove(0);
    }
}
