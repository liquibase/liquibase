package liquibase.logging.mdc;

import liquibase.Scope;

import java.io.Closeable;
import java.io.IOException;

public class MdcObject implements Closeable {

    private final String key;
    private final String value;

    public MdcObject(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void close() throws IOException {
        Scope.getCurrentScope().getMdcManager().remove(key);
    }
}
