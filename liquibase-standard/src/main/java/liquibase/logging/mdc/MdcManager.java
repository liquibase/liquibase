package liquibase.logging.mdc;

import liquibase.Beta;
import liquibase.plugin.Plugin;

import java.util.Map;

@Beta
public interface MdcManager extends Plugin {

    /**
     * Puts a context value (the <code>value</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     */
    @Beta
    MdcObject put(String key, String value);

    /**
     * Puts a context value (the <code>values</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     */
    @Beta
    MdcObject put(String key, Map<String, Object> values);

    /**
     * Puts a context value (the <code>customMdcObject</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     */
    @Beta
    MdcObject put(String key, CustomMdcObject customMdcObject);

    /**
     * Removes the context value identified by the <code>key</code> parameter.
     */
    @Beta
    void remove(String key);

    /**
     * Clear the MDC.
     */
    @Beta
    void clear();

    @Beta
    int getPriority();

    @Beta
    Map<String, Object> getAll();
}
