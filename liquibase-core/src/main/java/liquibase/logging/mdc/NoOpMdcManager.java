package liquibase.logging.mdc;

import liquibase.plugin.Plugin;

import java.util.Collections;
import java.util.Map;

/**
 * Default MDC manager, which does nothing.
 */
public class NoOpMdcManager implements MdcManager {
    @Override
    public MdcObject put(String key, String value) {
        return new MdcObject(key, value);
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void clear() {

    }

    @Override
    public int getPriority() {
        return Plugin.PRIORITY_DEFAULT;
    }

    @Override
    public Map<String, String> getAll() {
        return Collections.emptyMap();
    }
}
