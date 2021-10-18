//file:noinspection GroovyAccessibility
package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class LiquibaseUtilTest extends Specification {

    def cleanup() {
        LiquibaseUtil.liquibaseBuildProperties = null
    }

    @Unroll
    def getBuildVersionInfo() {
        when:
        LiquibaseUtil.liquibaseBuildProperties = new Properties()
        LiquibaseUtil.liquibaseBuildProperties.put("build.version", version)

        LiquibaseUtil.liquibaseBuildProperties.put("build.timestamp", localBuild ? "unknown" : "2021-09-17 14:06+0000")
        LiquibaseUtil.liquibaseBuildProperties.put("build.branch", localBuild ? "unknown" : "test-branch")
        LiquibaseUtil.liquibaseBuildProperties.put("build.number", localBuild ? "0" : "524")
        LiquibaseUtil.liquibaseBuildProperties.put("build.commit", localBuild ? "unknown" : "7904bb98687d5ade0dae5acc467c90d572f8080f")

        if (proData) {
            LiquibaseUtil.liquibaseBuildProperties.put("build.pro.timestamp", "2021-09-17T14:04:58Z")
            LiquibaseUtil.liquibaseBuildProperties.put("build.pro.branch", "other-branch")
            LiquibaseUtil.liquibaseBuildProperties.put("build.pro.number", "58")
            LiquibaseUtil.liquibaseBuildProperties.put("build.pro.commit", "e5195bb48d412ae8a8a5595a87d8517394072dc0")
        }

        then:
        LiquibaseUtil.getBuildVersionInfo() == expected
        LiquibaseUtil.getBuildVersion() == version

        where:
        version | localBuild | proData | expected
        "1.2.3" | false      | true    | "1.2.3"
        "1.2.3" | false      | false   | "1.2.3"
        "DEV"   | false      | true    | "[Core: test-branch/524/7904bb/2021-09-17 14:06+0000, Pro: other-branch/58/e5195b/2021-09-17T14:04:58Z]"
        "DEV"   | false      | false   | "[Core: test-branch/524/7904bb/2021-09-17 14:06+0000]"
        "DEV"   | true       | true    | "[local build]"
        "DEV"   | true       | false   | "[local build]"
    }
}
