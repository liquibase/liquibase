package liquibase.util.plugin;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.parser.ChangeLogParser;

public class ClassPathScannerTest {
    
    @Test
    public void getClasses() throws Exception {
        Class[] classes = ClassPathScanner.getInstance().getClasses("liquibase.parser", ChangeLogParser.class);
        assertTrue(classes.length > 0);

    }
}
