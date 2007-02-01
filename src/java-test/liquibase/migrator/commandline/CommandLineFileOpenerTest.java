package liquibase.migrator.commandline;

import junit.framework.*;
import liquibase.migrator.commandline.CommandLineFileOpener;
import liquibase.migrator.AbstractFileOpenerTest;
import liquibase.migrator.FileOpener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class CommandLineFileOpenerTest extends AbstractFileOpenerTest {

    protected FileOpener createFileOpener() {
        return new CommandLineFileOpener(Thread.currentThread().getContextClassLoader());
    }
}