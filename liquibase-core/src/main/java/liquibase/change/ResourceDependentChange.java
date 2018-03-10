package liquibase.change;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceDependentChange {
    InputStream openStream() throws IOException;
}
