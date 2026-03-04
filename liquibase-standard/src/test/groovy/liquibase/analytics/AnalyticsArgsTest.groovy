package liquibase.analytics

import liquibase.Scope
import liquibase.analytics.configuration.AnalyticsArgs
import liquibase.analytics.configuration.AnalyticsConfiguration
import liquibase.analytics.configuration.AnalyticsConfigurationFactory
import spock.lang.Specification
import spock.lang.Unroll

class AnalyticsArgsTest extends Specification {

    @Unroll
    def "test all permutations of options for enabling/disabling for community"(Boolean userCliOption, boolean remoteOssEnabled, boolean isEnabled) {
        setup:
        def analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class)
        def existingConfig = analyticsConfigurationFactory.getPlugin()
        analyticsConfigurationFactory.removeInstance(existingConfig)
        def mockConfig = new AnalyticsConfiguration() {
            @Override
            int getPriority() {
                return PRIORITY_SPECIALIZED
            }

            @Override
            boolean isOssAnalyticsEnabled() throws Exception {
                return remoteOssEnabled
            }

            @Override
            boolean isProAnalyticsEnabled() throws Exception {
                return true
            }
        }
        analyticsConfigurationFactory.register(mockConfig)
        def analyticsFactory = Scope.getCurrentScope().getSingleton(AnalyticsFactory.class)
        def oldListener = analyticsFactory.getListener()
        analyticsFactory.removeInstance(oldListener)
        def listener = new LiquibaseAnalyticsListener() {
            @Override
            int getPriority() {
                return PRIORITY_SPECIALIZED + 100
            }
        }
        analyticsFactory.register(listener)

        when:
        Map<String, ?> scopeKeys = new HashMap<>()
        scopeKeys.put(AnalyticsArgs.ENABLED.getKey(), userCliOption)
        scopeKeys.put(AnalyticsArgs.DEV_OVERRIDE.getKey(), true)
        scopeKeys.put(AnalyticsArgs.CONFIG_ENDPOINT_URL.getKey(), "some other value")
        Boolean actuallyEnabled = Scope.child(scopeKeys, () -> {
            return listener.isEnabled()
        } as Scope.ScopedRunnerWithReturn)

        then:
        actuallyEnabled == isEnabled

        cleanup:
        analyticsConfigurationFactory.removeInstance(mockConfig)
        analyticsConfigurationFactory.register(existingConfig)
        analyticsFactory.removeInstance(listener)
        analyticsFactory.register(oldListener)

        where:
        userCliOption | remoteOssEnabled | isEnabled
        true          | true             | true
        true          | false            | false
        false         | true             | false
        false         | false            | false
        null          | true             | true
        null          | false            | false
    }
}
