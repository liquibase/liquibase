package liquibase.migrator.commandline;

import liquibase.migrator.AbstractFileOpenerTest;
import liquibase.migrator.FileOpener;

public class CommandLineFileOpenerTest extends AbstractFileOpenerTest {

    protected FileOpener createFileOpener() {
        return new CommandLineFileOpener(Thread.currentThread().getContextClassLoader());
    }
}