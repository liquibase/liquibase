package liquibase.change;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceDependentChange {
    InputStream openSqlStream() throws IOException;
}
