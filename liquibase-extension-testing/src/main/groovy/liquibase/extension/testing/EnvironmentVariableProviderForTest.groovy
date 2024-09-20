package liquibase.extension.testing

import liquibase.configuration.core.EnvironmentValueProvider

class EnvironmentVariableProviderForTest extends EnvironmentValueProvider {
    private Map<?, ?> localMap = new HashMap<>()
    EnvironmentVariableProviderForTest(Map add, String[] remove) {
        add && add.each { it->
            localMap.put(it.key, it.value)
        }
        remove && remove.each { it->
            localMap.remove(it)
        }
    }

    @Override
    int getPrecedence() {
        return super.getPrecedence() - 1
    }

    @Override
    protected Map<?, ?> getMap() {
        return localMap
    }
}
