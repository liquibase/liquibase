package liquibase.io;

import liquibase.exception.LiquibaseException;
import liquibase.ui.ConsoleDelegate;
import liquibase.ui.ConsoleUIService;

public class MockUIService extends ConsoleUIService {
    final private String returnValue;
    final private int timerValue;

    public MockUIService(String returnValue, int timerValue) {
        this.returnValue = returnValue;
        this.timerValue = timerValue;
    }
    protected ConsoleDelegate getConsoleDelegate() throws LiquibaseException {
        return new MockConsoleDelegate(returnValue, timerValue);
    }
}
