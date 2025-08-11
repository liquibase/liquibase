package liquibase.logging.mdc;

import liquibase.Beta;
import liquibase.plugin.Plugin;

import java.util.List;
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
     * Puts a context value (the <code>value</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     * @param removeWhenScopeExits if true, this key value pair will be automatically removed from the MDC when this
     *                             scope exits. If there is not a demonstrable reason for setting this parameter to false
     *                             then it should be set to true.
     */
    @Beta
    MdcObject put(String key, String value, boolean removeWhenScopeExits);

    /**
     * Puts a context value (the <code>values</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     */
    @Beta
    MdcObject put(String key, Map<String, Object> values);

    /**
     * Puts a context value (the <code>values</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     * @param removeWhenScopeExits if true, this key value pair will be automatically removed from the MDC when this
     *                             scope exits. If there is not a demonstrable reason for setting this parameter to false
     *                             then it should be set to true.
     */
    @Beta
    MdcObject put(String key, Map<String, Object> values, boolean removeWhenScopeExits);

    /**
     * Puts a context value (the <code>values</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     */
    @Beta
    MdcObject put(String key, List<? extends CustomMdcObject> values);

    /**
     * Puts a context value (the <code>customMdcObject</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     */
    @Beta
    MdcObject put(String key, CustomMdcObject customMdcObject);

    /**
     * Puts a context value (the <code>customMdcObject</code> parameter) as identified with the <code>key</code> parameter into
     * the MDC. The caller is responsible for cleaning up this entry at an appropriate time.
     * @param removeWhenScopeExits if true, this key value pair will be automatically removed from the MDC when this
     *                             scope exits. If there is not a demonstrable reason for setting this parameter to false
     *                             then it should be set to true.
     */
    @Beta
    MdcObject put(String key, CustomMdcObject customMdcObject, boolean removeWhenScopeExits);

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
