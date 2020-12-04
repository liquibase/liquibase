package liquibase.io;

import liquibase.exception.LiquibaseException;
import liquibase.ui.ConsoleDelegate;
import liquibase.ui.ConsoleUIService;
import liquibase.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

public class MockUIService extends ConsoleUIService {
    private String[] returnValues = null;
    final private int timerValue;

    public MockUIService(int timerValue, String... returnValues) {
        this.returnValues = returnValues;
        this.timerValue = timerValue;
    }
    protected ConsoleDelegate getConsoleDelegate() throws LiquibaseException {
        return new MockConsoleDelegate(timerValue, returnValues);
    }
}
