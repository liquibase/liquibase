package liquibase.analytics

import liquibase.Scope
import liquibase.analytics.configuration.AnalyticsArgs
import liquibase.analytics.configuration.AnalyticsConfiguration
import liquibase.analytics.configuration.AnalyticsConfigurationFactory
import spock.lang.Specification
import spock.lang.Unroll

class AnalyticsArgsTest extends Specification {

    @Unroll
    def "test all permutations of options for enabling/disabling for oss"(Boolean userCliOption, boolean remoteOssEnabled, boolean isEnabled) {
        setup:
        def telemetryConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class)
        def existingConfig = telemetryConfigurationFactory.getPlugin()
        telemetryConfigurationFactory.removeInstance(existingConfig)
        def mockConfig = new AnalyticsConfiguration() {
            @Override
            int getPriority() {
                return PRIORITY_SPECIALIZED
            }

            @Override
            boolean isOssTelemetryEnabled() throws Exception {
                return remoteOssEnabled
            }

            @Override
            boolean isProTelemetryEnabled() throws Exception {
                return true
            }
        }
        telemetryConfigurationFactory.register(mockConfig)

        when:
        Map<String, ?> scopeKeys = new HashMap<>()
        scopeKeys.put(AnalyticsArgs.ENABLED.getKey(), userCliOption)
        Boolean actuallyEnabled = Scope.child(scopeKeys, () -> {
            return AnalyticsArgs.isTelemetryEnabled()
        } as Scope.ScopedRunnerWithReturn)

        then:
        actuallyEnabled == isEnabled

        cleanup:
        telemetryConfigurationFactory.removeInstance(mockConfig)
        telemetryConfigurationFactory.register(existingConfig)

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
