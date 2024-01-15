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
    public MdcObject put(String key, String value, boolean removeWhenScopeExits) {
        return new MdcObject(key, value);
    }

    @Override
    public MdcObject put(String key, Map<String, Object> values) {
        return new MdcObject(key, values);
    }

    @Override
    public MdcObject put(String key, Map<String, Object> values, boolean removeWhenScopeExits) {
        return new MdcObject(key, values);
    }

    @Override
    public MdcObject put(String key, CustomMdcObject customMdcObject) {
        return new MdcObject(key, customMdcObject);
    }

    @Override
    public MdcObject put(String key, CustomMdcObject customMdcObject, boolean removeWhenScopeExits) {
        return new MdcObject(key, customMdcObject);
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
    public Map<String, Object> getAll() {
        return Collections.emptyMap();
    }
}
